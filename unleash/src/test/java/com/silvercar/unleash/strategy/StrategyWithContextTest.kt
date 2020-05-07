package com.silvercar.unleash.strategy

import com.silvercar.unleash.UnleashContext
import com.silvercar.unleash.UnleashContext.Companion.builder
import com.silvercar.unleash.strategy.StrategyWithContextTest.TestStrategy.Companion.USER_IDS_PARAM
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class StrategyWithContextTest {
  val strategy: TestStrategy
    get() = TestStrategy()

  @Nested
  @DisplayName("WhenContextStrategy")
  inner class WhenContextStrategy {
    private val params = mapOf(Pair(USER_IDS_PARAM, "123"))

    @Test fun `should be enabled for known user`() {
      // Arrange
      val context = builder().userId("123").build()

      // Act
      val result = strategy.isEnabled(params, context)

      // Assert
      Assertions.assertTrue(result)
    }

    @Test fun `should not enabled for unknown user`() {
      // Arrange
      val context = builder().userId("other").build()

      // Act
      val result = strategy.isEnabled(params, context)

      // Assert
      Assertions.assertFalse(result)
    }
  }

  class TestStrategy : Strategy {
    override val name: String
      get() = STRATEGY_NAME

    override fun isEnabled(parameters: Map<String, String>): Boolean {
      return false
    }

    override fun isEnabled(
      parameters: Map<String, String>,
      unleashContext: UnleashContext
    ): Boolean {
      if (unleashContext.userId.isEmpty()) {
        return false
      }

      val userIds: List<String> = (parameters[USER_IDS_PARAM] ?: "").split(",\\s?".toRegex())

      return userIds.contains(unleashContext.userId)
    }

    companion object {
      private const val STRATEGY_NAME = "test"
      const val USER_IDS_PARAM = "userIds"
    }
  }
}
