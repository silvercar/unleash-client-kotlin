package com.silvercar.unleash.strategy;

import java.util.List;
import java.util.Map;

import com.silvercar.unleash.Constraint;
import com.silvercar.unleash.UnleashContext;

public interface Strategy {
    String getName();

    boolean isEnabled(Map<String, String> parameters);

    default boolean isEnabled(Map<String, String> parameters, UnleashContext unleashContext) {
        return isEnabled(parameters);
    }

    default boolean isEnabled(Map<String, String> parameters, UnleashContext unleashContext, List<Constraint> constraints) {
        return ConstraintUtil.validate(constraints, unleashContext) && isEnabled(parameters, unleashContext);
    }
}
