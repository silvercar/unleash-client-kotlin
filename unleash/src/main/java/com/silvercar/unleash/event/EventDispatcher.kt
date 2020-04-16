package com.silvercar.unleash.event

import com.silvercar.unleash.util.UnleashConfig
import com.silvercar.unleash.util.UnleashScheduledExecutor

class EventDispatcher(unleashConfig: UnleashConfig) {
  private val unleashSubscriber: UnleashSubscriber = unleashConfig.subscriber
  private val unleashScheduledExecutor: UnleashScheduledExecutor = unleashConfig.scheduledExecutor

  fun dispatch(unleashEvent: UnleashEvent) {
    unleashScheduledExecutor.scheduleOnce(Runnable {
      unleashSubscriber.on(unleashEvent)
      unleashEvent.publishTo(unleashSubscriber)
    })
  }
}
