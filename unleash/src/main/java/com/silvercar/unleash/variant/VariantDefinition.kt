package com.silvercar.unleash.variant

import com.silvercar.unleash.Variant

fun VariantDefinition.toVariant() = Variant(
    name = name,
    payload = payload,
    isEnabled = true
)

data class VariantDefinition(
  val name: String,
  val weight: Int,
  val payload: Payload?,
  private val overrides: List<VariantOverride>?
) {
  // TODO: Investigate why a Unit Test fails when this is a property
  fun getOverrides(): List<VariantOverride> {
    return overrides ?: emptyList()
  }
}
