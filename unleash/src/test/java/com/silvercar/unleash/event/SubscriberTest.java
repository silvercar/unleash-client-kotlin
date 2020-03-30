package com.silvercar.unleash.event;

import com.github.jenspiegsa.mockitoextension.ConfigureWireMock;
import com.github.jenspiegsa.mockitoextension.InjectServer;
import com.github.jenspiegsa.mockitoextension.WireMockExtension;
import com.github.jenspiegsa.mockitoextension.WireMockSettings;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.Options;
import com.silvercar.unleash.DefaultUnleash;
import com.silvercar.unleash.SynchronousTestExecutor;
import com.silvercar.unleash.Unleash;
import com.silvercar.unleash.UnleashException;
import com.silvercar.unleash.repository.FeatureToggleResponse;
import com.silvercar.unleash.util.UnleashConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.silvercar.unleash.repository.FeatureToggleResponse.Status.UNAVAILABLE;
import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(WireMockExtension.class)
@WireMockSettings(failOnUnmatchedRequests = false)
public class SubscriberTest {

    @ConfigureWireMock
    Options options = wireMockConfig().dynamicPort();

    @InjectServer
    WireMockServer serverMock;

    private TestSubscriber testSubscriber = new TestSubscriber();
    private UnleashConfig unleashConfig;

    @BeforeEach
    void setup() {
        unleashConfig = new UnleashConfig.Builder()
                .appName(SubscriberTest.class.getSimpleName())
                .instanceId(SubscriberTest.class.getSimpleName())
                .synchronousFetchOnInitialisation(true)
                .unleashAPI("http://localhost:" + serverMock.port())
                .subscriber(testSubscriber)
                .scheduledExecutor(new SynchronousTestExecutor())
                .build();
    }

    @Test
    void subscriberAreNotified() {
        Unleash unleash = new DefaultUnleash(unleashConfig);

        unleash.isEnabled("myFeature");
        unleash.isEnabled("myFeature");
        unleash.isEnabled("myFeature");

        assertThat(testSubscriber.togglesFetchedCounter).isEqualTo(2); // one forced, one scheduled
        assertThat(testSubscriber.status).isEqualTo(UNAVAILABLE);
        assertThat(testSubscriber.toggleEvalutatedCounter).isEqualTo(3);
        assertThat(testSubscriber.toggleName).isEqualTo("myFeature");
        assertThat(testSubscriber.toggleEnabled).isFalse();
        assertThat(testSubscriber.errors).hasSize(2);

        assertThat(testSubscriber.events).hasSize(3 // feature evaluations
                + 2 // toggle fetches
                + 1 // unleash ready
                + 1 // client registration
                + 1 // client metrics
        );
    }

    private class TestSubscriber implements UnleashSubscriber {

        private int togglesFetchedCounter;
        private FeatureToggleResponse.Status status;

        private int toggleEvalutatedCounter;
        private String toggleName;
        private boolean toggleEnabled;

        private List<UnleashEvent> events = new ArrayList<>();
        private List<UnleashException> errors = new ArrayList<>();

        @Override
        public void on(UnleashEvent unleashEvent) {
            this.events.add(unleashEvent);
        }

        @Override
        public void onError(UnleashException unleashException) {
            this.errors.add(unleashException);
        }

        @Override
        public void toggleEvaluated(ToggleEvaluated toggleEvaluated) {
            this.toggleEvalutatedCounter++;
            this.toggleName = toggleEvaluated.getToggleName();
            this.toggleEnabled = toggleEvaluated.isEnabled();
        }

        @Override
        public void togglesFetched(FeatureToggleResponse toggleResponse) {
            this.togglesFetchedCounter++;
            this.status = toggleResponse.getStatus();
        }
    }

}
