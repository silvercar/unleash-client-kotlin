package com.silvercar.unleash.repository

import com.silvercar.unleash.UnleashException
import com.silvercar.unleash.event.UnleashEvent
import com.silvercar.unleash.event.UnleashSubscriber
import com.silvercar.unleash.repository.FeatureToggleResponse.Status.UNAVAILABLE

data class FeatureToggleResponse(
  val status: Status,
  val httpStatusCode: Int = 200,
  val toggleCollection: ToggleCollection = ToggleCollection(listOf()),
  val location: String? = null
) : UnleashEvent {
  enum class Status {
    NOT_CHANGED,
    CHANGED,
    UNAVAILABLE
  }

  override fun toString(): String {
    return "FeatureToggleResponse: status=$status httpStatus=$httpStatusCode location=$location"
  }

  override fun publishTo(unleashSubscriber: UnleashSubscriber) {
    if (status == UNAVAILABLE) {
      var message = "Error fetching toggles from Unleash API - StatusCode: $httpStatusCode"
      if (location != null) {
        message += ", Location: $location"
      }
      unleashSubscriber.onError(UnleashException(message, null))
    }
    unleashSubscriber.togglesFetched(this)
  }
}
