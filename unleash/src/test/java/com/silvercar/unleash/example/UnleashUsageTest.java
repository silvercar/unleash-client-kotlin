package com.silvercar.unleash.example;

import com.silvercar.unleash.DefaultUnleash;
import com.silvercar.unleash.TestUtil;
import com.silvercar.unleash.Unleash;
import com.silvercar.unleash.util.UnleashConfig;
import com.silvercar.unleash.util.UnleashConfigBuilder;
import org.apache.logging.log4j.Level;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class UnleashUsageTest {

    @Test
    public void wire() {
        TestUtil.setLogLevel(Level.ERROR); //Mute warn messages.
        UnleashConfig config = new UnleashConfigBuilder()
                .appName("test")
                .instanceId("my-hostname:6517")
                .synchronousFetchOnInitialisation(true)
                .unleashAPI("http://localhost:4242/api")
                .build();

        Unleash unleash = new DefaultUnleash(config, new CustomStrategy());

        assertFalse(unleash.isEnabled("myFeature"));
    }
}
