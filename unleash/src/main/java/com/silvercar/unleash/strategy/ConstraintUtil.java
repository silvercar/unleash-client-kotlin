package com.silvercar.unleash.strategy;

import java.util.List;
import java.util.Optional;

import com.silvercar.unleash.Constraint;
import com.silvercar.unleash.Operator;
import com.silvercar.unleash.UnleashContext;

public class ConstraintUtil {

    public static boolean validate(List<Constraint> constraints, UnleashContext context) {
        if(constraints != null && constraints.size() > 0) {
            return constraints.stream().allMatch(c -> validateConstraint(c, context));
        } else {
            return true;
        }
    }

    private static boolean validateConstraint(Constraint constraint, UnleashContext context) {
        Optional<String> contextValue = context.getByName(constraint.getContextName());
        boolean isIn = contextValue.isPresent() && constraint.getValues().contains(contextValue.get().trim());
        return (constraint.getOperator() == Operator.IN) == isIn;
    }
}