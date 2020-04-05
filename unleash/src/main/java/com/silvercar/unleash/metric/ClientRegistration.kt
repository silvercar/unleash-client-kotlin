package com.silvercar.unleash.metric

import com.silvercar.unleash.event.UnleashEvent
import com.silvercar.unleash.event.UnleashSubscriber
import com.silvercar.unleash.util.UnleashConfig
import java.util.Date

class ClientRegistration internal constructor(
  config: UnleashConfig,
  val started: Date,
  val strategies: Set<String>
) : UnleashEvent {
  val appName: String = config.appName
  val instanceId: String = config.instanceId
  val sdkVersion: String = config.sdkVersion
  val interval: Long = config.sendMetricsInterval

  override fun publishTo(unleashSubscriber: UnleashSubscriber) {
    unleashSubscriber.clientRegistered(this)
  }
}
