package com.silvercar.unleash.repository

import com.silvercar.unleash.FeatureToggle
import java.util.concurrent.ConcurrentHashMap

class ToggleCollection internal constructor(val features: List<FeatureToggle>) {
  val version = 1 // required for serialization
  @Transient private val cache: MutableMap<String, FeatureToggle>

  init {
    cache = ConcurrentHashMap()
    for (featureToggle in this.features) {
      cache[featureToggle.name] = featureToggle
    }
  }

  fun getToggle(name: String): FeatureToggle? {
    return cache[name]
  }
}
