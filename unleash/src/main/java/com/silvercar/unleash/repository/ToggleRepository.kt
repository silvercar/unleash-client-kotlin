package com.silvercar.unleash.repository

import com.silvercar.unleash.FeatureToggle

interface ToggleRepository {
  val featureNames: List<String>
  fun getToggle(name: String): FeatureToggle?
}
