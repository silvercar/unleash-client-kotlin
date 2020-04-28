package com.silvercar.unleash

@Suppress("TooManyFunctions") interface Unleash {
  val featureToggleNames: List<String>

  fun isEnabled(toggleName: String): Boolean
  fun isEnabled(toggleName: String, defaultSetting: Boolean): Boolean
  fun isEnabled(toggleName: String, context: UnleashContext): Boolean {
    return isEnabled(toggleName, false)
  }

  fun isEnabled(toggleName: String, context: UnleashContext, defaultSetting: Boolean): Boolean {
    return isEnabled(toggleName, defaultSetting)
  }

  fun isEnabled(toggleName: String, fallbackAction: (String, UnleashContext) -> Boolean): Boolean {
    return isEnabled(toggleName, false)
  }

  fun isEnabled(
    toggleName: String,
    context: UnleashContext,
    fallbackAction: (String, UnleashContext) -> Boolean
  ): Boolean {
    return isEnabled(toggleName, false)
  }

  fun getVariant(toggleName: String): Variant
  fun getVariant(toggleName: String, defaultValue: Variant): Variant
  fun getVariant(toggleName: String, context: UnleashContext): Variant
  fun getVariant(toggleName: String, context: UnleashContext, defaultValue: Variant): Variant
  fun shutdown() {}
}
