package com.silvercar.unleash.strategy

class RandomGenerator {
  fun get(): String {
    return (Math.random() * OFFSET).toString()
  }

  companion object {
    private const val OFFSET = 100
  }
}
