package com.silvercar.unleash.metric

import com.google.gson.GsonBuilder
import com.silvercar.unleash.UnleashException
import com.silvercar.unleash.event.EventDispatcher
import com.silvercar.unleash.util.UnleashConfig
import java.io.IOException
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.Date
import java.util.concurrent.atomic.AtomicLong

class UnleashMetricsSender(private val unleashConfig: UnleashConfig) {
  private val gson = GsonBuilder()
    .registerTypeAdapter(Date::class.java, DateSerializer())
    .registerTypeAdapter(AtomicLong::class.java, AtomicLongSerializer())
    .create()
  private val eventDispatcher = EventDispatcher(unleashConfig)
  private val clientRegistrationURL: URL = unleashConfig.unleashURLs.clientRegisterURL
  private val clientMetricsURL: URL = unleashConfig.unleashURLs.clientMetricsURL

  fun registerClient(registration: ClientRegistration) {
    if (unleashConfig.isDisableMetrics) {
      return
    }

    try {
      post(clientRegistrationURL, registration)
      eventDispatcher.dispatch(registration)
    } catch (ex: UnleashException) {
      eventDispatcher.dispatch(ex)
    }
  }

  fun sendMetrics(metrics: ClientMetrics) {
    if (unleashConfig.isDisableMetrics) {
      return
    }

    try {
      post(clientMetricsURL, metrics)
      eventDispatcher.dispatch(metrics)
    } catch (ex: UnleashException) {
      eventDispatcher.dispatch(ex)
    }
  }

  @Throws(UnleashException::class) private fun post(
    url: URL,
    body: Any
  ) {
    var connection: HttpURLConnection? = null
    try {
      connection = url.openConnection() as HttpURLConnection
      connection.connectTimeout = CONNECT_TIMEOUT
      connection.readTimeout = CONNECT_TIMEOUT
      connection.requestMethod = "POST"
      connection.setRequestProperty("Accept", "application/json")
      connection.setRequestProperty("Content-Type", "application/json")
      unleashConfig.setRequestProperties(connection)
      connection.useCaches = false
      connection.doInput = true
      connection.doOutput = true
      val writer = OutputStreamWriter(connection.outputStream)
      gson.toJson(body, writer)
      writer.flush()
      writer.close()
      connection.connect()
      // TODO should probably check response code to detect errors?
      connection.responseCode
    } catch (e: IOException) {
      throw UnleashException("Could not post to Unleash API", e)
    } catch (e: IllegalStateException) {
      throw UnleashException(e.message, e)
    } finally {
      connection?.disconnect()
    }
  }

  companion object {
    private const val CONNECT_TIMEOUT = 1000
  }
}
