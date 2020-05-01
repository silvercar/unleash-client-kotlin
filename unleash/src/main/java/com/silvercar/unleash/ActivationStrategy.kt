package com.silvercar.unleash

data class ActivationStrategy constructor(
  val name: String,
  private val parameters: Map<String, String>?,
  val constraints: List<Constraint> = listOf()
) {

  fun getParameters(): Map<String, String> {
    return parameters ?: mapOf()
  }
}
