package com.silvercar.unleash.metric

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

internal class ToggleCount {
    private val yes: AtomicLong = AtomicLong(0)
    private val no: AtomicLong = AtomicLong(0)
    private val variants: MutableMap<String, AtomicLong>

    fun register(active: Boolean) {
        if (active) {
            yes.incrementAndGet()
        } else {
            no.incrementAndGet()
        }
    }

    fun register(variantName: String) {
        val current = variants.getOrPut(variantName, ::AtomicLong)
        current.incrementAndGet()
    }

    fun getYes(): Long {
        return yes.get()
    }

    fun getNo(): Long {
        return no.get()
    }

    fun getVariants(): Map<String, Number?> {
        return variants
    }

    init {
        variants =
            ConcurrentHashMap()
    }
}
