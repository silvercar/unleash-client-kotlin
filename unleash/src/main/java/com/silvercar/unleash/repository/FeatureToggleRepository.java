package com.silvercar.unleash.repository;

import com.silvercar.unleash.event.EventDispatcher;
import com.silvercar.unleash.event.UnleashReady;
import com.silvercar.unleash.util.UnleashConfig;
import com.silvercar.unleash.util.UnleashScheduledExecutor;

import java.util.List;
import java.util.stream.Collectors;

import com.silvercar.unleash.FeatureToggle;
import com.silvercar.unleash.UnleashException;

public final class FeatureToggleRepository implements ToggleRepository {
    private final ToggleBackupHandler toggleBackupHandler;
    private final ToggleFetcher toggleFetcher;
    private final EventDispatcher eventDispatcher;

    private ToggleCollection toggleCollection;
    private boolean ready;

    public FeatureToggleRepository(
            UnleashConfig unleashConfig,
            ToggleFetcher toggleFetcher,
            ToggleBackupHandler toggleBackupHandler) {
        this(
                unleashConfig,
                unleashConfig.getScheduledExecutor(),
                toggleFetcher,
                toggleBackupHandler
        );
    }


    @Deprecated
    public FeatureToggleRepository(
            UnleashConfig unleashConfig,
            UnleashScheduledExecutor executor,
            ToggleFetcher toggleFetcher,
            ToggleBackupHandler toggleBackupHandler) {

        this.toggleBackupHandler = toggleBackupHandler;
        this.toggleFetcher = toggleFetcher;
        this.eventDispatcher = new EventDispatcher(unleashConfig);

        toggleCollection = toggleBackupHandler.read();

        if(unleashConfig.isSynchronousFetchOnInitialisation()){
            updateToggles().run();
        }

        executor.setInterval(updateToggles(), 0, unleashConfig.getFetchTogglesInterval());
    }

    private Runnable updateToggles() {
        return () -> {
            try {
                FeatureToggleResponse response = toggleFetcher.fetchToggles();
                eventDispatcher.dispatch(response);
                if (response.getStatus() == FeatureToggleResponse.Status.CHANGED) {
                    toggleCollection = response.getToggleCollection();
                    toggleBackupHandler.write(response.getToggleCollection());
                }

                if (!ready) {
                    eventDispatcher.dispatch(new UnleashReady());
                    ready = true;
                }
            } catch (UnleashException e) {
                eventDispatcher.dispatch(e);
            }
        };
    }

    @Override
    public FeatureToggle getToggle(String name) {
        return toggleCollection.getToggle(name);
    }

    @Override
    public List<String> getFeatureNames() {
        return toggleCollection.getFeatures().stream().map(toggle -> toggle.getName()).collect(Collectors.toList());
    }
}
