package com.silvercar.unleash.metric

import java.util.Calendar
import java.util.Date
import java.util.TimeZone
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

class MetricsBucket internal constructor() {
  @Transient private val calendar: Calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"))
  val toggles = ConcurrentHashMap<String, ToggleCount>()
  val start: Date

  @Volatile var stop: Date? = null
    private set

  init {
    start = calendar.time
  }

  fun registerCount(
    toggleName: String,
    active: Boolean
  ) {
    getOrCreate(toggleName)?.register(active)
  }

  private fun getOrCreate(toggleName: String): ToggleCount? {
    if (toggles[toggleName] == null) {
      toggles.putIfAbsent(toggleName, ToggleCount())
    }
    return toggles[toggleName]
  }

  fun registerCount(
    toggleName: String,
    variantName: String?
  ) {
    getOrCreate(toggleName)?.register(variantName!!)
  }

  fun end() {
    stop = calendar.time
  }
}
