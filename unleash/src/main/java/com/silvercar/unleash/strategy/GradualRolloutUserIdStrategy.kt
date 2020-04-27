package com.silvercar.unleash.strategy

import com.silvercar.unleash.UnleashContext
import com.silvercar.unleash.strategy.Strategy.Companion.GROUP_ID
import com.silvercar.unleash.strategy.Strategy.Companion.PERCENTAGE

/**
 * Implements a gradual roll-out strategy based on userId.
 *
 * Using this strategy you can target only logged in users and gradually expose your
 * feature to higher percentage of the logged in user.
 *
 * This strategy takes two parameters:
 * - percentage :  a number between 0 and 100. The percentage you want to enable the feature for.
 * - groupId :     a groupId used for rolling out the feature. By using the same groupId for different
 * toggles you can correlate the user experience across toggles.
 *
 */
class GradualRolloutUserIdStrategy : Strategy {
  private val strategyUtils = StrategyUtils()

  override val name: String
    get() = STRATEGY_NAME

  override fun isEnabled(parameters: Map<String, String>): Boolean {
    return false
  }

  override fun isEnabled(
    parameters: Map<String, String>,
    unleashContext: UnleashContext
  ): Boolean {
    val userId: String = unleashContext.userId

    if (userId.isEmpty()) {
      return false
    }

    val percentage: Int = strategyUtils.getPercentage(parameters[PERCENTAGE])
    val groupId: String = parameters[GROUP_ID] ?: ""
    val normalizedUserId: Int = strategyUtils.getNormalizedNumber(userId, groupId)

    return percentage > 0 && normalizedUserId <= percentage
  }

  companion object {
    private const val STRATEGY_NAME = "gradualRolloutUserId"
  }
}
