package com.silvercar.unleash.repository;

import com.silvercar.unleash.UnleashException;

public interface ToggleFetcher {
   FeatureToggleResponse fetchToggles() throws UnleashException;
}
