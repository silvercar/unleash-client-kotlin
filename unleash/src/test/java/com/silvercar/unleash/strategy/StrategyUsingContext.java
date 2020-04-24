package com.silvercar.unleash.strategy;

import java.util.List;
import java.util.Map;
import com.silvercar.unleash.UnleashContext;

import static java.util.Arrays.asList;

public class StrategyUsingContext implements Strategy {


    @Override
    public String getName() {
        return "usingContext";
    }

    @Override
    public boolean isEnabled(Map<String, String> parameters) {
        return false;
    }

    @Override
    public boolean isEnabled(Map<String, String> parameters, UnleashContext unleashContext) {
        String userIdString = parameters.get("userIds");
        List<String> userIds = asList(userIdString.split(",\\s?"));
        if(!unleashContext.getUserId().isEmpty()) {
            String userId = unleashContext.getUserId();
            return userIds.contains(userId);
        } else {
            return false;
        }
    }
}
