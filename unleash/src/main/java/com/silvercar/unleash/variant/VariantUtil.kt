package com.silvercar.unleash.variant

import com.silvercar.unleash.FeatureToggle
import com.silvercar.unleash.UnleashContext
import com.silvercar.unleash.Variant
import com.silvercar.unleash.strategy.StrategyUtils

class VariantUtil {
  @SuppressWarnings("ReturnCount")
  fun selectVariant(
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
    val target = StrategyUtils.getNormalizedNumber(
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
        context.userId2
      }
      "sessionId" -> {
        context.sessionId2
      }
      "remoteAddress" -> {
        context.remoteAddress2
      }
      else -> context.properties[contextName] ?: ""
    }

    return values.contains(contextValue)
  }

  private fun getIdentifier(context: UnleashContext): String {
    return when {
      context.userId2.isNotEmpty() -> {
        context.userId2
      }
      context.sessionId2.isNotEmpty() -> {
        context.sessionId2
      }
      context.remoteAddress2.isNotEmpty() -> {
        context.remoteAddress2
      }
      else -> {
        Math.random()
            .toString()
      }
    }
  }
}
