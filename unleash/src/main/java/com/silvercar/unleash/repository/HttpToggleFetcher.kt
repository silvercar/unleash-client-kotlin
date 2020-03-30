package com.silvercar.unleash.repository

import com.silvercar.unleash.UnleashException
import com.silvercar.unleash.repository.FeatureToggleResponse
import com.silvercar.unleash.util.UnleashConfig
import com.silvercar.unleash.util.UnleashConfig.Companion.setRequestProperties
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

class HttpToggleFetcher(private val unleashConfig: UnleashConfig) : ToggleFetcher {
    private var etag: String? = ""
    private val toggleUrl: URL = unleashConfig.unleashURLs.fetchTogglesURL

    @Throws(UnleashException::class)
    override fun fetchToggles(): FeatureToggleResponse {
        var connection: HttpURLConnection? = null
        return try {
            connection = toggleUrl.openConnection() as HttpURLConnection
            connection.connectTimeout = CONNECT_TIMEOUT
            connection.readTimeout = CONNECT_TIMEOUT
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("Content-Type", "application/json")
            setRequestProperties(connection, unleashConfig)
            if (!etag.isNullOrEmpty()) {
                connection.setRequestProperty("If-None-Match", etag)
            }
            connection.useCaches = true
            connection.connect()
            val responseCode = connection.responseCode
            when {
                responseCode < 300 -> getToggleResponse(connection)
                responseCode == 304 -> FeatureToggleResponse(
                    FeatureToggleResponse.Status.NOT_CHANGED,
                    responseCode
                )
                else -> {
                    FeatureToggleResponse(
                        FeatureToggleResponse.Status.UNAVAILABLE,
                        responseCode,
                        getLocationHeader(connection)
                    )
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
        etag = request.getHeaderField("ETag") ?: ""
        BufferedReader(
            InputStreamReader(
                request.content as InputStream,
                StandardCharsets.UTF_8
            )
        ).use { reader ->
            val toggles = JsonToggleParser.fromJson(reader)
            return FeatureToggleResponse(FeatureToggleResponse.Status.CHANGED, toggles)
        }
    }

    private fun getLocationHeader(connection: HttpURLConnection?): String? {
        return connection?.getHeaderField("Location")
    }

    companion object {
        private const val CONNECT_TIMEOUT = 10000
    }
}
