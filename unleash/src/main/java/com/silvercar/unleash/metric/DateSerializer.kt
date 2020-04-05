package com.silvercar.unleash.metric

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

internal class DateSerializer : JsonSerializer<Date> {
  override fun serialize(
    date: Date,
    type: Type,
    jsonSerializationContext: JsonSerializationContext
  ): JsonElement {
    val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
      timeZone = TimeZone.getTimeZone("GMT")
    }
    return JsonPrimitive(formatter.format(date))
  }
}
