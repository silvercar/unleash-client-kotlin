package com.silvercar.unleash

data class Constraint(
  val contextName: String,
  val operator: Operator,
  val values: List<String>
)

