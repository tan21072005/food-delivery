package com.example.fooddelivery.ui.splash;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SessionDestinationResolver {
    private static final Pattern EXPIRY_PATTERN =
            Pattern.compile("\"exp\"\\s*:\\s*(\\d+)");

    public LaunchDestination resolve(String token, long nowEpochSeconds) {
        if (token == null || token.trim().isEmpty()) {
            return LaunchDestination.AUTH;
        }

        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return LaunchDestination.AUTH;
            }

            String payload = new String(
                    Base64.getUrlDecoder().decode(parts[1]),
                    StandardCharsets.UTF_8
            );
            Matcher matcher = EXPIRY_PATTERN.matcher(payload);
            if (!matcher.find()) {
                return LaunchDestination.AUTH;
            }

            long expiresAtEpochSeconds = Long.parseLong(matcher.group(1));
            return expiresAtEpochSeconds > nowEpochSeconds
                    ? LaunchDestination.MAIN
                    : LaunchDestination.AUTH;
        } catch (RuntimeException ignored) {
            return LaunchDestination.AUTH;
        }
    }
}
