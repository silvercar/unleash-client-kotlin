package com.silvercar.unleash.util

import java.util.concurrent.Future
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.ScheduledFuture

interface UnleashScheduledExecutor {
  @Throws(RejectedExecutionException::class) fun setInterval(
    command: Runnable,
    initialDelay: Long,
    period: Long
  ): ScheduledFuture<*>?

  fun scheduleOnce(runnable: Runnable): Future<Void?>
  fun shutdown() {}
}
