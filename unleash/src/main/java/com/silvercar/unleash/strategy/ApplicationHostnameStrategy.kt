package com.silvercar.unleash.strategy

import java.net.InetAddress
import java.net.UnknownHostException

class ApplicationHostnameStrategy : Strategy {
  private val hostname: String

  override val name: String
    get() = STRATEGY_NAME

  init {
    hostname = resolveHostname()
  }

  private fun resolveHostname(): String {
    var hostname: String? = System.getProperty("hostname")
    if (hostname == null) {
      hostname = try {
        InetAddress.getLocalHost().hostName
      } catch (exception: UnknownHostException) {
        "undefined"
      }
    }
    return hostname as String
  }

  override fun isEnabled(parameters: Map<String, String>): Boolean {
    val hostNames: String = parameters[HOST_NAMES_PARAM] ?: ""
    val hosts: List<String> = hostNames.split(",\\s*".toRegex())

    return hosts.any {
      it.equals(hostname, ignoreCase = true)
    }
  }

  companion object {
    private const val STRATEGY_NAME = "applicationHostname"
    const val HOST_NAMES_PARAM = "hostNames"
  }
}
