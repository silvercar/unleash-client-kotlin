package com.silvercar.unleash.strategy

import com.sangupta.murmur.Murmur3

class StrategyUtils {
  /**
   * Takes to string inputs concat them, produce a hash and return a normalized value between 0 and 100;
   *
   * @param identifier identifier id
   * @param groupId group id
   * @return normalized number
   */
  fun getNormalizedNumber(identifier: String, groupId: String): Int {
    return getNormalizedNumber(identifier, groupId, ONE_HUNDRED)
  }

  fun getNormalizedNumber(identifier: String, groupId: String, normalizer: Int): Int {
    val value = "$groupId:$identifier".toByteArray()
    val hash = Murmur3.hash_x86_32(value, value.size, 0)
    return (hash % normalizer).toInt() + 1
  }

  /**
   * Takes a numeric string value and converts it to a integer between 0 and 100.
   *
   * returns 0 if the string is not numeric.
   *
   * @param percentage - A numeric string value
   * @return a integer between 0 and 100
   */
  fun getPercentage(percentage: String?): Int {
    return if (!percentage.isNullOrEmpty() && percentage.all { it.isDigit() }) {
      percentage.toInt()
    } else {
      0
    }
  }

  companion object {
    private const val ONE_HUNDRED = 100
  }
}
