package com.silvercar.unleash.strategy

import com.silvercar.unleash.Constraint
import com.silvercar.unleash.Operator.IN
import com.silvercar.unleash.Operator.NOT_IN
import com.silvercar.unleash.UnleashContext
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ConstraintUtilTest {
  private val util: ConstraintUtil
    get() = ConstraintUtil()

  @Nested
  @DisplayName("When default Unleash context")
  inner class DefaultUnleashContext {
    private val context = UnleashContext.builder().build()

    @Test fun `should be valid for empty constraints`() {
      // Arrange
      val constraints = listOf<Constraint>()

      // Act
      val result = util.validate(constraints, context)

      // Assert
      assertTrue(result)
    }

    @Test fun `should be enabled for null constraints`() {
      // Arrange
      val constraints: List<Constraint>? = null

      // Act
      val result = util.validate(constraints, context)

      // Assert
      assertTrue(result)
    }
  }

  @Nested
  @DisplayName("When test Unleash context")
  inner class TestUnleashContext {
    private val context = UnleashContext.builder().environment("test").build()

    @Test fun `should be disabled when constraint not satisfied`() {
      // Arrange
      val constraints = listOf(Constraint("environment", IN, listOf("prod")))

      // Act
      val result = util.validate(constraints, context)

      // Assert
      assertFalse(result)
    }

    @Test fun should_be_enabled_when_constraint_is_satisfied() {
      // Arrange
      val constraints = listOf(Constraint("environment", IN, listOf("test", "prod")))

      // Act
      val result = util.validate(constraints, context)

      // Assert
      assertTrue(result)
    }

    @Test
    fun should_be_enabled_when_constraint_NOT_IN_satisfied() {
      // Arrange
      val constraints = listOf(Constraint("environment", NOT_IN, listOf("prod")))

      // Act
      val result = util.validate(constraints, context)

      // Assert
      assertTrue(result)
    }
  }

  @Nested
  @DisplayName("When test Unleash context with unique properties")
  inner class TestUniquePropertiesUnleashContext {
    private val context = UnleashContext.builder()
      .environment("test")
      .userId("123")
      .addProperty("customerId", "blue")
      .build()

    @Test fun `should be enabled when all constraints are satisfied`() {
      val constraints = listOf(
        Constraint("environment", IN, listOf("test", "prod")),
        Constraint("userId", IN, listOf("123")),
        Constraint("customerId", IN, listOf("red", "blue")
        )
      )

      // Act
      val result = util.validate(constraints, context)

      // Assert
      assertTrue(result)
    }

    @Test
    fun should_be_disabled_when_not_all_constraints_are_satisfied() {
      val constraints = listOf(
        Constraint("environment", IN, listOf("test", "prod")),
        Constraint("userId", IN, listOf("123")),
        Constraint("customerId", IN, listOf("red", "orange")
        )
      )

      // Act
      val result = util.validate(constraints, context)

      // Assert
      assertFalse(result)
    }
  }
}
