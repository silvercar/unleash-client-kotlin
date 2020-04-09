package com.silvercar.unleash.repository

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class GsonFactoryTest {
  private val factory: GsonFactory
    get() = GsonFactory

  @Test fun `should reuse Gson instance`() {
    // Act
    val result = factory.getInstance()

    // Assert
    Assertions.assertEquals(result, factory.getInstance())
  }
}
