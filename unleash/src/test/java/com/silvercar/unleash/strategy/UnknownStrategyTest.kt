package com.silvercar.unleash.strategy

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class UnknownStrategyTest {
  private val strategy: UnknownStrategy
    get() = UnknownStrategy()

  @Test fun `should have expected strategy name`() {
    // Assert
    assertEquals(strategy.name, "unknown")
  }

  @Test fun `should not be enabled `() {
    // Act
    val result = strategy.isEnabled(mapOf())

    // Assert
    Assertions.assertFalse(result)
  }
}
