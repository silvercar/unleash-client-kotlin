package com.silvercar.sample

import com.silvercar.unleash.strategy.Strategy

class EnvironmentStrategy : Strategy {
  override val name: String
    get() = NAME

  override fun isEnabled(parameters: Map<String, String>): Boolean {
    return "qa" == parameters[PARAMETER] ?: ""
  }

  companion object {
    private const val NAME = "environment"
    private const val PARAMETER = "environment"
  }
}
