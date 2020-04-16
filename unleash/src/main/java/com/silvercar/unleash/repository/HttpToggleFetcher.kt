package com.silvercar.unleash.repository

import com.silvercar.unleash.UnleashException
import com.silvercar.unleash.repository.FeatureToggleResponse.Status.CHANGED
import com.silvercar.unleash.repository.FeatureToggleResponse.Status.NOT_CHANGED
import com.silvercar.unleash.repository.FeatureToggleResponse.Status.UNAVAILABLE
import com.silvercar.unleash.util.UnleashConfig
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.text.Charsets.UTF_8

class HttpToggleFetcher(private val unleashConfig: UnleashConfig) : ToggleFetcher {
  private var eTag = ""
  private val toggleUrl: URL = unleashConfig.unleashURLs.fetchTogglesURL
  private val jsonToggleParser = JsonToggleParser()

  @Throws(UnleashException::class)
  override fun fetchToggles(): FeatureToggleResponse {
    var connection: HttpURLConnection? = null
    return try {
      connection = toggleUrl.openConnection() as HttpURLConnection
      connection.connectTimeout = CONNECT_TIMEOUT
      connection.readTimeout = CONNECT_TIMEOUT
      connection.setRequestProperty("Accept", "application/json")
      connection.setRequestProperty("Content-Type", "application/json")

      unleashConfig.setRequestProperties(connection)

      if (eTag.isNotEmpty()) {
        connection.setRequestProperty("If-None-Match", eTag)
      }

      connection.useCaches = true
      connection.connect()

      val responseCode = connection.responseCode
      when {
        responseCode < HTTP_STATUS_MULTIPLE_CHOICES -> {
          getToggleResponse(connection)
        }
        responseCode == HTTP_STATUS_NOT_MODIFIED -> {
          FeatureToggleResponse(NOT_CHANGED, responseCode)
        }
        else -> {
          FeatureToggleResponse(UNAVAILABLE, responseCode, location = getLocationHeader(connection))
        }
      }
    } catch (e: IOException) {
      throw UnleashException("Could not fetch toggles", e)
    } catch (e: IllegalStateException) {
      throw UnleashException(e.message, e)
    } finally {
      connection?.disconnect()
    }
  }

  @Throws(IOException::class)
  private fun getToggleResponse(request: HttpURLConnection): FeatureToggleResponse {
    eTag = request.getHeaderField("ETag") ?: ""

    BufferedReader(InputStreamReader(request.content as InputStream, UTF_8)).use { reader ->
      val toggles = jsonToggleParser.fromJson(reader)
      return FeatureToggleResponse(CHANGED, toggleCollection = toggles)
    }
  }

  private fun getLocationHeader(connection: HttpURLConnection): String? {
    return connection.getHeaderField("Location")
  }

  companion object {
    private const val CONNECT_TIMEOUT = 10000
    private const val HTTP_STATUS_MULTIPLE_CHOICES = 300
    private const val HTTP_STATUS_NOT_MODIFIED = 304
  }
}
