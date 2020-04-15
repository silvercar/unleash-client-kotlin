package com.silvercar.unleash.strategy

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class RandomGeneratorTest {
  private val generator
      get() = RandomGenerator()

  @Test fun `should get random double as String`() {
    // Act
    val result = generator.get()

    // Assert
    Assertions.assertNotNull(result.toDoubleOrNull())
  }
}
