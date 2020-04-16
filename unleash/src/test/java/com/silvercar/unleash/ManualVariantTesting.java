package com.silvercar.unleash;

import java.util.Map;
import java.util.Random;

import com.silvercar.unleash.strategy.Strategy;
import com.silvercar.unleash.util.UnleashConfig;
import org.jetbrains.annotations.NotNull;

public class ManualVariantTesting {
    public static void main(String[] args) throws Exception {
        Strategy strategy = new Strategy() {

            @Override public String getName() {
                return "ActiveForUserWithId";
            }

            @Override public boolean isEnabled(@NotNull Map<String, String> parameters) {
                System.out.println("parameters = " + parameters);
                return true;
            }

            @Override public boolean isEnabled(@NotNull Map<String, String> parameters,
                @NotNull UnleashContext unleashContext) {
                return isEnabled(parameters);
            }
        };
        UnleashConfig unleashConfig = new UnleashConfig.Builder()
                .appName("java-test")
                .instanceId("instance y")
                .unleashAPI("https://unleash.herokuapp.com/api/")
                .fetchTogglesInterval(1)
                .sendMetricsInterval(2)
                .unleashContextProvider(() -> UnleashContext.builder()
                        .sessionId(new Random().nextInt(10000) + "")
                        .userId(new Random().nextInt(10000) + "")
                        .remoteAddress("192.168.1.1")
                        .build())
                .build();

        Unleash unleash = new DefaultUnleash(unleashConfig, strategy);

        (new Thread(new UnleashThread(unleash, "thread-1", 10000))).start();
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

                Variant variant = unleash.getVariant("Test.variants");
                long timeUsed = System.nanoTime() - startTime;



                System.out.println(name + "\t" +"Test.variants" +":"  + variant.getName() + "\t " + timeUsed + "ns");

                try {
                    //Wait 1 to 10ms before next round
                    Thread.sleep(new Random().nextInt(100));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
