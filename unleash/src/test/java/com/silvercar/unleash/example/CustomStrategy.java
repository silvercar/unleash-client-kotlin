package com.silvercar.unleash.example;

import com.silvercar.unleash.UnleashContext;
import com.silvercar.unleash.strategy.Strategy;

import java.util.Map;
import org.jetbrains.annotations.NotNull;

final class CustomStrategy implements Strategy {
    @Override public String getName() {
        return "custom";
    }

    @Override public boolean isEnabled(@NotNull Map<String, String> parameters) {
        return false;
    }

    @Override public boolean isEnabled(@NotNull Map<String, String> parameters,
        @NotNull UnleashContext unleashContext) {
        return isEnabled(parameters);
    }
}
