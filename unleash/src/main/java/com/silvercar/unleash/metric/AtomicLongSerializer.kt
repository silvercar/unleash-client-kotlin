package com.silvercar.unleash.metric

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type
import java.util.concurrent.atomic.AtomicLong

internal class AtomicLongSerializer : JsonSerializer<AtomicLong> {
  override fun serialize(
    src: AtomicLong,
    typeOfSrc: Type,
    context: JsonSerializationContext
  ): JsonElement {
    return JsonPrimitive(src.get())
  }
}
