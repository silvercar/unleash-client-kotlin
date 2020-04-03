package com.silvercar.unleash.event

import com.silvercar.unleash.UnleashException
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager

class Log4JSubscriber : UnleashSubscriber {
  private var eventLevel = Level.INFO
  private var errorLevel = Level.WARN

  override fun on(event: UnleashEvent) {
    LOG.log(eventLevel, event.toString())
  }

  override fun onError(exception: UnleashException) {
    LOG.log(errorLevel, exception.message, exception)
  }

  fun setEventLevel(eventLevel: Level): Log4JSubscriber {
    this.eventLevel = eventLevel
    return this
  }

  fun setErrorLevel(errorLevel: Level): Log4JSubscriber {
    this.errorLevel = errorLevel
    return this
  }

  companion object {
    private val LOG = LogManager.getLogger(Log4JSubscriber::class.java)
  }
}
