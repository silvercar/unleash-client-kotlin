package com.silvercar.unleash.strategy

class DefaultStrategy : Strategy {
  override val name: String
    get() = STRATEGY_NAME

  override fun isEnabled(parameters: Map<String, String>): Boolean {
    return true
  }

  companion object {
    private const val STRATEGY_NAME = "default"
  }
}
