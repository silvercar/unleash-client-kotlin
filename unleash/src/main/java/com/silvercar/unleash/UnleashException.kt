package com.silvercar.unleash

import com.silvercar.unleash.event.UnleashEvent
import com.silvercar.unleash.event.UnleashSubscriber

class UnleashException(message: String?, cause: Throwable?) :
  RuntimeException(message, cause), UnleashEvent {
  override fun publishTo(unleashSubscriber: UnleashSubscriber) {
    unleashSubscriber.onError(this)
  }
}
