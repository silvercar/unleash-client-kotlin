package com.silvercar.unleash;


import com.silvercar.unleash.util.UnleashConfig;
import com.silvercar.unleash.util.UnleashConfigBuilder;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class UnleashContextTest {

    @Test
    public void should_generate_default_context() {
        UnleashContext context = UnleashContext.builder().build();
        assertThat(context.getUserId(), is(""));
        assertThat(context.getSessionId(), is(""));
        assertThat(context.getRemoteAddress(), is(""));
        assertThat(context.getProperties().size(), is(0));
    }

    @Test
    public void should_get_context_with_userId() {
        UnleashContext context = UnleashContext.builder()
                .userId("test@mail.com")
                .build();
        assertThat(context.getUserId(), is("test@mail.com"));
    }

    @Test
    public void should_get_context_with_fields_set() {
        UnleashContext context = UnleashContext.builder()
                .userId("test@mail.com")
                .sessionId("123")
                .remoteAddress("127.0.0.1")
                .environment("prod")
                .appName("myapp")
                .addProperty("test", "me")
                .build();

        assertThat(context.getUserId(), is("test@mail.com"));
        assertThat(context.getSessionId(), is("123"));
        assertThat(context.getRemoteAddress(), is("127.0.0.1"));
        assertThat(context.getEnvironment(), is("prod"));
        assertThat(context.getAppName(), is("myapp"));
        assertThat(context.getProperties().get("test"), is("me"));
    }

    @Test
    public void should_apply_context_fields() {
        UnleashContext context = UnleashContext.builder()
                .userId("test@mail.com")
                .sessionId("123")
                .remoteAddress("127.0.0.1")
                .addProperty("test", "me")
                .build();

        UnleashConfig config = new UnleashConfigBuilder()
                .unleashAPI("http://test.com")
                .appName("someApp")
                .environment("stage")
                .build();

        UnleashContext enhanced = context.applyStaticFields(config);

        assertThat(enhanced.getUserId(), is("test@mail.com"));
        assertThat(enhanced.getSessionId(), is("123"));
        assertThat(enhanced.getRemoteAddress(), is("127.0.0.1"));

        assertThat(enhanced.getEnvironment(), is("stage"));
        assertThat(enhanced.getAppName(), is("someApp"));
    }

    @Test
    public void should_not_override_static_context_fields() {
        UnleashContext context = UnleashContext.builder()
                .userId("test@mail.com")
                .sessionId("123")
                .remoteAddress("127.0.0.1")
                .environment("env")
                .appName("myApp")
                .addProperty("test", "me")
                .build();

        UnleashConfig config = new UnleashConfigBuilder()
                .unleashAPI("http://test.com")
                .appName("someApp")
                .environment("stage")
                .build();


        UnleashContext enhanced = context.applyStaticFields(config);

        assertThat(enhanced.getUserId(), is("test@mail.com"));
        assertThat(enhanced.getSessionId(), is("123"));
        assertThat(enhanced.getRemoteAddress(), is("127.0.0.1"));
        assertThat(enhanced.getEnvironment(), is("env"));
        assertThat(enhanced.getAppName(), is("myApp"));
    }

}
