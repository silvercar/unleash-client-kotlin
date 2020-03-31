package com.silvercar.unleash

import com.annimon.stream.Optional
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
    val environment: Optional<String?>
    val userId: Optional<String?>
    val sessionId: Optional<String?>
    val remoteAddress: Optional<String?>
    val properties: Map<String?, String?>

    constructor(
        userId: String?,
        sessionId: String?,
        remoteAddress: String?,
        properties: Map<String?, String?>
    ) : this(null, null, userId, sessionId, remoteAddress, properties) {
    }

    // TODO: This won't be needed anymore after converting this class's optionals to strings
    fun getByName(contextName: String?): Optional<String?> {
        return when (contextName) {
            "environment" -> environment
//            "appName" -> appName
            "userId" -> userId
            "sessionId" -> sessionId
            "remoteAddress" -> remoteAddress
            else -> Optional.ofNullable(
                properties[contextName]
            )
        }
    }

    fun applyStaticFields(config: UnleashConfig): UnleashContext {
        val builder =
            Builder(this)
        if (!environment.isPresent) {
            builder.environment(config.environment)
        }
        if(this.appName.isNullOrEmpty()) builder.appName(config.appName)
        return builder.build()
    }

    class Builder {
        private var appName: String? = null
        private var environment: String? = null
        private var userId: String? = null
        private var sessionId: String? = null
        private var remoteAddress: String? = null
        private val properties: MutableMap<String?, String?> = HashMap()

        constructor() // Need empty constructor for builder pattern
        constructor(context: UnleashContext) {
            appName = if (context.appName.isNullOrEmpty()) "" else context.appName
            context.environment.ifPresent { `val`: String? ->
                environment = `val`
            }
            context.userId.ifPresent { `val`: String? ->
                userId = `val`
            }
            context.sessionId.ifPresent { `val`: String? ->
                sessionId = `val`
            }
            context.remoteAddress.ifPresent { `val`: String? ->
                remoteAddress = `val`
            }

            context.properties.forEach { (key, value) -> this.properties[key] = value };
        }

        fun appName(appName: String?): Builder {
            this.appName = appName
            return this
        }

        fun environment(environment: String?): Builder {
            this.environment = environment
            return this
        }

        fun userId(userId: String?): Builder {
            this.userId = userId
            return this
        }

        fun sessionId(sessionId: String?): Builder {
            this.sessionId = sessionId
            return this
        }

        fun remoteAddress(remoteAddress: String?): Builder {
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
        this.environment = Optional.ofNullable(environment)
        this.userId = Optional.ofNullable(userId)
        this.sessionId = Optional.ofNullable(sessionId)
        this.remoteAddress = Optional.ofNullable(remoteAddress)
        this.properties = properties
    }
}
