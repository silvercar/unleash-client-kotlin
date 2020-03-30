package com.silvercar.unleash.strategy;

import java.util.Map;
import java8.util.Optional;

import com.silvercar.unleash.UnleashContext;

import static java.util.Arrays.asList;

public final class UserWithIdStrategy implements Strategy {

    protected static final String PARAM = "userIds";
    private static final String STRATEGY_NAME = "userWithId";

    @Override
    public String getName() {
        return STRATEGY_NAME;
    }

    @Override
    public boolean isEnabled(Map<String, String> parameters) {
        return false;
    }

    @Override
    public boolean isEnabled(Map<String, String> parameters, UnleashContext unleashContext) {
        return unleashContext.getUserId()
                           .map(currentUserId -> Optional.ofNullable(parameters.get(PARAM))
                                                         .map(userIdString -> asList(userIdString.split(",\\s?")))
                                                         .filter(f -> f.contains(currentUserId)).isPresent())
                           .orElse(false);
    }
}
