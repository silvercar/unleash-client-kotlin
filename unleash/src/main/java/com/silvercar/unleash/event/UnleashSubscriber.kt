package com.silvercar.unleash.event

import com.silvercar.unleash.UnleashException
import com.silvercar.unleash.metric.ClientMetrics
import com.silvercar.unleash.metric.ClientRegistration
import com.silvercar.unleash.repository.FeatureToggleResponse
import com.silvercar.unleash.repository.ToggleCollection
import org.apache.logging.log4j.LogManager

interface UnleashSubscriber {
  fun onError(exception: UnleashException) {
    LogManager.getLogger(UnleashSubscriber::class.java)
        .warn(exception.message, exception)
  }

  fun on(event: UnleashEvent) {}
  fun onReady(ready: UnleashReady) {}
  fun toggleEvaluated(evaluated: ToggleEvaluated) {}
  fun togglesFetched(response: FeatureToggleResponse) {}
  fun clientMetrics(metrics: ClientMetrics) {}
  fun clientRegistered(clientRegistration: ClientRegistration) {}
  fun togglesBackedUp(toggleCollection: ToggleCollection) {}
  fun toggleBackupRestored(toggleCollection: ToggleCollection) {}
}
