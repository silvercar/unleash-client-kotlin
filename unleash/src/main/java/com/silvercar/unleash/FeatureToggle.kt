package com.silvercar.unleash

import com.google.gson.annotations.SerializedName
import com.silvercar.unleash.variant.VariantDefinition

data class FeatureToggle constructor(
  val name: String,
  @SerializedName("enabled") val isEnabled: Boolean,
  val strategies: List<ActivationStrategy>,
  val variants: List<VariantDefinition> = mutableListOf()
) {
  constructor(
    name: String,
    isEnabled: Boolean,
    strategies: List<ActivationStrategy>
  ) : this(name, isEnabled, strategies, mutableListOf())

  override fun toString(): String {
    return "FeatureToggle{name='$name', enabled=$isEnabled, strategies='$strategies', " +
        "variants='$variants'}"
  }
}
