package com.silvercar.unleash.strategy

import com.silvercar.unleash.UnleashContext

interface Strategy {
  val name: String
  fun isEnabled(parameters: Map<String, String>): Boolean
  fun isEnabled(
    parameters: Map<String, String>,
    unleashContext: UnleashContext
  ): Boolean {
    return isEnabled(parameters)
  }

  companion object {
    const val ROLLOUT = "rollout"
    const val PERCENTAGE = "percentage"
    const val GROUP_ID = "groupId"
  }
}
