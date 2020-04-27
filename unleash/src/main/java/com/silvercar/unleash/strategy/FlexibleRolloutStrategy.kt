package com.silvercar.unleash.strategy

import com.silvercar.unleash.UnleashContext
import com.silvercar.unleash.strategy.Strategy.Companion.GROUP_ID
import com.silvercar.unleash.strategy.Strategy.Companion.ROLLOUT

class FlexibleRolloutStrategy(private val randomGenerator: RandomGenerator) :
  Strategy {
  private val strategyUtils: StrategyUtils = StrategyUtils()

  override val name: String
    get() = STRATEGY_NAME

  override fun isEnabled(parameters: Map<String, String>): Boolean {
    return false
  }

  private fun resolveStickiness(stickiness: String, context: UnleashContext): String {
    return when (stickiness) {
      "userId" -> context.userId
      "sessionId" -> context.sessionId
      "random" -> randomGenerator.get()
      else -> if (context.userId.isNotEmpty()) {
        context.userId
      } else if (context.sessionId.isNotEmpty()) {
        context.sessionId
      } else {
        randomGenerator.get()
      }
    }
  }

  override fun isEnabled(
    parameters: Map<String, String>,
    unleashContext: UnleashContext
  ): Boolean {
    val stickiness = getStickiness(parameters)
    val stickinessId = resolveStickiness(stickiness, unleashContext)
    val percentage = strategyUtils.getPercentage(parameters[ROLLOUT])
    val groupId = parameters[GROUP_ID] ?: ":"

    return if (stickinessId.isNotEmpty()) {
      val normalizedUserId = strategyUtils.getNormalizedNumber(stickinessId, groupId)
      percentage > 0 && normalizedUserId <= percentage
    } else {
      false
    }
  }

  private fun getStickiness(parameters: Map<String, String>): String {
    val stickiness = parameters[STICKINESS] ?: ""
    return if (stickiness.isEmpty()) "default" else stickiness
  }

  companion object {
    private const val STRATEGY_NAME = "flexibleRollout"
    const val STICKINESS = "stickiness"
  }
}
