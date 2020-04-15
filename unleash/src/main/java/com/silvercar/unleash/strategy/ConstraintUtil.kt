package com.silvercar.unleash.strategy

import com.silvercar.unleash.Constraint
import com.silvercar.unleash.Operator.IN
import com.silvercar.unleash.UnleashContext

class ConstraintUtil {
  fun validate(
    constraints: List<Constraint>?,
    context: UnleashContext
  ): Boolean {
    if (constraints.isNullOrEmpty()) {
      return true
    }
    return constraints.all { validateConstraint(it, context) }
  }

  private fun validateConstraint(
    constraint: Constraint,
    context: UnleashContext
  ): Boolean {
    val contextValue: String = context.getByName(constraint.contextName).trim()
    return constraint.operator == IN == constraint.values.contains(contextValue)
  }
}
