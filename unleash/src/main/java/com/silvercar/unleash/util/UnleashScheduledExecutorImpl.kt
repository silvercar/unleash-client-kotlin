package com.silvercar.unleash.util

import org.apache.logging.log4j.LogManager
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit.SECONDS

object UnleashScheduledExecutorFactory {
  private lateinit var INSTANCE: UnleashScheduledExecutorImpl

  @JvmStatic @get:Synchronized val instance: UnleashScheduledExecutorImpl
    get() {
      if (!this::INSTANCE.isInitialized) {
        INSTANCE = UnleashScheduledExecutorImpl()
      }
      return INSTANCE
    }
}

class UnleashScheduledExecutorImpl : UnleashScheduledExecutor {
  private val scheduledThreadPoolExecutor: ScheduledThreadPoolExecutor
  private val executorService: ExecutorService

  init {
    val threadFactory =
      ThreadFactory { runnable: Runnable ->
        val thread = Executors.defaultThreadFactory().newThread(runnable)
        thread.name = "unleash-api-executor"
        thread.isDaemon = true
        thread
      }
    scheduledThreadPoolExecutor = ScheduledThreadPoolExecutor(1, threadFactory)
    scheduledThreadPoolExecutor.removeOnCancelPolicy = true
    executorService = Executors.newSingleThreadExecutor(threadFactory)
  }

  override fun setInterval(
    command: Runnable,
    initialDelay: Long,
    period: Long
  ): ScheduledFuture<*>? {
    return try {
      scheduledThreadPoolExecutor.scheduleAtFixedRate(command, initialDelay, period, SECONDS)
    } catch (ex: RejectedExecutionException) {
      LOG.error("Unleash background task crashed", ex)
      null
    }
  }

  override fun scheduleOnce(runnable: Runnable): Future<Void?> {
    return executorService.submit(runnable) as Future<Void?>
  }

  override fun shutdown() {
    scheduledThreadPoolExecutor.shutdown()
  }

  companion object {
    private val LOG = LogManager.getLogger(UnleashScheduledExecutorImpl::class.java)
  }
}
