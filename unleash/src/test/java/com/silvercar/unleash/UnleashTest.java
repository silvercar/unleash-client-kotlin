package com.silvercar.unleash;

import com.silvercar.unleash.repository.ToggleRepository;
import com.silvercar.unleash.strategy.Strategy;
import com.silvercar.unleash.strategy.UserWithIdStrategy;
import com.silvercar.unleash.util.UnleashConfig;
import com.silvercar.unleash.util.UnleashConfigBuilder;
import com.silvercar.unleash.variant.Payload;
import com.silvercar.unleash.variant.VariantDefinition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import kotlin.jvm.functions.Function2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UnleashTest {

    private ToggleRepository toggleRepository;
    private UnleashContextProvider contextProvider;
    private Unleash unleash;

    @BeforeEach
    public void setup() {
        toggleRepository = mock(ToggleRepository.class);
        contextProvider = mock(UnleashContextProvider.class);
        when(contextProvider.getContext()).thenReturn(UnleashContext.builder().build());

        UnleashConfig config = new UnleashConfigBuilder()
                .appName("test")
                .unleashAPI("http://localhost:4242/api/")
                .environment("test")
                .unleashContextProvider(contextProvider)
                .build();

        unleash = new DefaultUnleash(config, toggleRepository, new UserWithIdStrategy());
    }

    @Test
    public void known_toggle_and_strategy_should_be_active() {
        final ActivationStrategy strategy = new ActivationStrategyBuilder().withName("default").build();
        when(toggleRepository.getToggle("test")).thenReturn(new FeatureToggle("test", true, asList(
            strategy)));

        assertThat(unleash.isEnabled("test"), is(true));
    }

    @Test
    public void unknown_strategy_should_be_considered_inactive() {
        final ActivationStrategy strategy = new ActivationStrategyBuilder().withName("whoot_strat").build();
        when(toggleRepository.getToggle("test")).thenReturn(new FeatureToggle("test", true, asList(strategy)));

        assertThat(unleash.isEnabled("test"), is(false));
    }

    @Test
    public void unknown_feature_should_be_considered_inactive() {
        when(toggleRepository.getToggle("test")).thenReturn(null);

        assertThat(unleash.isEnabled("test"), is(false));
    }

    @Test
    public void unknown_feature_should_use_default_setting() {
        when(toggleRepository.getToggle("test")).thenReturn(null);

        assertThat(unleash.isEnabled("test", true), is(true));
    }

    @Test
    public void fallback_function_should_be_invoked_and_return_true() {
        when(toggleRepository.getToggle("test")).thenReturn(null);
        Function2<String, UnleashContext, Boolean>  fallbackAction = mock(Function2.class);
        when(fallbackAction.invoke(eq("test"), any(UnleashContext.class))).thenReturn(true);

        assertThat(unleash.isEnabled("test", fallbackAction), is(true));
        verify(fallbackAction, times(1)).invoke(anyString(), any(UnleashContext.class));
    }

    @Test
    public void fallback_function_should_be_invoked_also_with_context() {
        when(toggleRepository.getToggle("test")).thenReturn(null);
        Function2<String, UnleashContext, Boolean>  fallbackAction = mock(Function2.class);
        when(fallbackAction.invoke(eq("test"), any(UnleashContext.class))).thenReturn(true);

        UnleashContext context = UnleashContext.builder().userId("123").build();

        assertThat(unleash.isEnabled("test", context, fallbackAction), is(true));
        verify(fallbackAction, times(1)).invoke(anyString(), any(UnleashContext.class));
    }

    @Test
    void fallback_function_should_be_invoked_and_return_false() {
        when(toggleRepository.getToggle("test")).thenReturn(null);
        Function2<String, UnleashContext, Boolean>  fallbackAction = mock(Function2.class);
        when(fallbackAction.invoke(eq("test"), any(UnleashContext.class))).thenReturn(false);

        assertThat(unleash.isEnabled("test", fallbackAction), is(false));
        verify(fallbackAction, times(1)).invoke(anyString(), any(UnleashContext.class));
    }

	@Test
	void fallback_function_should_not_be_called_when_toggle_is_defined() {
        final ActivationStrategy strategy = new ActivationStrategyBuilder().withName("default").build();
		when(toggleRepository.getToggle("test")).thenReturn(new FeatureToggle("test", true, asList(strategy)));

        Function2<String, UnleashContext, Boolean>  fallbackAction = mock(Function2.class);
        when(fallbackAction.invoke(eq("test"), any(UnleashContext.class))).thenReturn(false);

		assertThat(unleash.isEnabled("test", fallbackAction), is(true));
        verify(fallbackAction, never()).invoke(anyString(), any(UnleashContext.class));
	}

    @Test
    public void should_register_custom_strategies() {
        //custom strategy
        Strategy customStrategy = mock(Strategy.class);
        when(customStrategy.getName()).thenReturn("custom");

        final ActivationStrategy strategy = new ActivationStrategyBuilder()
            .withName("custom")
            .build();

        //register custom strategy
        UnleashConfig config = new UnleashConfigBuilder()
                .appName("test")
                .unleashAPI("http://localhost:4242/api/")
                .build();
        unleash = new DefaultUnleash(config, toggleRepository, customStrategy);
        when(toggleRepository.getToggle("test")).thenReturn(new FeatureToggle("test", true, asList(strategy)));

        unleash.isEnabled("test");

        verify(customStrategy, times(1)).isEnabled(any(Map.class), any(UnleashContext.class));
    }

    @Test
    public void should_support_multiple_strategies() {
        ActivationStrategy strategy1 = new ActivationStrategyBuilder()
            .withName("unknown")
            .build();
        ActivationStrategy activeStrategy = new ActivationStrategyBuilder()
            .withName("default")
            .build();

        FeatureToggle featureToggle = new FeatureToggle("test", true, asList(strategy1, activeStrategy));

        when(toggleRepository.getToggle("test")).thenReturn(featureToggle);

        assertThat(unleash.isEnabled("test"), is(true));
    }

    @Test
    public void should_support_context_provider() {
        UnleashContext context = UnleashContext.builder().userId("111").build();
        when(contextProvider.getContext()).thenReturn(context);

        //Set up a toggleName using UserWithIdStrategy
        Map<String, String> params = new HashMap<>();
        params.put("userIds", "123, 111, 121");
        ActivationStrategy strategy = new ActivationStrategyBuilder()
            .withName("userWithId")
            .withParameters(params)
            .build();
        FeatureToggle featureToggle = new FeatureToggle("test", true, asList(strategy));

        when(toggleRepository.getToggle("test")).thenReturn(featureToggle);

        assertThat(unleash.isEnabled("test"), is(true));
    }
    
    @Test
    public void should_support_context_as_part_of_is_enabled_call() {
        UnleashContext context = UnleashContext.builder().userId("13").build();

        //Set up a toggleName using UserWithIdStrategy
        Map<String, String> params = new HashMap<>();
        params.put("userIds", "123, 111, 121, 13");
        ActivationStrategy strategy = new ActivationStrategyBuilder()
            .withName("userWithId")
            .withParameters(params)
            .build();
        FeatureToggle featureToggle = new FeatureToggle("test", true, asList(strategy));

        when(toggleRepository.getToggle("test")).thenReturn(featureToggle);

        assertThat(unleash.isEnabled("test", context), is(true));
    }

    @Test
    public void should_support_context_as_part_of_is_enabled_call_and_use_default() {
        UnleashContext context = UnleashContext.builder().userId("13").build();

        //Set up a toggle using UserWithIdStrategy
        Map<String, String> params = new HashMap<>();
        params.put("userIds", "123, 111, 121, 13");

        assertThat(unleash.isEnabled("test", context, true), is(true));
    }

    @Test
    public void inactive_feature_toggle() {
        ActivationStrategy strategy1 = new ActivationStrategyBuilder()
            .withName("unknown")
            .build();
        FeatureToggle featureToggle = new FeatureToggle("test", false, asList(strategy1));
        when(toggleRepository.getToggle("test")).thenReturn(featureToggle);

        assertThat(unleash.isEnabled("test"), is(false));
    }

    @Test
    public void should_return_known_feature_toggle_definition() {
        ActivationStrategy strategy1 = new ActivationStrategyBuilder()
            .withName("unknown")
            .build();
        FeatureToggle featureToggle = new FeatureToggle("test", false, asList(strategy1));
        when(toggleRepository.getToggle("test")).thenReturn(featureToggle);

        assertThat(((DefaultUnleash)unleash).getFeatureToggleDefinition("test"), is(featureToggle));
    }

    @Test
    public void should_return_empty_for_unknown_feature_toggle_definition() {
        ActivationStrategy strategy1 = new ActivationStrategyBuilder()
            .withName("unknown")
            .build();
        FeatureToggle featureToggle = new FeatureToggle("test", false, asList(strategy1));
        when(toggleRepository.getToggle("test")).thenReturn(featureToggle);

        assertNull(((DefaultUnleash)unleash).getFeatureToggleDefinition("another toggleName"));
    }

    @Test
    public void get_feature_names_should_return_list_of_feature_names() {
        when(toggleRepository.getFeatureNames()).thenReturn(asList("toggleFeatureName1", "toggleFeatureName2"));
        assertTrue(2 == unleash.getFeatureToggleNames().size());
        assertTrue("toggleFeatureName2".equals(unleash.getFeatureToggleNames().get(1)));
    }

    @Test
    public void get_default_variant_when_disabled() {
        UnleashContext context = UnleashContext.builder().userId("1").build();

        //Set up a toggleName using UserWithIdStrategy
        Map<String, String> params = new HashMap<>();
        params.put("userIds", "123, 111, 121, 13");
        ActivationStrategy strategy = new ActivationStrategyBuilder()
            .withName("userWithId")
            .withParameters(params)
            .build();
        FeatureToggle featureToggle = new FeatureToggle("test", true, asList(strategy));

        when(toggleRepository.getToggle("test")).thenReturn(featureToggle);

        final Variant result = unleash.getVariant("test", context, new Variant("Chuck", "Norris", true));

        assertThat(result, is(notNullValue()));
        assertThat(result.getName(), is("Chuck"));
        assertThat(result.getPayload().map(Payload::getValue).get(), is("Norris"));
        assertThat(result.isEnabled(), is(true));
    }

    @Test
    public void get_default_empty_variant_when_disabled_and_no_default_value_is_specified() {
        UnleashContext context = UnleashContext.builder().userId("1").build();

        //Set up a toggleName using UserWithIdStrategy
        Map<String, String> params = new HashMap<>();
        params.put("userIds", "123, 111, 121, 13");
        ActivationStrategy strategy = new ActivationStrategyBuilder()
            .withName("userWithId")
            .withParameters(params)
            .build();
        FeatureToggle featureToggle = new FeatureToggle("test", true, asList(strategy));

        when(toggleRepository.getToggle("test")).thenReturn(featureToggle);

        final Variant result = unleash.getVariant("test", context);

        assertThat(result, is(notNullValue()));
        assertThat(result.getName(), is("disabled"));
        assertThat(result.getPayload().map(Payload::getValue), is(Optional.empty()));
        assertThat(result.isEnabled(), is(false));
    }

    @Test
    public void get_first_variant() {
        UnleashContext context = UnleashContext.builder().userId("356").build();

        //Set up a toggleName using UserWithIdStrategy
        Map<String, String> params = new HashMap<>();
        params.put("userIds", "123, 111, 121, 356");
        ActivationStrategy strategy = new ActivationStrategyBuilder()
            .withName("userWithId")
            .withParameters(params)
            .build();
        FeatureToggle featureToggle = new FeatureToggle("test", true, asList(strategy), getTestVariants());

        when(toggleRepository.getToggle("test")).thenReturn(featureToggle);

        final Variant result = unleash.getVariant("test", context);

        assertThat(result, is(notNullValue()));
        assertThat(result.getName(), is("en"));
        assertThat(result.getPayload().map(Payload::getValue).get(), is("en"));
        assertThat(result.isEnabled(), is(true));
    }

    @Test
    public void get_second_variant() {
        UnleashContext context = UnleashContext.builder().userId("111").build();

        //Set up a toggleName using UserWithIdStrategy
        Map<String, String> params = new HashMap<>();
        params.put("userIds", "123, 111, 121, 13");
        ActivationStrategy strategy = new ActivationStrategyBuilder()
            .withName("userWithId")
            .withParameters(params)
            .build();
        FeatureToggle featureToggle = new FeatureToggle("test", true, asList(strategy), getTestVariants());

        when(toggleRepository.getToggle("test")).thenReturn(featureToggle);

        final Variant result = unleash.getVariant("test", context);

        assertThat(result, is(notNullValue()));
        assertThat(result.getName(), is("to"));
        assertThat(result.getPayload().map(Payload::getValue).get(), is("to"));
        assertThat(result.isEnabled(), is(true));
    }

    @Test
    public void get_disabled_variant_without_context() {

        //Set up a toggleName using UserWithIdStrategy
        ActivationStrategy strategy1 = new ActivationStrategyBuilder()
            .withName("unknown")
            .build();
        FeatureToggle featureToggle = new FeatureToggle("test", true, asList(strategy1), getTestVariants());

        when(toggleRepository.getToggle("test")).thenReturn(featureToggle);

        final Variant result = unleash.getVariant("test");

        assertThat(result, is(notNullValue()));
        assertThat(result.getName(), is("disabled"));
        assertThat(result.getPayload().map(Payload::getValue), is(Optional.empty()));
        assertThat(result.isEnabled(), is(false));
    }

    @Test
    public void get_default_variant_without_context() {
        //Set up a toggleName using UserWithIdStrategy
        ActivationStrategy strategy1 = new ActivationStrategyBuilder()
            .withName("unknown")
            .build();
        FeatureToggle featureToggle = new FeatureToggle("test", true, asList(strategy1), getTestVariants());

        when(toggleRepository.getToggle("test")).thenReturn(featureToggle);

        final Variant result = unleash.getVariant("test", new Variant("Chuck", "Norris", true));

        assertThat(result, is(notNullValue()));
        assertThat(result.getName(), is("Chuck"));
        assertThat(result.getPayload().map(Payload::getValue).get(), is("Norris"));
        assertThat(result.isEnabled(), is(true));
    }

    @Test
    public void get_first_variant_with_context_provider() {

        UnleashContext context = UnleashContext.builder().userId("356").build();
        when(contextProvider.getContext()).thenReturn(context);

        //Set up a toggleName using UserWithIdStrategy
        Map<String, String> params = new HashMap<>();
        params.put("userIds", "123, 111, 356");
        ActivationStrategy strategy = new ActivationStrategyBuilder()
            .withName("userWithId")
            .withParameters(params)
            .build();
        FeatureToggle featureToggle = new FeatureToggle("test", true, asList(strategy), getTestVariants());

        when(toggleRepository.getToggle("test")).thenReturn(featureToggle);

        final Variant result = unleash.getVariant("test");

        assertThat(result, is(notNullValue()));
        assertThat(result.getName(), is("en"));
        assertThat(result.getPayload().map(Payload::getValue).get(), is("en"));
        assertThat(result.isEnabled(), is(true));
    }

    @Test
    public void get_second_variant_with_context_provider() {

        UnleashContext context = UnleashContext.builder().userId("111").build();
        when(contextProvider.getContext()).thenReturn(context);

        //Set up a toggleName using UserWithIdStrategy
        Map<String, String> params = new HashMap<>();
        params.put("userIds", "123, 111, 121");
        ActivationStrategy strategy = new ActivationStrategyBuilder()
            .withName("userWithId")
            .withParameters(params)
            .build();
        FeatureToggle featureToggle = new FeatureToggle("test", true, asList(strategy), getTestVariants());

        when(toggleRepository.getToggle("test")).thenReturn(featureToggle);

        final Variant result = unleash.getVariant("test");

        assertThat(result, is(notNullValue()));
        assertThat(result.getName(), is("to"));
        assertThat(result.getPayload().map(Payload::getValue).get(), is("to"));
        assertThat(result.isEnabled(), is(true));
    }

    @Test
    public void should_be_enabled_with_strategy_constraints() {
        List<Constraint> constraints = new ArrayList<>();
        constraints.add(new Constraint("environment", Operator.IN, Arrays.asList("test")));
        ActivationStrategy activeStrategy = new ActivationStrategy("default", null, constraints);

        FeatureToggle featureToggle = new FeatureToggle("test", true, asList(activeStrategy));

        when(toggleRepository.getToggle("test")).thenReturn(featureToggle);

        assertThat(unleash.isEnabled("test"), is(true));
    }

    @Test
    public void should_be_disabled_with_strategy_constraints() {
        List<Constraint> constraints = new ArrayList<>();
        constraints.add(new Constraint("environment", Operator.IN, Arrays.asList("dev", "prod")));
        ActivationStrategy activeStrategy = new ActivationStrategy("default", null, constraints);

        FeatureToggle featureToggle = new FeatureToggle("test", true, asList(activeStrategy));

        when(toggleRepository.getToggle("test")).thenReturn(featureToggle);

        assertThat(unleash.isEnabled("test"), is(false));
    }

    @Test public void should_be_enabled_for_empty_constraints() {
        final ActivationStrategy activeStrategy =
            new ActivationStrategy("default", null, new ArrayList<>());

        final FeatureToggle featureToggle = new FeatureToggle("test", true,
            Collections.singletonList(activeStrategy));
        when(toggleRepository.getToggle("test")).thenReturn(featureToggle);

        final boolean result = unleash.isEnabled("test");

        assertTrue(result);
    }

    @Test public void should_be_enabled_for_null_constraints() {
        final ActivationStrategy activeStrategy =
            new ActivationStrategy("default", null, new ArrayList<>());

        final FeatureToggle featureToggle = new FeatureToggle("test", true,
            Collections.singletonList(activeStrategy));
        when(toggleRepository.getToggle("test")).thenReturn(featureToggle);

        final boolean result = unleash.isEnabled("test");

        assertTrue(result);
    }

    @Test public void should_be_disabled_when_constraint_not_satisfied() {
        final List<Constraint> constraints = new ArrayList<>();
        constraints.add(new Constraint("environment", Operator.IN,
            Collections.singletonList("prod")));

        final ActivationStrategy activeStrategy =
            new ActivationStrategy("default", null, constraints);

        final FeatureToggle featureToggle = new FeatureToggle("test", true,
            Collections.singletonList(activeStrategy));
        when(toggleRepository.getToggle("test")).thenReturn(featureToggle);

        final boolean result = unleash.isEnabled("test");

        assertFalse(result);
    }

    @Test public void should_be_enabled_when_constraint_is_satisfied() {
        final List<Constraint> constraints = new ArrayList<>();
        constraints.add(new Constraint("environment", Operator.IN, Arrays.asList("test", "prod")));

        final ActivationStrategy activeStrategy =
            new ActivationStrategy("default", null, constraints);

        final FeatureToggle featureToggle = new FeatureToggle("test", true,
            Collections.singletonList(activeStrategy));
        when(toggleRepository.getToggle("test")).thenReturn(featureToggle);

        final boolean result = unleash.isEnabled("test");

        assertTrue(result);
    }

    @Test public void should_be_enabled_when_constraint_NOT_IN_satisfied() {
        final List<Constraint> constraints = new ArrayList<>();
        constraints.add(new Constraint("environment", Operator.NOT_IN,
            Collections.singletonList("prod")));

        final ActivationStrategy activeStrategy =
            new ActivationStrategy("default", null, constraints);

        final FeatureToggle featureToggle = new FeatureToggle("test", true,
            Collections.singletonList(activeStrategy));
        when(toggleRepository.getToggle("test")).thenReturn(featureToggle);

        final boolean result = unleash.isEnabled("test");

        assertTrue(result);
    }

    @Test
    public void should_be_enabled_when_all_constraints_are_satisfied() {
        final UnleashContext context = UnleashContext.builder()
            .environment("test")
            .userId("123")
            .addProperty("customerId", "blue")
            .build();

        final List<Constraint> constraints = new ArrayList<>();
        constraints.add(new Constraint("environment", Operator.IN, Arrays.asList("test", "prod")));
        constraints.add(new Constraint("userId", Operator.IN, Arrays.asList("123")));
        constraints.add(new Constraint("customerId", Operator.IN, Arrays.asList("red", "blue")));

        final ActivationStrategy activeStrategy =
            new ActivationStrategy("default", null, constraints);

        final FeatureToggle featureToggle = new FeatureToggle("test", true,
            Collections.singletonList(activeStrategy));
        when(toggleRepository.getToggle("test")).thenReturn(featureToggle);

        final boolean result = unleash.isEnabled("test", context);

        assertTrue(result);
    }

    @Test public void should_be_disabled_when_not_all_constraints_are_satisfied() {
        final UnleashContext context = UnleashContext.builder()
            .environment("test")
            .userId("123")
            .addProperty("customerId", "orange")
            .build();

        final List<Constraint> constraints = new ArrayList<>();
        constraints.add(new Constraint("environment", Operator.IN, Arrays.asList("test", "prod")));
        constraints.add(new Constraint("userId", Operator.IN, Arrays.asList("123")));
        constraints.add(new Constraint("customerId", Operator.IN, Arrays.asList("red", "blue")));

        final ActivationStrategy activeStrategy =
            new ActivationStrategy("default", null, constraints);

        final FeatureToggle featureToggle = new FeatureToggle("test", true,
            Collections.singletonList(activeStrategy));
        when(toggleRepository.getToggle("test")).thenReturn(featureToggle);

        final boolean result = unleash.isEnabled("test", context);

        assertFalse(result);
    }

    private List<VariantDefinition> getTestVariants() {
        return asList(
            new VariantDefinition("en", 50, new Payload("string", "en"), Collections.emptyList()),
            new VariantDefinition("to", 50, new Payload("string", "to"), Collections.emptyList())
        );
    }
}
