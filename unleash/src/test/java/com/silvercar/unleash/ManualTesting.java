package com.silvercar.unleash;

import com.silvercar.unleash.event.ToggleEvaluated;
import com.silvercar.unleash.event.UnleashEvent;
import com.silvercar.unleash.event.UnleashSubscriber;
import com.silvercar.unleash.metric.ClientMetrics;
import com.silvercar.unleash.metric.ClientRegistration;
import java.util.Map;
import java.util.Random;

import com.silvercar.unleash.event.UnleashReady;
import com.silvercar.unleash.repository.FeatureToggleResponse;
import com.silvercar.unleash.repository.ToggleCollection;
import com.silvercar.unleash.strategy.Strategy;
import com.silvercar.unleash.util.UnleashConfig;
import org.jetbrains.annotations.NotNull;

public class ManualTesting {
    public static void main(String[] args) throws Exception {
        Strategy strategy = new Strategy() {
            @Override
            public String getName() {
                return "ActiveForUserWithId";
            }

            @Override
            public boolean isEnabled(Map<String, String> parameters) {
                System.out.println("parameters = " + parameters);
                return true;
            }
        };
        UnleashConfig unleashConfig = new UnleashConfig.Builder()
                .appName("java-test")
                .instanceId("instance y")
                .unleashAPI("https://unleash.herokuapp.com/api/")
                .subscriber(new UnleashSubscriber() {
                    @Override
                    public void onReady(UnleashReady ready) {
                        System.out.println("Unleash is ready");
                    }

                    @Override
                    public void togglesFetched(FeatureToggleResponse response) {
                        System.out.println("Fetch toggles with status: " + response.getStatus());
                    }

                    @Override
                    public void togglesBackedUp(ToggleCollection toggleCollection) {
                        System.out.println("Backup stored.");
                    }

                    @Override
                    public void toggleBackupRestored(ToggleCollection toggleCollection) {
                        System.out.println("Backup read.");
                    }

                    @Override
                    public void onError(UnleashException exception) {
                        System.err.println(exception);
                    }

                    @Override
                    public void clientRegistered(@NotNull ClientRegistration clientRegistration) { }

                    @Override public void clientMetrics(@NotNull ClientMetrics metrics) { }

                    @Override public void toggleEvaluated(@NotNull ToggleEvaluated evaluated) { }

                    @Override public void on(@NotNull UnleashEvent event) { }
                })
                .fetchTogglesInterval(10)
                .sendMetricsInterval(10)
                .unleashContextProvider(() -> UnleashContext.builder()
                        .sessionId(new Random().nextInt(10000) + "")
                        .userId(new Random().nextInt(10000) + "")
                        .remoteAddress("192.168.1.1")
                        .build())
                .build();

        Unleash unleash = new DefaultUnleash(unleashConfig, strategy);

        for(int i=0;i<100;i++) {
            (new Thread(new UnleashThread(unleash, "thread-"+i, 100))).start();
        }
    }

    static class UnleashThread implements Runnable {

        final Unleash unleash;
        final String name;
        final int maxRounds;
        int currentRound = 0;

        UnleashThread(Unleash unleash, String name, int maxRounds) {
            this.unleash = unleash;
            this.name = name;
            this.maxRounds = maxRounds;
        }

        public void run() {
            while(currentRound < maxRounds) {
                currentRound++;
                long startTime = System.nanoTime();

                boolean enabled = unleash.isEnabled("Demo");
                long timeUsed = System.nanoTime() - startTime;

                System.out.println(name + "\t" +"Demo" +":"  + enabled + "\t " + timeUsed + "ns");

                try {
                    //Wait 1 to 10ms before next round
                    Thread.sleep(new Random().nextInt(10000));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
