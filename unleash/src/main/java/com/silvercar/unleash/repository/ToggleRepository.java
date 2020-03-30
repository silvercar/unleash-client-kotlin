package com.silvercar.unleash.repository;

import com.silvercar.unleash.FeatureToggle;

import java.util.List;

public interface ToggleRepository {
    FeatureToggle getToggle(String name);

    List<String> getFeatureNames();
}
