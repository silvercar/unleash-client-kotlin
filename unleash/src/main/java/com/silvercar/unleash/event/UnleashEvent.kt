package com.silvercar.unleash.event

interface UnleashEvent {
  fun publishTo(unleashSubscriber: UnleashSubscriber)
}
