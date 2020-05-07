package com.silvercar.unleash.strategy

import com.silvercar.unleash.UnleashContext.Companion.builder
import com.silvercar.unleash.strategy.UserWithIdStrategy.Companion.USER_IDS_PARAM
import com.silvercar.unleash.unleashContext
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class UserWithIdStrategyTest {
  private val strategy: UserWithIdStrategy
    get() = UserWithIdStrategy()

  @Test fun `should have expected strategy name`() {
    // Assert
    assertEquals(strategy.name, "userWithId")
  }

  @ParameterizedTest
  @MethodSource("userIdContext")
  fun `should match user id`(
    parameters: Pair<String, String>,
    userId: String,
    expected: Boolean
  ) {
    // Arrange
    val context = unleashContext { userId(userId) }.build()

    // Act
    val result = strategy.isEnabled(mapOf(parameters), context)

    // Assert
    assertEquals(result, expected)
  }

  @Test fun `should not match csv without space`() {
    // Arrange
    val parameters = mutableMapOf(Pair(USER_IDS_PARAM, "123,122,121"))
    val context = builder().userId("123").build()

    // Act
    val result = strategy.isEnabled(parameters, context)

    // Assert
    Assertions.assertTrue(result)
  }

  @Test fun `should not be enabled without id`() {
    // Arrange
    val parameters = mutableMapOf(Pair(USER_IDS_PARAM, "160118738, 1823311338"))

    // Act
    val result = strategy.isEnabled(parameters)

    // Assert
    Assertions.assertFalse(result)
  }

  companion object {
    @Suppress("unused") @JvmStatic fun userIdContext() = listOf(
      Arguments.of(Pair(USER_IDS_PARAM, "001"), "001", true),
      Arguments.of(Pair(USER_IDS_PARAM, "111, 112, 113"), "111", true),
      Arguments.of(Pair(USER_IDS_PARAM, "221, 222, 223"), "222", true),
      Arguments.of(Pair(USER_IDS_PARAM, "331, 332, 333"), "333", true),
      Arguments.of(Pair(
          USER_IDS_PARAM,
          "160118738, 1823311338, 1422637466, 2125981185, 298261117, 1829486714, 463568019, 271166598"
        ), "298261117", true
      ),
      Arguments.of(Pair(USER_IDS_PARAM, "123, 122, 121, 212"), "12", false),
      Arguments.of(
        Pair(
          USER_IDS_PARAM,
          "160118738, 1823311338, 1422637466, 2125981185, 298261117, 1829486714, 463568019, 271166598"
        ), "32667774", false
      )
    )
  }
}
