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
  // Json can return overrides as null so this ensures safe access
  fun getOverrides(): List<VariantOverride> {
    return overrides ?: emptyList()
  }
}
