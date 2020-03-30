package com.silvercar.sample

import com.silvercar.unleash.strategy.Strategy

class EnvironmentStrategy : Strategy {
  override fun isEnabled(parameters: MutableMap<String, String>?): Boolean {
    return "qa" == parameters?.get(PARAMETER) ?: ""
  }

  override fun getName(): String {
    return NAME
  }

  companion object {
    private const val NAME = "environment"
    private const val PARAMETER = "environment"
  }
}
