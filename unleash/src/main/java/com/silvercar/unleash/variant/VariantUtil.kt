package com.silvercar.unleash.variant

import com.silvercar.unleash.FeatureToggle
import com.silvercar.unleash.UnleashContext
import com.silvercar.unleash.Variant
import com.silvercar.unleash.strategy.StrategyUtils

class VariantUtil {
  private val strategyUtils = StrategyUtils()

  @SuppressWarnings("ReturnCount") fun selectVariant(
    featureToggle: FeatureToggle,
    context: UnleashContext,
    defaultVariant: Variant
  ): Variant {
    val variants = featureToggle.variants
    val totalWeight = variants.map { it.weight }
        .sum()
    if (totalWeight == 0) {
      return defaultVariant
    }
    val variantOverride = getOverride(variants, context)
    variantOverride?.let {
      return it.toVariant()
    }
    val target = strategyUtils.getNormalizedNumber(
        getIdentifier(context),
        featureToggle.name,
        totalWeight
    )
    var counter = 0
    for (definition in featureToggle.variants) {
      if (definition.weight != 0) {
        counter += definition.weight
        if (counter >= target) {
          return definition.toVariant()
        }
      }
    }
    //Should not happen
    return defaultVariant
  }

  private fun getOverride(
    variants: List<VariantDefinition>,
    context: UnleashContext
  ): VariantDefinition? {
    return variants.firstOrNull { variant ->
      variant.getOverrides()
          .any {
            overrideMatchesContext(it.contextName, it.values, context)
          }
    }
  }

  private fun overrideMatchesContext(
    contextName: String,
    values: List<String>,
    context: UnleashContext
  ): Boolean {
    val contextValue = when (contextName) {
      "userId" -> {
        context.userId
      }
      "sessionId" -> {
        context.sessionId
      }
      "remoteAddress" -> {
        context.remoteAddress
      }
      else -> context.properties[contextName] ?: ""
    }

    return values.contains(contextValue)
  }

  private fun getIdentifier(context: UnleashContext): String {
    return when {
      context.userId.isNotEmpty() -> {
        context.userId
      }
      context.sessionId.isNotEmpty() -> {
        context.sessionId
      }
      context.remoteAddress.isNotEmpty() -> {
        context.remoteAddress
      }
      else -> {
        Math.random()
            .toString()
      }
    }
  }
}
