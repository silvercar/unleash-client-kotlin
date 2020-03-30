package com.silvercar.unleash.metric

import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId

internal class MetricsBucket {
    private val toggles: MutableMap<String, ToggleCount> = mutableMapOf()
    val start: LocalDateTime = LocalDateTime.now(ZoneId.of("UTC"))

    @Volatile
    var stop: LocalDateTime? = null
        private set

    fun registerCount(toggleName: String, active: Boolean) {
        getOrCreate(toggleName).register(active)
    }

    fun registerCount(toggleName: String, variantName: String) {
        getOrCreate(toggleName).register(variantName)
    }

    private fun getOrCreate(toggleName: String): ToggleCount {
        return toggles.getOrPut(toggleName, ::ToggleCount)
    }

    fun end() {
        stop = LocalDateTime.now(ZoneId.of("UTC"))
    }

    fun getToggles(): Map<String, ToggleCount> {
        return toggles
    }
}
