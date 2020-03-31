package com.silvercar.unleash

import com.silvercar.unleash.util.UnleashConfig
import java.util.*

class UnleashContext(
    appName: String?,
    environment: String?,
    userId: String?,
    sessionId: String?,
    remoteAddress: String?,
    properties: Map<String?, String?>
) {
    val appName: String?
    val environment: String?
    val userId: String?
    val sessionId: String?
    val remoteAddress: String?
    val properties: Map<String?, String?>

    constructor(
        userId: String?,
        sessionId: String?,
        remoteAddress: String?,
        properties: Map<String?, String?>
    ) : this(null, null, userId, sessionId, remoteAddress, properties)

    fun getByName(contextName: String?): String? = when (contextName) {
        "environment" -> environment
        "appName" -> appName
        "userId" -> userId
        "sessionId" -> sessionId
        "remoteAddress" -> remoteAddress
        else -> properties[contextName]
    }

    fun applyStaticFields(config: UnleashConfig): UnleashContext {
        val builder = Builder(this)
        if (this.environment.isNullOrEmpty()) builder.environment(config.environment)
        if (this.appName.isNullOrEmpty()) builder.appName(config.appName)
        return builder.build()
    }

    class Builder {
        private var appName: String? = ""
        private var environment: String? = ""
        private var userId: String? = ""
        private var sessionId: String? = ""
        private var remoteAddress: String? = ""
        private val properties: MutableMap<String?, String?> = HashMap()

        constructor() // Need empty constructor for builder pattern
        constructor(context: UnleashContext) {
            appName = if (context.appName.isNullOrBlank()) "" else context.appName
            environment = if (context.environment.isNullOrBlank()) "" else context.environment
            userId = if (context.userId.isNullOrBlank()) "" else context.userId
            sessionId = if (context.sessionId.isNullOrBlank()) "" else context.sessionId
            remoteAddress = if (context.remoteAddress.isNullOrBlank()) "" else context.remoteAddress

            context.properties.forEach { (key, value) -> this.properties[key] = value }
        }

        fun appName(appName: String?): Builder {
            this.appName = appName
            return this
        }

        fun environment(environment: String): Builder {
            this.environment = environment
            return this
        }

        fun userId(userId: String): Builder {
            this.userId = userId
            return this
        }

        fun sessionId(sessionId: String): Builder {
            this.sessionId = sessionId
            return this
        }

        fun remoteAddress(remoteAddress: String): Builder {
            this.remoteAddress = remoteAddress
            return this
        }

        fun addProperty(
            name: String?,
            value: String?
        ): Builder {
            properties[name] = value
            return this
        }

        fun build(): UnleashContext {
            return UnleashContext(
                appName,
                environment,
                userId,
                sessionId,
                remoteAddress,
                properties
            )
        }
    }

    companion object {
        @JvmStatic
        fun builder(): Builder = Builder()
    }

    init {
        this.appName = appName
        this.environment = environment
        this.userId = userId
        this.sessionId = sessionId
        this.remoteAddress = remoteAddress
        this.properties = properties
    }
}
