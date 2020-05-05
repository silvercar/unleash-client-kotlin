package com.silvercar.unleash

fun activationStrategy(func: ActivationStrategyBuilder.() -> Unit) =
  ActivationStrategyBuilder().apply(func)

class ActivationStrategyBuilder {
  private var name = ""
  private var parameters: Map<String, String>? = null
  private var constraints: List<Constraint> = listOf()

  fun withName(name: String): ActivationStrategyBuilder {
    this.name = name;
    return this
  }

  fun withParameters(parameters: Map<String, String>): ActivationStrategyBuilder {
    this.parameters = parameters
    return this
  }

  fun build(): ActivationStrategy {
    return ActivationStrategy(name, parameters, constraints)
  }
}
