package com.silvercar.unleash;

import com.silvercar.unleash.event.UnleashEvent;
import com.silvercar.unleash.event.UnleashSubscriber;

public class UnleashException extends RuntimeException implements UnleashEvent {

    public UnleashException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public void publishTo(UnleashSubscriber unleashSubscriber) {
        unleashSubscriber.onError(this);
    }
}
