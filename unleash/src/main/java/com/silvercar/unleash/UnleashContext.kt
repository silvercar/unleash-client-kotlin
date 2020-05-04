package com.silvercar.unleash

import com.silvercar.unleash.UnleashContext.Builder
import com.silvercar.unleash.util.UnleashConfig

fun unleashContext(func: Builder.() -> Unit) = Builder().apply(func)

@Suppress("LongParameterList") class UnleashContext(
  val appName: String?,
  val environment: String?,
  val userId: String = "",
  val sessionId: String = "",
  val remoteAddress: String = "",
  val properties: Map<String, String> = mapOf()
) {

  constructor(
    userId: String,
    sessionId: String,
    remoteAddress: String,
    properties: Map<String, String>
  ) : this(null, null, userId, sessionId, remoteAddress, properties)

  @Suppress("ComplexMethod") fun getByName(contextName: String): String {
    return when (contextName) {
      "environment" -> environment ?: ""
      "appName" -> appName ?: ""
      "userId" -> userId
      "sessionId" -> sessionId
      "remoteAddress" -> remoteAddress
      else -> properties[contextName] ?: ""
    }
  }

  fun applyStaticFields(config: UnleashConfig): UnleashContext {
    val builder = Builder(this)
    if (environment.isNullOrEmpty()) {
      builder.environment(config.environment)
    }
    if (appName.isNullOrEmpty()) {
      builder.appName(config.appName)
    }
    return builder.build()
  }

  class Builder {
    private var appName: String? = null
    private var environment: String? = null
    private var userId: String = ""
    private var sessionId: String = ""
    private var remoteAddress: String = ""
    private val properties: MutableMap<String, String> = mutableMapOf()

    constructor()
    constructor(context: UnleashContext) {
      if (context.appName != null) {
        appName = context.appName
      }
      if (context.environment != null) {
        environment = context.environment
      }
      if (context.userId.isNotEmpty()) {
        userId = context.userId
      }
      if (context.sessionId.isNotEmpty()) {
        sessionId = context.sessionId
      }
      if (context.remoteAddress.isNotEmpty()) {
        remoteAddress = context.remoteAddress
      }
      for ((key, value) in context.properties) {
        properties[key] = value
      }
    }

    fun appName(appName: String): Builder {
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
      name: String,
      value: String
    ): Builder {
      properties[name] = value
      return this
    }

    fun build(): UnleashContext {
      return UnleashContext(
        appName, environment, userId, sessionId, remoteAddress, properties
      )
    }
  }

  companion object {
    @JvmStatic fun builder(): Builder {
      return Builder()
    }
  }
}
