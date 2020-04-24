package com.silvercar.unleash.util

import com.silvercar.unleash.CustomHttpHeadersProvider
import com.silvercar.unleash.UnleashContext
import com.silvercar.unleash.UnleashContextProvider
import com.silvercar.unleash.event.NoOpSubscriber
import com.silvercar.unleash.event.UnleashSubscriber
import java.io.File
import java.net.URI

fun unleashConfig(func: UnleashConfigBuilder.() -> Unit) = UnleashConfigBuilder().apply { func() }

@Suppress("TooManyFunctions") class UnleashConfigBuilder {
  private var unleashAPI: URI? = null
  private val customHttpHeaders: MutableMap<String, String> = mutableMapOf()
  private var customHttpHeadersProvider: CustomHttpHeadersProvider =
    object : CustomHttpHeadersProvider {
      override fun getCustomHeaders(): Map<String, String> {
        return mutableMapOf()
      }
  }
  private var appName: String? = null
  private var environment = "default"
  private var instanceId = DefaultInstanceIdFactory.getInstance()
  private val sdkVersion = defaultSdkVersion
  private var backupFile: String? = null
  private var fetchTogglesInterval: Long = TEN_SECONDS
  private var sendMetricsInterval: Long = SIXTY_SECONDS
  private var disableMetrics = false
  private var contextProvider: UnleashContextProvider = object : UnleashContextProvider {
    override fun getContext(): UnleashContext {
      return UnleashContext.builder().build()
    }
  }
  private var synchronousFetchOnInitialisation = false
  private var scheduledExecutor: UnleashScheduledExecutor? = null
  private var unleashSubscriber: UnleashSubscriber? = null
  private var isProxyAuthenticationByJvmProperties = false
  private val defaultSdkVersion: String
    get() {
      val implementationVersion = javaClass.getPackage().implementationVersion
      val version = implementationVersion ?: "development"
      return "unleash-client-java:$version"
    }

  fun unleashAPI(unleashAPI: URI): UnleashConfigBuilder {
    this.unleashAPI = unleashAPI
    return this
  }

  fun unleashAPI(unleashAPI: String): UnleashConfigBuilder {
    this.unleashAPI = URI.create(unleashAPI)
    return this
  }

  fun customHttpHeader(name: String, value: String): UnleashConfigBuilder {
    customHttpHeaders[name] = value
    return this
  }

  fun customHttpHeadersProvider(provider: CustomHttpHeadersProvider): UnleashConfigBuilder {
    customHttpHeadersProvider = provider
//    customHttpHeadersProvider = provider
    return this
  }

  fun appName(appName: String): UnleashConfigBuilder {
    this.appName = appName
    return this
  }

  fun environment(environment: String): UnleashConfigBuilder {
    this.environment = environment
    return this
  }

  fun instanceId(instanceId: String): UnleashConfigBuilder {
    this.instanceId = instanceId
    return this
  }

  fun fetchTogglesInterval(fetchTogglesInterval: Long): UnleashConfigBuilder {
    this.fetchTogglesInterval = fetchTogglesInterval
    return this
  }

  fun sendMetricsInterval(sendMetricsInterval: Long): UnleashConfigBuilder {
    this.sendMetricsInterval = sendMetricsInterval
    return this
  }

  fun disableMetrics(): UnleashConfigBuilder {
    disableMetrics = true
    return this
  }

  fun backupFile(backupFile: String): UnleashConfigBuilder {
    this.backupFile = backupFile
    return this
  }

  fun enableProxyAuthenticationByJvmProperties(): UnleashConfigBuilder {
    isProxyAuthenticationByJvmProperties = true
    return this
  }

  fun unleashContextProvider(contextProvider: UnleashContextProvider): UnleashConfigBuilder {
    this.contextProvider = contextProvider
    return this
  }

  fun synchronousFetchOnInitialisation(enable: Boolean): UnleashConfigBuilder {
    synchronousFetchOnInitialisation = enable
    return this
  }

  fun scheduledExecutor(scheduledExecutor: UnleashScheduledExecutor): UnleashConfigBuilder {
    this.scheduledExecutor = scheduledExecutor
    return this
  }

  fun subscriber(unleashSubscriber: UnleashSubscriber): UnleashConfigBuilder {
    this.unleashSubscriber = unleashSubscriber
    return this
  }

  private fun getBackupFile(): String {
    return if (backupFile != null) {
      backupFile as String
    } else {
      val fileName = "unleash-$appName-repo.json"
      System.getProperty("java.io.tmpdir") + File.separatorChar + fileName
    }
  }

  fun build(): UnleashConfig {
    return UnleashConfig(
      unleashAPI!!,
      customHttpHeaders,
      customHttpHeadersProvider,
      appName!!,
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
      scheduledExecutor ?: UnleashScheduledExecutorFactory.instance,
      unleashSubscriber ?: NoOpSubscriber()
    )
  }

  companion object {
    private const val TEN_SECONDS = 10L
    private const val SIXTY_SECONDS = 60L
  }
}
