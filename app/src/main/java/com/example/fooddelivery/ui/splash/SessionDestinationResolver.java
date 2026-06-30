package com.example.fooddelivery.ui.splash;

import java.nio.charset.StandardCharsets;
import java.io.ByteArrayOutputStream;
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
                    decodeBase64Url(parts[1]),
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

    private byte[] decodeBase64Url(String value) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        int buffer = 0;
        int bitsCollected = 0;

        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            if (character == '=') {
                break;
            }

            int decoded = decodeBase64UrlCharacter(character);
            if (decoded < 0) {
                throw new IllegalArgumentException("Invalid base64url character");
            }

            buffer = (buffer << 6) | decoded;
            bitsCollected += 6;

            while (bitsCollected >= 8) {
                bitsCollected -= 8;
                output.write((buffer >> bitsCollected) & 0xFF);
            }
        }

        return output.toByteArray();
    }

    private int decodeBase64UrlCharacter(char character) {
        if (character >= 'A' && character <= 'Z') {
            return character - 'A';
        }
        if (character >= 'a' && character <= 'z') {
            return character - 'a' + 26;
        }
        if (character >= '0' && character <= '9') {
            return character - '0' + 52;
        }
        if (character == '-') {
            return 62;
        }
        if (character == '_') {
            return 63;
        }
        return -1;
    }
}
