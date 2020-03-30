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
            Optional<String> contextValue;
            switch (override.getContextName()) {
                case "userId": {
                    contextValue = context.getUserId();
                    break;
                } case "sessionId": {
                    contextValue = context.getSessionId();
                    break;
                } case "remoteAddress": {
                    contextValue = context.getRemoteAddress();
                    break;
                } default:
                    contextValue = Optional.ofNullable(context.getProperties().get(override.getContextName()));
                    break;
            }
            return override.getValues().contains(contextValue.orElse(""));
        };
    }

    private static Optional<VariantDefinition> getOverride(List<VariantDefinition> variants, UnleashContext context) {
        return Stream.of(variants)
                .filter(variant -> Stream.of(variant.getOverrides()).anyMatch(overrideMatchesContext(context)))
                .findFirst();
    }

    private static String getIdentifier(UnleashContext context) {
        return context.getUserId()
                .orElse(context.getSessionId()
                .orElse(context.getRemoteAddress()
                .orElse(Double.toString(Math.random()))));
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
