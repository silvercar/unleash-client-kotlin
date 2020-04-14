package com.silvercar.unleash.repository

import com.silvercar.unleash.FeatureToggle
import com.silvercar.unleash.UnleashException
import com.silvercar.unleash.event.EventDispatcher
import com.silvercar.unleash.event.UnleashReady
import com.silvercar.unleash.repository.FeatureToggleResponse.Status.CHANGED
import com.silvercar.unleash.util.UnleashConfig

class FeatureToggleRepository(
  unleashConfig: UnleashConfig,
  private val toggleFetcher: ToggleFetcher,
  private val toggleBackupHandler: ToggleBackupHandler
) : ToggleRepository {
  private val eventDispatcher = EventDispatcher(unleashConfig)
  private var toggleCollection: ToggleCollection
  private var ready = false

  init {
    toggleCollection = toggleBackupHandler.read()
    if (unleashConfig.isSynchronousFetchOnInitialisation) {
      updateToggles().run()
    }
    unleashConfig.scheduledExecutor
      .setInterval(updateToggles(), 0, unleashConfig.fetchTogglesInterval)
  }

  private fun updateToggles(): Runnable {
    return Runnable {
      try {
        val response = toggleFetcher.fetchToggles()
        eventDispatcher.dispatch(response)
        if (response.status == CHANGED) {
          toggleCollection = response.toggleCollection
          toggleBackupHandler.write(toggleCollection)
        }
        if (!ready) {
          eventDispatcher.dispatch(UnleashReady())
          ready = true
        }
      } catch (exception: UnleashException) {
        eventDispatcher.dispatch(exception)
      }
    }
  }

  override val featureNames: List<String>
    get() {
      return toggleCollection.features.map { it.name }.toList()
    }

  override fun getToggle(name: String): FeatureToggle? {
    return toggleCollection.getToggle(name)
  }
}
