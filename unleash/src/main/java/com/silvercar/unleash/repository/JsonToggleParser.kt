package com.silvercar.unleash.repository

import java.io.Reader

internal class JsonToggleParser {
  private val gsonFactory = GsonFactory

  fun toJsonString(toggleCollection: ToggleCollection?): String {
    return gsonFactory.getInstance().toJson(toggleCollection)
  }

  @Throws(IllegalStateException::class)
  fun fromJson(reader: Reader?): ToggleCollection {
    val gson = gsonFactory.getInstance()
    val toggleCollection = gson.fromJson(reader, ToggleCollection::class.java)

    check(toggleCollection?.features != null) {
      "Could not extract toggles from json"
    }

    // TODO: Break up ToggleCollection model and ToggleCollectionResponse used for serialization
    return ToggleCollection(toggleCollection.features)
  }
}
