package com.silvercar.unleash.metric

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

class ToggleCount {
  val variants = ConcurrentHashMap<String, AtomicLong?>()

  private val _yes = AtomicLong(0)
  val yes: Long
    get() = _yes.get()

  private val _no = AtomicLong(0)
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
