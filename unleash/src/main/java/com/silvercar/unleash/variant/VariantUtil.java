package com.silvercar.unleash.variant;

import com.annimon.stream.Stream;
import com.annimon.stream.function.Predicate;
import com.silvercar.unleash.FeatureToggle;
import com.silvercar.unleash.UnleashContext;
import com.silvercar.unleash.Variant;
import com.silvercar.unleash.strategy.StrategyUtils;

import java.util.List;

import com.annimon.stream.Optional;

public final class VariantUtil {
    // Utility class
    private VariantUtil() {}

    private static Predicate<VariantOverride> overrideMatchesContext(UnleashContext context) {
        return (override) -> {
            String contextValue;
            switch (override.getContextName()) {
                case "userId": {
                    String userId = context.getUserId();
                    contextValue = userId != null && !userId.isEmpty() ? userId : "";
                    break;
                } case "sessionId": {
                    String sessionId = context.getSessionId();
                    contextValue = sessionId != null && !sessionId.isEmpty() ? sessionId : "";
                    break;
                } case "remoteAddress": {
                    String remoteAddress = context.getRemoteAddress();
                    contextValue = remoteAddress != null && !remoteAddress.isEmpty() ? remoteAddress : "";
                    break;
                } default:
                    String defaultValue = context.getProperties().get(override.getContextName());
                    contextValue = defaultValue != null && !defaultValue.isEmpty() ? defaultValue : "";
                    break;
            }
            return override.getValues().contains(contextValue);
        };
    }

    private static Optional<VariantDefinition> getOverride(List<VariantDefinition> variants, UnleashContext context) {
        return Stream.of(variants)
                .filter(variant -> Stream.of(variant.getOverrides()).anyMatch(overrideMatchesContext(context)))
                .findFirst();
    }

    private static String getIdentifier(UnleashContext context) {
        if (context.getUserId() != null && !context.getUserId().isEmpty()) {
            return context.getUserId();
        }

        if (context.getSessionId() != null && !context.getSessionId().isEmpty()) {
            return context.getSessionId();
        }

        if (context.getRemoteAddress() != null && !context.getRemoteAddress().isEmpty()) {
            return context.getRemoteAddress();
        }

        return Double.toString(Math.random());
    }

    public static Variant selectVariant(FeatureToggle featureToggle, UnleashContext context, Variant defaultVariant) {
        List<VariantDefinition> variants = featureToggle.getVariants();
        int totalWeight = Stream.of(variants).mapToInt(VariantDefinition::getWeight).sum();
        if(totalWeight == 0) {
            return defaultVariant;
        }

        Optional<VariantDefinition> variantOverride = getOverride(variants, context);
        if(variantOverride.isPresent()) {
            return variantOverride.get().toVariant();
        }

        int target = StrategyUtils.getNormalizedNumber(getIdentifier(context),featureToggle.getName(), totalWeight);

        int counter = 0;
        for (final VariantDefinition definition : featureToggle.getVariants()) {
            if (definition.getWeight() != 0) {
                counter += definition.getWeight();
                if (counter >= target) {
                    return definition.toVariant();
                }
            }
        }

        //Should not happen
        return defaultVariant;
    }
}
