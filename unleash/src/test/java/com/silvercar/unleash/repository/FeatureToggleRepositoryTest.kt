package com.silvercar.unleash.repository

import com.silvercar.unleash.ActivationStrategy
import com.silvercar.unleash.FeatureToggle
import com.silvercar.unleash.repository.FeatureToggleResponse
import com.silvercar.unleash.util.UnleashConfig
import com.silvercar.unleash.util.UnleashConfig.Companion.builder
import com.silvercar.unleash.util.UnleashScheduledExecutor
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import java.net.URISyntaxException
import java.util.*

class FeatureToggleRepositoryTest {
    @Test
    fun no_backup_file_and_no_repository_available_should_give_empty_repo() {
        val config = builder()
            .appName("test")
            .unleashAPI("http://localhost:4242/api/").build()
        val toggleFetcher: ToggleFetcher = HttpToggleFetcher(config)
        val toggleBackupHandler: ToggleBackupHandler = ToggleBackupHandlerFile(config)
        val executor = Mockito.mock(
            UnleashScheduledExecutor::class.java
        )
        val toggleRepository: ToggleRepository =
            FeatureToggleRepository(config, executor, toggleFetcher, toggleBackupHandler)
        Assertions.assertNull(
            toggleRepository.getToggle("unknownFeature"),
            "should be null"
        )
    }

    @Test
    fun backup_toggles_should_be_loaded_at_startup() {
        val config = builder()
            .appName("test")
            .unleashAPI("http://localhost:4242/api/")
            .fetchTogglesInterval(Long.MAX_VALUE)
            .build()
        val toggleBackupHandler = Mockito.mock(
            ToggleBackupHandler::class.java
        )
        val toggleFetcher = Mockito.mock(ToggleFetcher::class.java)
        val executor = Mockito.mock(
            UnleashScheduledExecutor::class.java
        )
        FeatureToggleRepository(config, executor, toggleFetcher, toggleBackupHandler)
        Mockito.verify(toggleBackupHandler, Mockito.times(1)).read()
    }

    @Test
    @Throws(URISyntaxException::class, InterruptedException::class)
    fun feature_toggles_should_be_updated() {
        val toggleFetcher = Mockito.mock(ToggleFetcher::class.java)

        //setup backupHandler
        val toggleBackupHandler = Mockito.mock(
            ToggleBackupHandler::class.java
        )
        var toggleCollection = populatedToggleCollection(
            FeatureToggle(
                "toggleFetcherCalled",
                false,
                Arrays.asList(ActivationStrategy("custom", null))
            )
        )
        Mockito.`when`(toggleBackupHandler.read()).thenReturn(toggleCollection)

        //setup fetcher
        toggleCollection = populatedToggleCollection(
            FeatureToggle(
                "toggleFetcherCalled",
                true,
                Arrays.asList(ActivationStrategy("custom", null))
            )
        )
        val response =
            FeatureToggleResponse(FeatureToggleResponse.Status.CHANGED, toggleCollection)
        Mockito.`when`(toggleFetcher.fetchToggles()).thenReturn(response)

        //init
        val executor = Mockito.mock(
            UnleashScheduledExecutor::class.java
        )
        val runnableArgumentCaptor =
            ArgumentCaptor.forClass(
                Runnable::class.java
            )
        val config = UnleashConfig.Builder()
            .appName("test")
            .unleashAPI("http://localhost:4242/api/")
            .fetchTogglesInterval(200L)
            .build()
        val toggleRepository: ToggleRepository =
            FeatureToggleRepository(config, executor, toggleFetcher, toggleBackupHandler)

        //run the toggleName fetcher callback
        Mockito.verify(executor).setInterval(
            runnableArgumentCaptor.capture(),
            ArgumentMatchers.anyLong(),
            ArgumentMatchers.anyLong()
        )
        Mockito.verify(toggleFetcher, Mockito.times(0)).fetchToggles()
        runnableArgumentCaptor.value.run()
        Mockito.verify(toggleBackupHandler, Mockito.times(1)).read()
        Mockito.verify(toggleFetcher, Mockito.times(1)).fetchToggles()
        Assertions.assertTrue(
            toggleRepository.getToggle("toggleFetcherCalled").isEnabled
        )
    }

    @Test
    fun get_feature_names_should_return_list_of_names() {
        val config = Mockito.mock(UnleashConfig::class.java)
        val executor = Mockito.mock(
            UnleashScheduledExecutor::class.java
        )
        val toggleFetcher = Mockito.mock(ToggleFetcher::class.java)
        val toggleBackupHandler = Mockito.mock(
            ToggleBackupHandler::class.java
        )
        val toggleCollection = populatedToggleCollection(
            FeatureToggle(
                "toggleFeatureName1",
                true,
                Arrays.asList(ActivationStrategy("custom", null))
            ),
            FeatureToggle(
                "toggleFeatureName2",
                true,
                Arrays.asList(ActivationStrategy("custom", null))
            )
        )
        Mockito.`when`(toggleBackupHandler.read()).thenReturn(toggleCollection)
        val toggleRepository: ToggleRepository =
            FeatureToggleRepository(config, executor, toggleFetcher, toggleBackupHandler)
        Assertions.assertTrue(2 == toggleRepository.featureNames.size)
        Assertions.assertTrue(
            "toggleFeatureName2" == toggleRepository.featureNames[1]
        )
    }

    @Test
    fun should_perform_synchronous_fetch_on_initialisation() {
        val config = builder()
            .synchronousFetchOnInitialisation(true)
            .appName("test-sync-update")
            .unleashAPI("http://localhost:8080")
            .build()
        val executor = Mockito.mock(
            UnleashScheduledExecutor::class.java
        )
        val toggleFetcher = Mockito.mock(ToggleFetcher::class.java)
        val toggleBackupHandler = Mockito.mock(
            ToggleBackupHandler::class.java
        )

        //setup fetcher
        val toggleCollection = populatedToggleCollection()
        val response =
            FeatureToggleResponse(FeatureToggleResponse.Status.CHANGED, toggleCollection)
        Mockito.`when`(toggleFetcher.fetchToggles()).thenReturn(response)
        FeatureToggleRepository(config, executor, toggleFetcher, toggleBackupHandler)
        Mockito.verify(toggleFetcher, Mockito.times(1)).fetchToggles()
    }

    @Test
    fun should_not_perform_synchronous_fetch_on_initialisation() {
        val config = builder()
            .synchronousFetchOnInitialisation(false)
            .appName("test-sync-update")
            .unleashAPI("http://localhost:8080")
            .build()
        val executor = Mockito.mock(
            UnleashScheduledExecutor::class.java
        )
        val toggleFetcher = Mockito.mock(ToggleFetcher::class.java)
        val toggleBackupHandler = Mockito.mock(
            ToggleBackupHandler::class.java
        )

        //setup fetcher
        val toggleCollection = populatedToggleCollection()
        val response =
            FeatureToggleResponse(FeatureToggleResponse.Status.CHANGED, toggleCollection)
        Mockito.`when`(toggleFetcher.fetchToggles()).thenReturn(response)
        FeatureToggleRepository(config, executor, toggleFetcher, toggleBackupHandler)
        Mockito.verify(toggleFetcher, Mockito.times(0)).fetchToggles()
    }

    private fun populatedToggleCollection(vararg featureToggles: FeatureToggle): ToggleCollection {
        val list: MutableList<FeatureToggle?> = arrayListOf()
        list.addAll(listOf(*featureToggles))
        return ToggleCollection(list)
    }
}
