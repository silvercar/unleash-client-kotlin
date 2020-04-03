package com.silvercar.unleash

import com.google.gson.annotations.SerializedName
import com.silvercar.unleash.variant.Payload
import java.util.Objects
import java.util.Optional

class Variant(
  val name: String,
  private val payload: Payload?,
  @SerializedName("enabled") val isEnabled: Boolean
) {

  constructor(
    name: String,
    payload: String?,
    isEnabled: Boolean
  ) : this(name, Payload("string", payload), isEnabled)

  fun getPayload(): Optional<Payload> {
    return Optional.ofNullable(payload)
  }

  override fun toString(): String {
    return "Variant{" +
        "name='" + name + '\'' +
        ", payload='" + payload + '\'' +
        ", enabled=" + isEnabled +
        '}'
  }

  companion object {
    @JvmField val DISABLED_VARIANT =
      Variant("disabled", null as String?, false)
  }
}
