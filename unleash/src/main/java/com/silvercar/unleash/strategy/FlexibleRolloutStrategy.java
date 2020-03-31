package com.silvercar.unleash.strategy;

import java.util.Map;
import com.annimon.stream.Optional;

import com.annimon.stream.function.Supplier;
import com.silvercar.unleash.UnleashContext;

public class FlexibleRolloutStrategy implements Strategy {
    protected static final String PERCENTAGE = "rollout";
    protected static final String GROUP_ID = "groupId";

    private Supplier<String> randomGenerator;

    public FlexibleRolloutStrategy() {
        this.randomGenerator = () ->  Math.random() * 100 + "";
    }

    public FlexibleRolloutStrategy(Supplier<String> randomGenerator) {
        this.randomGenerator = randomGenerator;
    }

    @Override
    public String getName() {
        return "flexibleRollout";
    }

    @Override
    public boolean isEnabled(Map<String, String> parameters) {
        return false;
    }

    private String resolveStickiness(String stickiness, UnleashContext context) {
        switch (stickiness) {
            case "userId": return context.getUserId();
            case "sessionId": return context.getSessionId();
            case "random": return randomGenerator.get();
            default:
                String userId = context.getUserId();
                if (userId != null && !userId.isEmpty()) return userId;

                String sessionId = context.getSessionId();
                if (sessionId != null && !sessionId.isEmpty()) return sessionId;

                return this.randomGenerator.get();
        }
    }

    @Override
    public boolean isEnabled(Map<String, String> parameters, UnleashContext unleashContext) {
        final String stickiness = getStickiness(parameters);
        final String stickinessId = resolveStickiness(stickiness, unleashContext);
        final int percentage = StrategyUtils.getPercentage(parameters.get(PERCENTAGE));
        final String groupId = Optional.ofNullable(parameters.get(GROUP_ID)).orElse("");

        if (stickinessId != null && !stickinessId.isEmpty()) {
            final int normalizedUserId = StrategyUtils.getNormalizedNumber(stickinessId, groupId);
            return percentage > 0 && normalizedUserId <= percentage;
        } else {
            return false;
        }
    }

    private String getStickiness(Map<String, String> parameters) {
        Optional<String> stickiness = Optional.ofNullable(parameters.get("stickiness"));
        return stickiness.orElse("default");
    }
}
