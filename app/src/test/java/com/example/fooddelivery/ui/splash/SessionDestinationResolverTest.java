package com.example.fooddelivery.ui.splash;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class SessionDestinationResolverTest {
    private final SessionDestinationResolver resolver = new SessionDestinationResolver();

    private String jwt(long expiresAtEpochSeconds) {
        String header = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"alg\":\"none\"}".getBytes(StandardCharsets.UTF_8));
        String payload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(("{\"exp\":" + expiresAtEpochSeconds + "}")
                        .getBytes(StandardCharsets.UTF_8));
        return header + "." + payload + ".signature";
    }

    @Test
    public void missingTokenGoesToAuth() {
        assertEquals(LaunchDestination.AUTH, resolver.resolve(null, 1_000L));
    }

    @Test
    public void futureExpiryGoesToMain() {
        assertEquals(LaunchDestination.MAIN, resolver.resolve(jwt(2_000L), 1_000L));
    }

    @Test
    public void expiryAtCurrentSecondGoesToAuth() {
        assertEquals(LaunchDestination.AUTH, resolver.resolve(jwt(1_000L), 1_000L));
    }

    @Test
    public void malformedTokenGoesToAuth() {
        assertEquals(LaunchDestination.AUTH, resolver.resolve("not-a-jwt", 1_000L));
    }
}
