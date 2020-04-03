package com.silvercar.unleash.event

class ToggleEvaluated(
  val toggleName: String,
  val isEnabled: Boolean
) : UnleashEvent {

  override fun publishTo(unleashSubscriber: UnleashSubscriber) {
    unleashSubscriber.toggleEvaluated(this)
  }

  override fun toString(): String {
    return "ToggleEvaluated: $toggleName=$isEnabled"
  }
}
