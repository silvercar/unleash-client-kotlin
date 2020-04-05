package com.silvercar.unleash.metric

import com.silvercar.unleash.util.UnleashConfig
import com.silvercar.unleash.util.UnleashScheduledExecutor
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

class UnleashMetricService internal constructor(
  private val unleashConfig: UnleashConfig,
  private val unleashMetricsSender: UnleashMetricsSender,
  executor: UnleashScheduledExecutor
) {
  private val started: Date = Calendar.getInstance(TimeZone.getTimeZone("GMT")).time
  private val metricsInterval: Long = unleashConfig.sendMetricsInterval

  //mutable
  @Volatile private var currentMetricsBucket: MetricsBucket

  constructor(
    unleashConfig: UnleashConfig,
    executor: UnleashScheduledExecutor
  ) : this(unleashConfig, UnleashMetricsSender(unleashConfig), executor)

  init {
    currentMetricsBucket = MetricsBucket()

    executor.setInterval(sendMetrics(), metricsInterval, metricsInterval)
  }

  fun register(strategies: Set<String>) {
    val registration = ClientRegistration(unleashConfig, started, strategies)
    unleashMetricsSender.registerClient(registration)
  }

  fun count(
    toggleName: String,
    active: Boolean
  ) {
    currentMetricsBucket.registerCount(toggleName, active)
  }

  fun countVariant(
    toggleName: String,
    variantName: String
  ) {
    currentMetricsBucket.registerCount(toggleName, variantName)
  }

  private fun sendMetrics(): Runnable {
    return Runnable {
      val metricsBucket = currentMetricsBucket
      currentMetricsBucket = MetricsBucket()
      metricsBucket.end()
      val metrics = ClientMetrics(unleashConfig, metricsBucket)
      unleashMetricsSender.sendMetrics(metrics)
    }
  }
}
