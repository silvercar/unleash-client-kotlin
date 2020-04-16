package com.silvercar.unleash.util

import java.net.InetAddress
import java.net.UnknownHostException
import kotlin.math.roundToLong

object DefaultInstanceIdFactory {
  @Suppress("MagicNumber") fun getInstance(): String {
    var hostName = ""
    try {
      hostName = InetAddress.getLocalHost().hostName + "-"
    } catch (ignored: UnknownHostException) {
    }
    return hostName + "generated-" + (Math.random() * 1_000_000.0).roundToLong()
  }
}
