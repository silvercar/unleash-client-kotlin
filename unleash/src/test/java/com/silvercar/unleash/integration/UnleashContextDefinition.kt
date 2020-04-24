package com.silvercar.unleash.integration

data class UnleashContextDefinition(
  private val userId: String?,
  private val sessionId: String?,
  private val remoteAddress: String?,
  private val environment: String?,
  private val appName: String?,
  // Custom context fields used in tests
  private val properties: Map<String, String>?
) {

  fun getUserId(): String {
    return userId ?: ""
  }

  fun getSessionId(): String {
    return sessionId ?: ""
  }

  fun getRemoteAddress(): String {
    return remoteAddress ?: ""
  }

  fun getEnvironment(): String {
    return environment ?: ""
  }

  fun getAppName(): String {
    return appName ?: ""
  }

  fun getProperties(): Map<String, String> {
    return properties ?: mapOf()
  }
}
