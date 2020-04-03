package com.silvercar.unleash.event

class UnleashReady : UnleashEvent {
  override fun publishTo(unleashSubscriber: UnleashSubscriber) {
    unleashSubscriber.onReady(this)
  }
}
