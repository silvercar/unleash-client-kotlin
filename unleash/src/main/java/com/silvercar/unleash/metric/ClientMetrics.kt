package com.silvercar.unleash.metric

import com.silvercar.unleash.event.UnleashEvent
import com.silvercar.unleash.event.UnleashSubscriber
import com.silvercar.unleash.util.UnleashConfig

class ClientMetrics internal constructor(
  config: UnleashConfig,
  val bucket: MetricsBucket
) : UnleashEvent {
  val appName: String = config.appName
  val instanceId: String = config.instanceId

  override fun publishTo(unleashSubscriber: UnleashSubscriber) {
    unleashSubscriber.clientMetrics(this)
  }
}
