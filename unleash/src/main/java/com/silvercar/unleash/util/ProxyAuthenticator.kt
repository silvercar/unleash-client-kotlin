package com.silvercar.unleash.util

import java.net.Authenticator
import java.net.Authenticator.RequestorType.PROXY
import java.net.PasswordAuthentication
import java.util.Locale

internal class ProxyAuthenticator : Authenticator() {
  public override fun getPasswordAuthentication(): PasswordAuthentication? {
    if (requestorType == PROXY) {
      val proto = requestingProtocol.toLowerCase(Locale.US)
      val proxyHost = System.getProperty("$proto.proxyHost", "")
      val proxyPort = System.getProperty("$proto.proxyPort", "")
      val proxyUser = System.getProperty("$proto.proxyUser", "")
      val proxyPassword = System.getProperty("$proto.proxyPassword", "")

      // Only apply PasswordAuthentication to requests to the proxy itself - if not set just ignore
      if (requestingHost.equals(proxyHost, ignoreCase = true)
        && proxyPort.toInt() == requestingPort
      ) {
        return PasswordAuthentication(proxyUser, proxyPassword.toCharArray())
      }
    }
    return null
  }
}
