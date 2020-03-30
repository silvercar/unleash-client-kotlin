package com.silvercar.unleash;

public interface UnleashContextProvider {
    UnleashContext getContext();

    static UnleashContextProvider getDefaultProvider() {
        return () -> UnleashContext.builder().build();
    }
}
