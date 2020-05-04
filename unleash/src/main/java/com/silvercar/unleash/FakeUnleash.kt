package com.silvercar.unleash

import com.silvercar.unleash.UnleashContext.Builder
import java.util.ArrayList

@Suppress("TooManyFunctions") class FakeUnleash : Unleash {
  private var enableAll = false
  private var disableAll = false
  private val features: MutableMap<String, Boolean> = mutableMapOf()
  private val variants: MutableMap<String, Variant> = mutableMapOf()

  override val featureToggleNames: List<String>
    get() = ArrayList(features.keys)

  override fun isEnabled(toggleName: String): Boolean {
    return isEnabled(toggleName, false)
  }

  override fun isEnabled(
    toggleName: String,
    defaultSetting: Boolean
  ): Boolean {
    return when {
      enableAll -> true
      disableAll -> false
      else -> features[toggleName] ?: defaultSetting
    }
  }

  override fun isEnabled(
    toggleName: String,
    context: UnleashContext,
    fallbackAction: (String, UnleashContext) -> Boolean
  ): Boolean {
    return isEnabled(toggleName, fallbackAction)
  }

  override fun isEnabled(
    toggleName: String,
    fallbackAction: (String, UnleashContext) -> Boolean
  ): Boolean {
    return if (!features.containsKey(toggleName)) {
      fallbackAction.invoke(toggleName, Builder().build())
    } else isEnabled(toggleName)
  }

  override fun getVariant(
    toggleName: String,
    context: UnleashContext
  ): Variant {
    return getVariant(toggleName, Variant.DISABLED_VARIANT)
  }

  override fun getVariant(
    toggleName: String,
    context: UnleashContext,
    defaultValue: Variant
  ): Variant {
    return getVariant(toggleName, defaultValue)
  }

  override fun getVariant(toggleName: String): Variant {
    return getVariant(toggleName, Variant.DISABLED_VARIANT)
  }

  override fun getVariant(
    toggleName: String,
    defaultValue: Variant
  ): Variant {
    return if (isEnabled(toggleName) && variants.containsKey(toggleName)) {
      variants[toggleName] as Variant
    } else {
      defaultValue
    }
  }

  fun enableAll() {
    disableAll = false
    enableAll = true
    features.clear()
  }

  fun disableAll() {
    disableAll = true
    enableAll = false
    features.clear()
  }

  fun resetAll() {
    disableAll = false
    enableAll = false
    features.clear()
    variants.clear()
  }

  fun enable(vararg features: String) {
    for (name in features) {
      this.features[name] = true
    }
  }

  fun disable(vararg features: String) {
    for (name in features) {
      this.features[name] = false
    }
  }

  fun reset(vararg features: String) {
    for (name in features) {
      this.features.remove(name)
    }
  }

  fun setVariant(toggleName: String, variant: Variant) {
    variants[toggleName] = variant
  }
}
