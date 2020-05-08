package com.silvercar.unleash.variant;

import com.silvercar.unleash.ActivationStrategyBuilder;
import java.util.Arrays;
import java.util.Collections;

import com.silvercar.unleash.ActivationStrategy;
import com.silvercar.unleash.FeatureToggle;
import com.silvercar.unleash.UnleashContext;
import com.silvercar.unleash.Variant;
import org.junit.Test;

import static java.util.Arrays.asList;
import static com.silvercar.unleash.Variant.DISABLED_VARIANT;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;


public class VariantUtilTest {
    private final ActivationStrategy defaultStrategy = new ActivationStrategyBuilder()
        .withName("default")
        .build();

    @Test
    public void should_return_default_variant_when_toggle_has_no_variants() {
        FeatureToggle toggle = new FeatureToggle("test.variants", true, asList(defaultStrategy));
        UnleashContext context = UnleashContext.builder().build();

        Variant variant = new VariantUtil().selectVariant(toggle, context, DISABLED_VARIANT);


        assertThat(variant, is(DISABLED_VARIANT));
    }

    @Test
    public void should_return_variant1() {
        VariantDefinition v1 = new VariantDefinition("a", 33, new Payload("string", "asd"), Collections.emptyList());
        VariantDefinition v2 = new VariantDefinitionBuilder("b", 33).build();
        VariantDefinition v3 = new VariantDefinitionBuilder("c", 34).build();

        FeatureToggle toggle = new FeatureToggle(
                "test.variants",
                true,
                asList(defaultStrategy), asList(v1, v2, v3));

        UnleashContext context = UnleashContext.builder().userId("11").build();

        Variant variant = new VariantUtil().selectVariant(toggle, context, DISABLED_VARIANT);

        assertThat(variant.getName(), is(v1.getName()));
        assertThat(variant.getPayload(), is(v1.getPayload()));
        assertThat(variant.isEnabled(), is(true));
    }

    @Test
    public void should_return_variant2() {
        VariantDefinition v1 = new VariantDefinition("a", 33, new Payload("string", "asd"), Collections.emptyList());
        VariantDefinition v2 = new VariantDefinitionBuilder("b", 33).build();
        VariantDefinition v3 = new VariantDefinitionBuilder("c", 34).build();

        FeatureToggle toggle = new FeatureToggle(
                "test.variants",
                true,
                asList(defaultStrategy), asList(v1, v2, v3));

        UnleashContext context = UnleashContext.builder().userId("163").build();

        Variant variant = new VariantUtil().selectVariant(toggle, context, DISABLED_VARIANT);

        assertThat(variant.getName(), is(v2.getName()));
    }

    @Test
    public void should_return_variant3() {
        VariantDefinition v1 = new VariantDefinitionBuilder("a", 33).build();
        VariantDefinition v2 = new VariantDefinitionBuilder("b", 33).build();
        VariantDefinition v3 = new VariantDefinitionBuilder("c", 34).build();

        FeatureToggle toggle = new FeatureToggle(
                "test.variants",
                true,
                asList(defaultStrategy), asList(v1, v2, v3));

        UnleashContext context = UnleashContext.builder().userId("40").build();

        Variant variant = new VariantUtil().selectVariant(toggle, context, DISABLED_VARIANT);

        assertThat(variant.getName(), is(v3.getName()));
    }

    @Test
    public void should_return_variant_override() {
        VariantDefinition v1 = new VariantDefinitionBuilder("a", 33).build();
        VariantOverride override = new VariantOverride("userId", asList("11", "12", "123", "44"));
        VariantDefinition v2 = new VariantDefinition("b", 33, null, asList(override));
        VariantDefinition v3 = new VariantDefinitionBuilder("c", 34).build();

        FeatureToggle toggle = new FeatureToggle(
                "test.variants",
                true,
                asList(defaultStrategy), asList(v1, v2, v3));

        UnleashContext context = UnleashContext.builder().userId("123").build();

        Variant variant = new VariantUtil().selectVariant(toggle, context, DISABLED_VARIANT);

        assertThat(variant.getName(), is(v2.getName()));
    }

    @Test
    public void should_return_variant_override_on_remote_adr() {
        VariantDefinition v1 = new VariantDefinition("a", 33, new Payload("string", "asd"), Collections.emptyList());
        VariantDefinition v2 = new VariantDefinition("b", 33, null, Collections.emptyList());
        VariantOverride override = new VariantOverride("remoteAddress", asList("11.11.11.11"));
        VariantDefinition v3 = new VariantDefinition("c", 34, new Payload("string", "blob"), asList(override));

        FeatureToggle toggle = new FeatureToggle(
                "test.variants",
                true,
                asList(defaultStrategy), asList(v1, v2, v3));

        UnleashContext context = UnleashContext.builder().remoteAddress("11.11.11.11").build();

        Variant variant = new VariantUtil().selectVariant(toggle, context, DISABLED_VARIANT);

        assertThat(variant.getName(), is(v3.getName()));
        assertThat(variant.getPayload(), is(v3.getPayload()));
        assertThat(variant.isEnabled(), is(true));
    }

    @Test
    public void should_return_variant_override_on_custom_prop() {
        VariantDefinition v1 = new VariantDefinitionBuilder("a", 33).build();
        VariantOverride override = new VariantOverride("env", asList("ci", "local", "dev"));
        VariantDefinition v2 = new VariantDefinition("b", 33, null, asList(override));
        VariantDefinition v3 = new VariantDefinitionBuilder("c", 34).build();

        FeatureToggle toggle = new FeatureToggle(
                "test.variants",
                true,
                asList(defaultStrategy), asList(v1, v2, v3));

        UnleashContext context = UnleashContext.builder()
                .userId("11")
                .addProperty("env", "dev")
                .build();

        Variant variant = new VariantUtil().selectVariant(toggle, context, DISABLED_VARIANT);

        assertThat(variant.getName(), is(v2.getName()));
    }

    @Test
    public void should_return_variant_override_on_sessionId() {
        String sessionId = "122221";

        VariantDefinition v1 = new VariantDefinitionBuilder("a", 33).build();
        VariantOverride override_env = new VariantOverride("env", asList("dev"));
        VariantOverride override_session = new VariantOverride("sessionId", asList(sessionId));
        VariantDefinition v2 = new VariantDefinition("b", 33, null, asList(override_env, override_session));
        VariantDefinition v3 = new VariantDefinitionBuilder("c", 34).build();

        FeatureToggle toggle = new FeatureToggle(
                "test.variants",
                true,
                asList(defaultStrategy), asList(v1, v2, v3));

        UnleashContext context = UnleashContext.builder()
                .userId("11")
                .addProperty("env", "prod")
                .sessionId(sessionId)
                .build();

        Variant variant = new VariantUtil().selectVariant(toggle, context, DISABLED_VARIANT);

        assertThat(variant.getName(), is(v2.getName()));
    }
}