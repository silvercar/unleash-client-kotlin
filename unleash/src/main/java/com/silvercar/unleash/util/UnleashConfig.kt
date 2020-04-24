package com.silvercar.unleash.util

import com.silvercar.unleash.CustomHttpHeadersProvider
import com.silvercar.unleash.UnleashContextProvider
import com.silvercar.unleash.event.UnleashSubscriber
import java.net.Authenticator
import java.net.HttpURLConnection
import java.net.URI

@Suppress("LongParameterList") class UnleashConfig(
  val unleashAPI: URI,
  private val customHttpHeaders: Map<String, String>,
  private val customHttpHeadersProvider: CustomHttpHeadersProvider,
  val appName: String,
  val environment: String,
  val instanceId: String,
  val sdkVersion: String,
  val backupFile: String,
  val fetchTogglesInterval: Long,
  val sendMetricsInterval: Long,
  val isDisableMetrics: Boolean,
  val contextProvider: UnleashContextProvider,
  val isProxyAuthenticationByJvmProperties: Boolean,
  val isSynchronousFetchOnInitialisation: Boolean,
  val scheduledExecutor: UnleashScheduledExecutor,
  val subscriber: UnleashSubscriber
) {
  val unleashURLs: UnleashURLs

  init {
    if (isProxyAuthenticationByJvmProperties) {
      enableProxyAuthentication()
    }
    unleashURLs = UnleashURLs(unleashAPI)
  }

  fun setRequestProperties(connection: HttpURLConnection) {
    connection.setRequestProperty(UNLEASH_APP_NAME_HEADER, appName)
    connection.setRequestProperty(UNLEASH_INSTANCE_ID_HEADER, instanceId)
    connection.setRequestProperty("User-Agent", appName)
    for ((key, value) in customHttpHeaders) {
      connection.setRequestProperty(key, value)
    }
    for ((key, value) in customHttpHeadersProvider.getCustomHeaders()) {
      connection.setRequestProperty(key, value)
    }
  }

  private fun enableProxyAuthentication() {
    // http.proxyUser http.proxyPassword is only consumed by Apache HTTP Client,
    // for HttpUrlConnection we have to define an Authenticator
    Authenticator.setDefault(ProxyAuthenticator())
  }

  companion object {
    const val UNLEASH_APP_NAME_HEADER = "UNLEASH-APPNAME"
    const val UNLEASH_INSTANCE_ID_HEADER = "UNLEASH-INSTANCEID"
  }
}
