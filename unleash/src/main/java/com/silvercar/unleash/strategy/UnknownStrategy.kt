package com.silvercar.unleash.strategy

class UnknownStrategy : Strategy {
  override val name: String
    get() = STRATEGY_NAME

  override fun isEnabled(parameters: Map<String, String>): Boolean {
    return false
  }

  companion object {
    private const val STRATEGY_NAME = "unknown"
  }
}
