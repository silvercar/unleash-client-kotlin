package com.silvercar.unleash.repository

import com.silvercar.unleash.UnleashException

interface ToggleFetcher {
  @Throws(UnleashException::class) fun fetchToggles(): FeatureToggleResponse
}
