package com.silvercar.unleash.strategy

import junit.framework.TestCase.assertEquals
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory

class StrategyUtilsTest {
  private val strategyUtils: StrategyUtils
    get() = StrategyUtils()

  @TestFactory
  fun `should be the same across node java and go clients`(): Collection<DynamicTest> {
    return listOf(
      dynamicTest("should match 73 to 123/gr1") {
        assertEquals(73, strategyUtils.getNormalizedNumber("123", "gr1"))
      },
      dynamicTest("should match 25 to 999/groupX") {
        assertEquals(25, strategyUtils.getNormalizedNumber("999", "groupX"))
      }
    )
  }
}
