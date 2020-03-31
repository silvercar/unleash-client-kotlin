package com.silvercar.unleash.strategy;

import java.util.List;
import com.annimon.stream.Optional;

import com.annimon.stream.Stream;
import com.silvercar.unleash.Constraint;
import com.silvercar.unleash.Operator;
import com.silvercar.unleash.UnleashContext;

public class ConstraintUtil {

    public static boolean validate(List<Constraint> constraints, UnleashContext context) {
        if(constraints != null && constraints.size() > 0) {
            return Stream.of(constraints).allMatch(c -> validateConstraint(c, context));
        } else {
            return true;
        }
    }

    private static boolean validateConstraint(Constraint constraint, UnleashContext context) {
        String contextValue = context.getByName(constraint.getContextName());
        boolean contextValueValid = contextValue != null && !contextValue.isEmpty();
        boolean isIn = contextValueValid && constraint.getValues().contains(contextValue.trim());
        return (constraint.getOperator() == Operator.IN) == isIn;
    }
}
