/*
 * Copyright 2002-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.silvercar.unleash.util

import java.net.InetAddress
import java.net.UnknownHostException
import java.util.Arrays
import kotlin.experimental.and

/**
 * Matches a request based on IP Address or subnet mask matching against the remote
 * address.
 *
 *
 * Both IPv6 and IPv4 addresses are supported, but a matcher which is configured with an
 * IPv4 address will never match a request which returns an IPv6 address, and vice-versa.
 *
 * @author Luke Taylor
 *
 * Takes a specific IP address or a range specified using the IP/Netmask (e.g.
 * 192.168.1.0/24 or 202.24.0.0/14).
 *
 * @param ipAddress the address or range of addresses from which the request must
 * come.
 */
class IpAddressMatcher(ipAddress: String?) {
  private var maskBits = 0
  private val requiredAddress: InetAddress?

  init {
    val trimmedIpAddress = ipAddress?.trim() ?: ""

    if (trimmedIpAddress.indexOf('/') > 0) {
      val addressAndMask = "/".toPattern().split(trimmedIpAddress, -1)
      requiredAddress = parseAddress(addressAndMask[0])
      maskBits = addressAndMask[1].toInt()
    } else {
      requiredAddress = parseAddress(trimmedIpAddress)
      maskBits = -1
    }
  }

  @Suppress("ComplexMethod", "MagicNumber", "ReturnCount")
  fun matches(address: String?): Boolean {
    if (address.isNullOrEmpty() || requiredAddress == null) {
      return false
    }
    val remoteAddress = parseAddress(address)

    if (requiredAddress.javaClass != remoteAddress?.javaClass) {
      return false
    }

    if (maskBits < 0) {
      return remoteAddress == requiredAddress
    }

    val oddBits = maskBits % 8
    val maskBytes = maskBits / 8 + if (oddBits == 0) 0 else 1
    val mask = ByteArray(maskBytes)

    Arrays.fill(mask, 0, if (oddBits == 0) mask.size else mask.size - 1, 0xFF.toByte())

    if (oddBits != 0) {
      var finalByte = (1 shl oddBits) - 1
      finalByte = finalByte shl 8 - oddBits
      mask[mask.size - 1] = finalByte.toByte()
    }

    for (i in mask.indices) {
      if (remoteAddress.address[i] and mask[i] != requiredAddress.address[i] and mask[i]) {
        return false
      }
    }
    return true
  }

  private fun parseAddress(address: String?): InetAddress? {
    return if (address.isNullOrEmpty()) {
      null
    } else try {
      InetAddress.getByName(address)
    } catch (e: UnknownHostException) {
      throw IllegalArgumentException("Failed to parse address $address", e)
    }
  }
}
