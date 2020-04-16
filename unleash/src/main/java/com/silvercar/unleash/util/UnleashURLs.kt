package com.silvercar.unleash.util

import java.net.MalformedURLException
import java.net.URI
import java.net.URL

class UnleashURLs(unleashAPI: URI) {
  val fetchTogglesURL: URL
  val clientMetricsURL: URL
  val clientRegisterURL: URL

  init {
    try {
      val unleashAPIUrls = unleashAPI.toString()
      fetchTogglesURL = URI.create("$unleashAPIUrls/client/features").normalize().toURL()
      clientMetricsURL = URI.create("$unleashAPIUrls/client/metrics").normalize().toURL()
      clientRegisterURL = URI.create("$unleashAPIUrls/client/register").normalize().toURL()
    } catch (ex: MalformedURLException) {
      throw IllegalArgumentException("Unleash API is not a valid URL: $unleashAPI")
    }
  }
}
