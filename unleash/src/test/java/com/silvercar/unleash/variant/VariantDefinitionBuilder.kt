package com.silvercar.unleash.variant

data class VariantDefinitionBuilder(val name: String, val weight: Int) {
  fun build(): VariantDefinition {
    return VariantDefinition(name, weight, null, emptyList())
  }
}