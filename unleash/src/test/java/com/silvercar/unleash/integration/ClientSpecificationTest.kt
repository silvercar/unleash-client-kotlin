package com.silvercar.unleash.integration

import com.annimon.stream.Collectors
import com.annimon.stream.Stream
import com.github.jenspiegsa.mockitoextension.ConfigureWireMock
import com.github.jenspiegsa.mockitoextension.InjectServer
import com.github.jenspiegsa.mockitoextension.WireMockExtension
import com.github.jenspiegsa.mockitoextension.WireMockSettings
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.Options
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.silvercar.unleash.DefaultUnleash
import com.silvercar.unleash.Unleash
import com.silvercar.unleash.UnleashContext
import com.silvercar.unleash.util.UnleashConfig.Companion.builder
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.extension.ExtendWith
import java.io.*
import java.net.URI
import java.net.URISyntaxException
import java.util.*

@ExtendWith(WireMockExtension::class)
@WireMockSettings(failOnUnmatchedRequests = false)
class ClientSpecificationTest {
    @ConfigureWireMock
    var options: Options =
        WireMockConfiguration.wireMockConfig()
            .dynamicPort()

    @InjectServer
    var serverMock: WireMockServer? = null

//    @TestFactory
//    @Throws(IOException::class, URISyntaxException::class)
//    // NOTE: We had to use `java.util.stream.Stream` instead of `com.annimon.stream.Stream`
//    // due to JUnit's @TestFactory only allowing the original stream lib
//    fun clientSpecification(): java.util.stream.Stream<DynamicTest> {
//        val content =
//            getFileReader("/client-specification/specifications/index.json")
//        val testDefinitions =
//            Gson().fromJson<List<String>>(
//                content,
//                object :
//                    TypeToken<List<String?>?>() {}.type
//            )
//        val tests: MutableList<DynamicTest> = ArrayList()
//        for (name in testDefinitions) {
//            tests.addAll(createTests(name))
//            tests.addAll(createVariantTests(name))
//        }
//        return tests.stream()
//    }

    @Throws(IOException::class, URISyntaxException::class)
    private fun createTests(fileName: String): List<DynamicTest> {
        val testDefinition = getTestDefinition(fileName)
        val unleash = setupUnleash(testDefinition)

        //Create all test cases in testDefinition.
        return Stream.of(
            testDefinition.tests
        )
            .map { test: TestCase ->
                DynamicTest.dynamicTest(
                    fileName + "/" + test.description
                ) {
                    val result = unleash.isEnabled(
                        test.toggleName,
                        buildContext(test.context)
                    )
                    Assertions.assertEquals(
                        test.expectedResult,
                        result,
                        test.description
                    )
                }
            }
            .collect(Collectors.toList())
    }

    @Throws(IOException::class, URISyntaxException::class)
    private fun createVariantTests(fileName: String): List<DynamicTest> {
        val testDefinition = getTestDefinition(fileName)
        val unleash = setupUnleash(testDefinition)

        //Create all test cases in testDefinition.
        return Stream.of(testDefinition.variantTests)
            .map { test: TestCaseVariant ->
                DynamicTest.dynamicTest(
                    fileName + "/" + test.description
                ) {
                    val result = unleash.getVariant(
                        test.toggleName,
                        buildContext(test.context)
                    )
                    Assertions.assertEquals(
                        test.expectedResult.name,
                        result.name,
                        test.description
                    )
                    Assertions.assertEquals(
                        test.expectedResult.isEnabled,
                        result.isEnabled,
                        test.description
                    )
                    Assertions.assertEquals(
                        test.expectedResult.payload,
                        result.payload,
                        test.description
                    )
                }
            }
            .collect(Collectors.toList())
    }

    @Throws(URISyntaxException::class)
    private fun setupUnleash(testDefinition: TestDefinition): Unleash {
        mockUnleashAPI(testDefinition)

        // Required because the client is available before it may have had the chance to talk with the API
        val backupFile = writeUnleashBackup(testDefinition)

        // Set-up a unleash instance, using mocked API and backup-file
        val config = builder()
            .appName(testDefinition.name)
            .unleashAPI(URI("http://localhost:" + serverMock!!.port() + "/api/"))
            .synchronousFetchOnInitialisation(true)
            .backupFile(backupFile)
            .build()
        return DefaultUnleash(config)
    }

    private fun mockUnleashAPI(definition: TestDefinition) {
        WireMock.stubFor(
            WireMock.get(WireMock.urlEqualTo("/api/client/features"))
                .withHeader("Accept", WireMock.equalTo("application/json"))
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(definition.state.toString())
                )
        )
    }

    @Throws(IOException::class)
    private fun getTestDefinition(fileName: String): TestDefinition {
        val content =
            getFileReader("/client-specification/specifications/$fileName")
        return Gson().fromJson(content, TestDefinition::class.java)
    }

    private fun buildContext(context: UnleashContextDefinition): UnleashContext {
        //TODO: All other properties!
        val builder = UnleashContext.builder()
            .userId(context.userId)
            .sessionId(context.sessionId)
            .remoteAddress(context.remoteAddress)
            .environment(context.environment)
            .appName(context.appName)
        if (context.properties != null) {
            // TODO: Use Kotlins method after Kotlion conversion - original line below
//            context.getProperties().forEach(builder::addProperty);
            for ((key, value) in context.properties) {
                builder.addProperty(key, value)
            }
        }
        return builder.build()
    }

    @Throws(IOException::class)
    private fun getFileReader(filename: String): Reader {
        val `in` = this.javaClass.getResourceAsStream(filename)
        Assertions.assertNotNull(
            `in`, """
     Could not find test specification ($filename).
     You must first run 'mvn test' to download the specifications files
     """.trimIndent()
        )
        val reader = InputStreamReader(`in`)
        return BufferedReader(reader)
    }

    private fun writeUnleashBackup(definition: TestDefinition): String {
        val backupFile = System.getProperty("java.io.tmpdir") +
                File.separatorChar +
                "unleash-test-" +
                definition.name +
                ".json"

        // TODO: we can probably drop this after introduction of `synchronousFetchOnInitialisation`.
        try {
            FileWriter(backupFile)
                .use { writer -> writer.write(definition.state.toString()) }
        } catch (e: IOException) {
            println("Unable to write toggles to file")
        }
        return backupFile
    }
}
