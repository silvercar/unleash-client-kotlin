package com.silvercar.unleash.strategy

import com.silvercar.unleash.UnleashContext

class UserWithIdStrategy : Strategy {
  override val name: String
    get() = STRATEGY_NAME

  override fun isEnabled(parameters: Map<String, String>): Boolean {
    return false
  }

  override fun isEnabled(
    parameters: Map<String, String>,
    unleashContext: UnleashContext
  ): Boolean {
    if (unleashContext.userId2.isEmpty()) {
      return false
    }

    val userIds: List<String> = (parameters[USER_IDS_PARAM] ?: "").split(",\\s?".toRegex())

    return userIds.contains(unleashContext.userId2)
  }

  companion object {
    private const val STRATEGY_NAME = "userWithId"
    const val USER_IDS_PARAM = "userIds"
  }
}
