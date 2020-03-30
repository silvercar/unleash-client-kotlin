package com.silvercar.unleash.event;

import com.silvercar.unleash.UnleashException;
import com.silvercar.unleash.metric.ClientMetrics;
import com.silvercar.unleash.metric.ClientRegistration;
import com.silvercar.unleash.repository.FeatureToggleResponse;
import com.silvercar.unleash.repository.ToggleCollection;
import org.apache.logging.log4j.LogManager;

public interface UnleashSubscriber {

    default void onError(UnleashException unleashException) {
        LogManager.getLogger(UnleashSubscriber.class).warn(unleashException.getMessage(), unleashException);
    }

    default void on(UnleashEvent unleashEvent) { }
    default void onReady(UnleashReady unleashReady) { }
    default void toggleEvaluated(ToggleEvaluated toggleEvaluated) { }
    default void togglesFetched(FeatureToggleResponse toggleResponse) { }
    default void clientMetrics(ClientMetrics clientMetrics) { }
    default void clientRegistered(ClientRegistration clientRegistration) { }
    default void togglesBackedUp(ToggleCollection toggleCollection) { }
    default void toggleBackupRestored(ToggleCollection toggleCollection) { }

}
