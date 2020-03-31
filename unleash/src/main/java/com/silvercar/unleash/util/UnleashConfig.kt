package com.silvercar.unleash.util

import com.annimon.stream.Optional
import com.silvercar.unleash.CustomHttpHeadersProvider
import com.silvercar.unleash.DefaultCustomHttpHeadersProviderImpl
import com.silvercar.unleash.UnleashContextProvider
import com.silvercar.unleash.event.NoOpSubscriber
import com.silvercar.unleash.event.UnleashSubscriber
import java.io.File
import java.net.*
import java.util.*

class UnleashConfig(
    unleashAPI: URI?,
    customHttpHeaders: Map<String, String>,
    customHttpHeadersProvider: CustomHttpHeadersProvider,
    appName: String,
    environment: String,
    instanceId: String?,
    sdkVersion: String,
    backupFile: String,
    fetchTogglesInterval: Long,
    sendMetricsInterval: Long,
    disableMetrics: Boolean,
    contextProvider: UnleashContextProvider,
    isProxyAuthenticationByJvmProperties: Boolean,
    synchronousFetchOnInitialisation: Boolean,
    unleashScheduledExecutor: UnleashScheduledExecutor?,
    unleashSubscriber: UnleashSubscriber?
) {
    val unleashAPI: URI
    val unleashURLs: UnleashURLs
    val customHttpHeaders: Map<String, String>
    val customHttpHeadersProvider: CustomHttpHeadersProvider
    val appName: String
    val environment: String
    val instanceId: String
    val sdkVersion: String
    val backupFile: String
    val fetchTogglesInterval: Long
    val sendMetricsInterval: Long
    val isDisableMetrics: Boolean
    val isProxyAuthenticationByJvmProperties: Boolean
    val contextProvider: UnleashContextProvider
    val isSynchronousFetchOnInitialisation: Boolean
    val scheduledExecutor: UnleashScheduledExecutor
    val subscriber: UnleashSubscriber
    private fun enableProxyAuthentication() {
        // http.proxyUser http.proxyPassword is only consumed by Apache HTTP Client, for HttpUrlConnection we have to define an Authenticator
        Authenticator.setDefault(ProxyAuthenticator())
    }

    internal class ProxyAuthenticator : Authenticator() {
        public override fun getPasswordAuthentication(): PasswordAuthentication? {
            if (requestorType == RequestorType.PROXY) {
                val proto = requestingProtocol.toLowerCase()
                val proxyHost =
                    System.getProperty("$proto.proxyHost", "")
                val proxyPort =
                    System.getProperty("$proto.proxyPort", "")
                val proxyUser =
                    System.getProperty("$proto.proxyUser", "")
                val proxyPassword =
                    System.getProperty("$proto.proxyPassword", "")

                // Only apply PasswordAuthentication to requests to the proxy itself - if not set just ignore
                if (requestingHost.equals(
                        proxyHost,
                        ignoreCase = true
                    ) && proxyPort.toInt() == requestingPort
                ) {
                    return PasswordAuthentication(proxyUser, proxyPassword.toCharArray())
                }
            }
            return null
        }
    }

    class Builder {
        private var unleashAPI: URI? = null
        private val customHttpHeaders: MutableMap<String, String> =
            HashMap()
        private var customHttpHeadersProvider: CustomHttpHeadersProvider =
            DefaultCustomHttpHeadersProviderImpl()
        private var appName = ""
        private var environment = "default"
        private var instanceId =
            defaultInstanceId
        private val sdkVersion = defaultSdkVersion
        private var backupFile: String? = null
        private var fetchTogglesInterval: Long = 10
        private var sendMetricsInterval: Long = 60
        private var disableMetrics = false
        private var contextProvider =
            UnleashContextProvider.getDefaultProvider()
        private var synchronousFetchOnInitialisation = false
        private var scheduledExecutor: UnleashScheduledExecutor? = null
        private var unleashSubscriber: UnleashSubscriber? = null
        private var isProxyAuthenticationByJvmProperties = false
        fun unleashAPI(unleashAPI: URI?): Builder {
            this.unleashAPI = unleashAPI
            return this
        }

        fun unleashAPI(unleashAPI: String?): Builder {
            this.unleashAPI = URI.create(unleashAPI)
            return this
        }

        fun customHttpHeader(
            name: String,
            value: String
        ): Builder {
            customHttpHeaders[name] = value
            return this
        }

        fun customHttpHeadersProvider(provider: CustomHttpHeadersProvider): Builder {
            customHttpHeadersProvider = provider
            return this
        }

        fun appName(appName: String): Builder {
            this.appName = appName
            return this
        }

        fun environment(environment: String): Builder {
            this.environment = environment
            return this
        }

        fun instanceId(instanceId: String): Builder {
            this.instanceId = instanceId
            return this
        }

        fun fetchTogglesInterval(fetchTogglesInterval: Long): Builder {
            this.fetchTogglesInterval = fetchTogglesInterval
            return this
        }

        fun sendMetricsInterval(sendMetricsInterval: Long): Builder {
            this.sendMetricsInterval = sendMetricsInterval
            return this
        }

        fun disableMetrics(): Builder {
            disableMetrics = true
            return this
        }

        fun backupFile(backupFile: String?): Builder {
            this.backupFile = backupFile
            return this
        }

        fun enableProxyAuthenticationByJvmProperties(): Builder {
            isProxyAuthenticationByJvmProperties = true
            return this
        }

        fun unleashContextProvider(contextProvider: UnleashContextProvider): Builder {
            this.contextProvider = contextProvider
            return this
        }

        fun synchronousFetchOnInitialisation(enable: Boolean): Builder {
            synchronousFetchOnInitialisation = enable
            return this
        }

        fun scheduledExecutor(scheduledExecutor: UnleashScheduledExecutor?): Builder {
            this.scheduledExecutor = scheduledExecutor
            return this
        }

        fun subscriber(unleashSubscriber: UnleashSubscriber?): Builder {
            this.unleashSubscriber = unleashSubscriber
            return this
        }

        private fun getBackupFile(): String {
            return if (!backupFile.isNullOrEmpty()) {
                backupFile as String
            } else {
                val fileName = "unleash-$appName-repo.json"
                System.getProperty("java.io.tmpdir") + File.separatorChar + fileName
            }
        }

        fun build(): UnleashConfig {
            return UnleashConfig(
                unleashAPI,
                customHttpHeaders,
                customHttpHeadersProvider,
                appName,
                environment,
                instanceId,
                sdkVersion,
                getBackupFile(),
                fetchTogglesInterval,
                sendMetricsInterval,
                disableMetrics,
                contextProvider,
                isProxyAuthenticationByJvmProperties,
                synchronousFetchOnInitialisation,
                Optional.ofNullable(scheduledExecutor)
                    .orElseGet { UnleashScheduledExecutorImpl.getInstance() },
                Optional.ofNullable(unleashSubscriber)
                    .orElseGet { NoOpSubscriber() }
            )
        }

        val defaultSdkVersion: String
            get() {
                val version = Optional.ofNullable(
                    javaClass.getPackage().implementationVersion
                )
                    .orElse("development")
                return "unleash-client-java:$version"
            }

        companion object {
            val defaultInstanceId: String
                get() {
                    var hostName = ""
                    try {
                        hostName = InetAddress.getLocalHost().hostName + "-"
                    } catch (e: UnknownHostException) {
                    }
                    return hostName + "generated-" + Math.round(Math.random() * 1000000.0)
                }
        }
    }

    companion object {
        const val UNLEASH_APP_NAME_HEADER = "UNLEASH-APPNAME"
        const val UNLEASH_INSTANCE_ID_HEADER = "UNLEASH-INSTANCEID"

        @JvmStatic
        fun builder(): Builder {
            return Builder()
        }

        @JvmStatic
        fun setRequestProperties(
            connection: HttpURLConnection,
            config: UnleashConfig
        ) {
            connection.setRequestProperty(
                UNLEASH_APP_NAME_HEADER,
                config.appName
            )
            connection.setRequestProperty(
                UNLEASH_INSTANCE_ID_HEADER,
                config.instanceId
            )
            connection.setRequestProperty("User-Agent", config.appName)
            config.customHttpHeaders
                .forEach { (s: String?, s1: String?) ->
                    connection.setRequestProperty(
                        s,
                        s1
                    )
                }
            config.customHttpHeadersProvider.customHeaders
                .forEach { (s: String?, s1: String?) ->
                    connection.setRequestProperty(
                        s,
                        s1
                    )
                }
        }
    }

    init {
        check(appName.isNotEmpty()) { "You are required to specify the unleash appName" }
        checkNotNull(instanceId) { "You are required to specify the unleash instanceId" }
        checkNotNull(unleashAPI) { "You are required to specify the unleashAPI url" }
        checkNotNull(unleashScheduledExecutor) { "You are required to specify a scheduler" }
        checkNotNull(unleashSubscriber) { "You are required to specify a subscriber" }
        if (isProxyAuthenticationByJvmProperties) {
            enableProxyAuthentication()
        }
        this.unleashAPI = unleashAPI
        this.customHttpHeaders = customHttpHeaders
        this.customHttpHeadersProvider = customHttpHeadersProvider
        unleashURLs = UnleashURLs(unleashAPI)
        this.appName = appName
        this.environment = environment
        this.instanceId = instanceId
        this.sdkVersion = sdkVersion
        this.backupFile = backupFile
        this.fetchTogglesInterval = fetchTogglesInterval
        this.sendMetricsInterval = sendMetricsInterval
        isDisableMetrics = disableMetrics
        this.contextProvider = contextProvider
        this.isProxyAuthenticationByJvmProperties = isProxyAuthenticationByJvmProperties
        isSynchronousFetchOnInitialisation = synchronousFetchOnInitialisation
        scheduledExecutor = unleashScheduledExecutor
        subscriber = unleashSubscriber
    }
}
