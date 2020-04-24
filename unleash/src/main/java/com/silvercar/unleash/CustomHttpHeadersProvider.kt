package com.silvercar.unleash

interface CustomHttpHeadersProvider {
  fun getCustomHeaders(): Map<String, String>
}
