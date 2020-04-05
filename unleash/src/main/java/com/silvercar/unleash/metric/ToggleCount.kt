package com.silvercar.unleash.metric

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.atomic.AtomicLong

class ToggleCount {
  private val _yes: AtomicLong = AtomicLong(0)
  private val _no: AtomicLong = AtomicLong(0)
  val variants: ConcurrentMap<String, AtomicLong?> = ConcurrentHashMap()

  val yes: Long
    get() = _yes.get()

  val no: Long
    get() = _no.get()

  fun register(active: Boolean) {
    if (active) {
      _yes.incrementAndGet()
    } else {
      _no.incrementAndGet()
    }
  }

  fun register(variantName: String) {
    if (variants[variantName] == null) {
      variants.putIfAbsent(variantName, AtomicLong())
    }
    variants[variantName]?.incrementAndGet()
  }
}
