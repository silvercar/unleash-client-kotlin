package com.silvercar.unleash.strategy

import com.silvercar.unleash.UnleashContext
import com.silvercar.unleash.util.IpAddressMatcher

class RemoteAddressStrategy : Strategy {
  override val name: String
    get() = STRATEGY_NAME

  override fun isEnabled(parameters: Map<String, String>): Boolean {
    return false
  }

  override fun isEnabled(
    parameters: Map<String, String>,
    unleashContext: UnleashContext
  ): Boolean {
    val ips = parameters[IPS_PARAM] ?: ""
    val ipAddresses = ",".toPattern().split(ips, -1)

    return ipAddresses.any {
      val buildIpAddressMatcher = buildIpAddressMatcher(it)

      unleashContext.remoteAddress.isNotEmpty()
          && (buildIpAddressMatcher?.matches(unleashContext.remoteAddress) ?: false)
    }
  }

  private fun buildIpAddressMatcher(ipAddress: String): IpAddressMatcher? {
    return try {
      IpAddressMatcher(ipAddress)
    } catch (exception: IllegalArgumentException) {
      null
    }
  }

  companion object {
    private const val STRATEGY_NAME = "remoteAddress"
    const val IPS_PARAM = "IPs"
  }
}
