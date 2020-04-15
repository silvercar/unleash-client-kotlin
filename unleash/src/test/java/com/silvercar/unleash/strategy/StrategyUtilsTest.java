package com.silvercar.unleash.strategy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StrategyUtilsTest {
    private StrategyUtils strategyUtils = new StrategyUtils();

    @Test
    public void normalized_values_are_the_same_across_node_java_and_go_clients() {
        assertEquals(73, strategyUtils.getNormalizedNumber("123", "gr1"));
        assertEquals(25, strategyUtils.getNormalizedNumber("999", "groupX"));
    }

}