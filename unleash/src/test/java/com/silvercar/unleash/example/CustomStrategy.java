package com.silvercar.unleash.example;

import com.silvercar.unleash.strategy.Strategy;

import java.util.Map;

final class CustomStrategy implements Strategy {
    @Override
    public String getName() {
        return "custom";
    }

    @Override
    public boolean isEnabled(Map<String, String> parameters) {
        return false;
    }
}
