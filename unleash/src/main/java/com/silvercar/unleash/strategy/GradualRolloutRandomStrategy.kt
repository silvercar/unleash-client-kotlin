package com.silvercar.unleash.strategy

import com.silvercar.unleash.strategy.Strategy.Companion.PERCENTAGE
import java.util.Random

class GradualRolloutRandomStrategy(private val random: Random) : Strategy {
  private val strategyUtils = StrategyUtils()

  override val name: String
    get() = STRATEGY_NAME

  override fun isEnabled(parameters: Map<String, String>): Boolean {
    val percentage = strategyUtils.getPercentage(parameters[PERCENTAGE])
    val randomNumber = random.nextInt(RANDOM_SEED) + RANDOM_SEED_INCREMENT
    return percentage >= randomNumber
  }

  companion object {
    private const val STRATEGY_NAME = "gradualRolloutRandom"
    private const val RANDOM_SEED = 100
    private const val RANDOM_SEED_INCREMENT = 1
  }
}
