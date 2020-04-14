package com.silvercar.unleash.repository

import com.google.gson.Gson

object GsonFactory {
  private val gson = Gson()

  fun getInstance(): Gson {
    return gson
  }
}
