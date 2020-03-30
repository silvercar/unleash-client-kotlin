package com.silvercar.unleash.strategy;

import com.annimon.stream.Stream;
import com.silvercar.unleash.UnleashContext;
import com.silvercar.unleash.util.IpAddressMatcher;

import java.util.Arrays;
import java.util.Map;
import java.util.regex.Pattern;

import com.annimon.stream.Optional;

public final class RemoteAddressStrategy implements Strategy {
    static final String PARAM = "IPs";
    private static final String STRATEGY_NAME = "remoteAddress";
    private static final Pattern SPLITTER = Pattern.compile(",");

    @Override
    public String getName() {
        return STRATEGY_NAME;
    }

    @Override
    public boolean isEnabled(Map<String, String> parameters) {
        return false;
    }

    @Override
    public boolean isEnabled(Map<String, String> parameters, UnleashContext context) {
        return Optional.ofNullable(parameters.get(PARAM))
                .map(ips -> Arrays.asList(SPLITTER.split(ips, -1)))
                .map(ips -> Stream.of(ips)
                        .flatMap(ipAddress -> buildIpAddressMatcher(ipAddress)
                                .map(Stream::of)
                                .orElseGet(Stream::empty))
                        .map(subnet -> context.getRemoteAddress()
                                .map(subnet::matches)
                                .orElse(false))
                        .anyMatch(Boolean.TRUE::equals))
                .orElse(false);
    }

    private Optional<IpAddressMatcher> buildIpAddressMatcher(String ipAddress) {
        try {
            return Optional.of(new IpAddressMatcher(ipAddress));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}
