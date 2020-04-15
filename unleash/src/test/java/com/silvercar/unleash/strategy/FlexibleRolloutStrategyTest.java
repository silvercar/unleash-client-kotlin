package com.silvercar.unleash.strategy;

import java.util.HashMap;
import java.util.Map;

import com.silvercar.unleash.UnleashContext;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static com.silvercar.unleash.strategy.FlexibleRolloutStrategy.STICKINESS;
import static com.silvercar.unleash.strategy.Strategy.GROUP_ID;
import static com.silvercar.unleash.strategy.Strategy.ROLLOUT;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FlexibleRolloutStrategyTest {

    @Test
    public void should_have_correct_name() {
        assertEquals("flexibleRollout", getStrategy().getName());
    }

    @Test
    public void should_always_be_false() {
        assertFalse(getStrategy().isEnabled(new HashMap<>()));
    }

    @Test
    public void should_NOT_be_enabled_for_rollout_9_and_userId_61() {
        Map<String, String> params = new HashMap<>();
        params.put(ROLLOUT, "9");
        params.put(STICKINESS, "default");
        params.put(GROUP_ID, "Demo");

        UnleashContext context = UnleashContext.builder().userId("61").build();
        boolean enabled = getStrategy().isEnabled(params, context);
        assertFalse(enabled);
    }

    @Test
    public void should_be_enabled_for_rollout_10_and_userId_61() {
        Map<String, String> params = new HashMap<>();
        params.put(ROLLOUT, "10");
        params.put(STICKINESS, "default");
        params.put(GROUP_ID, "Demo");

        UnleashContext context = UnleashContext.builder().userId("61").build();
        boolean enabled = getStrategy().isEnabled(params, context);
        assertTrue(enabled);
    }

    @Test
    public void should_be_enabled_for_rollout_10_and_userId_61_and_stickiness_userId() {
        Map<String, String> params = new HashMap<>();
        params.put(ROLLOUT, "10");
        params.put(STICKINESS, "userId");
        params.put(GROUP_ID, "Demo");

        UnleashContext context = UnleashContext.builder().userId("61").build();
        boolean enabled = getStrategy().isEnabled(params, context);
        assertTrue(enabled);
    }

    @Test
    public void should_be_disabled_when_userId_missing() {
        Map<String, String> params = new HashMap<>();
        params.put(ROLLOUT, "100");
        params.put(STICKINESS, "userId");
        params.put(GROUP_ID, "Demo");

        UnleashContext context = UnleashContext.builder().build();
        boolean enabled = getStrategy().isEnabled(params, context);
        assertFalse(enabled);
    }

    @Test
    public void should_be_enabled_for_rollout_10_and_sessionId_61() {
        Map<String, String> params = new HashMap<>();
        params.put(ROLLOUT, "10");
        params.put(STICKINESS, "default");
        params.put(GROUP_ID, "Demo");

        UnleashContext context = UnleashContext.builder().sessionId("61").build();
        boolean enabled = getStrategy().isEnabled(params, context);
        assertTrue(enabled);
    }
    @Test
    public void should_be_enabled_for_rollout_10_and_randomId_61_and_stickiness_sessionId() {
        Map<String, String> params = new HashMap<>();
        params.put(ROLLOUT, "10");
        params.put(STICKINESS, "sessionId");
        params.put(GROUP_ID, "Demo");

        UnleashContext context = UnleashContext.builder().sessionId("61").build();
        boolean enabled = getStrategy().isEnabled(params, context);
        assertTrue(enabled);
    }

    @Test
    public void should_be_enabled_for_rollout_10_and_randomId_61_and_stickiness_default() {
        final RandomGenerator randomGenerator = mock(RandomGenerator.class);
        when(randomGenerator.get()).thenReturn("61");

        FlexibleRolloutStrategy strategy = new FlexibleRolloutStrategy(randomGenerator);
        Map<String, String> params = new HashMap<>();
        params.put(ROLLOUT, "10");
        params.put(STICKINESS, "default");
        params.put(GROUP_ID, "Demo");

        UnleashContext context = UnleashContext.builder().build();
        boolean enabled = strategy.isEnabled(params, context);
        assertTrue(enabled);
    }

    @Test
    public void should_be_enabled_for_rollout_10_and_randomId_61_and_stickiness_random() {
        final RandomGenerator randomGenerator = mock(RandomGenerator.class);
        when(randomGenerator.get()).thenReturn("61");

        FlexibleRolloutStrategy strategy = new FlexibleRolloutStrategy(randomGenerator);
        Map<String, String> params = new HashMap<>();
        params.put(ROLLOUT, "10");
        params.put(STICKINESS, "random");
        params.put(GROUP_ID, "Demo");

        UnleashContext context = UnleashContext.builder().build();
        boolean enabled = strategy.isEnabled(params, context);
        assertTrue(enabled);
    }



    @Test
    public void should_NOT_be_enabled_for_rollout_10_and_randomId_1() {
        final RandomGenerator randomGenerator = mock(RandomGenerator.class);
        when(randomGenerator.get()).thenReturn("1");

        FlexibleRolloutStrategy strategy = new FlexibleRolloutStrategy(randomGenerator);
        Map<String, String> params = new HashMap<>();
        params.put(ROLLOUT, "10");
        params.put(STICKINESS, "default");
        params.put(GROUP_ID, "Demo");

        UnleashContext context = UnleashContext.builder().build();
        boolean enabled = strategy.isEnabled(params, context);
        assertFalse(enabled);
    }

    @NotNull private FlexibleRolloutStrategy getStrategy() {
        return new FlexibleRolloutStrategy(new RandomGenerator());
    }
}