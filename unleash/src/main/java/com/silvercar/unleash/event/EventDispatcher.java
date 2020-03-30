package com.silvercar.unleash.event;

import com.silvercar.unleash.util.UnleashConfig;
import com.silvercar.unleash.util.UnleashScheduledExecutor;

public class EventDispatcher {

    private final UnleashSubscriber unleashSubscriber;
    private final UnleashScheduledExecutor unleashScheduledExecutor;

    public EventDispatcher(UnleashConfig unleashConfig) {
        this.unleashSubscriber = unleashConfig.getSubscriber();
        this.unleashScheduledExecutor = unleashConfig.getScheduledExecutor();
    }

    public void dispatch(UnleashEvent unleashEvent) {
        unleashScheduledExecutor.scheduleOnce(() -> {
            unleashSubscriber.on(unleashEvent);
            unleashEvent.publishTo(unleashSubscriber);
        });
    }
}
