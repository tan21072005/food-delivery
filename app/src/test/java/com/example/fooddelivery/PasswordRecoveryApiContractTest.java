package com.example.fooddelivery;

import static org.junit.Assert.assertEquals;

import com.example.fooddelivery.data.remote.apis.PasswordRecoveryApiService;
import com.example.fooddelivery.data.remote.request.PasswordRecoveryRequests;
import com.google.gson.Gson;

import org.junit.Test;

import retrofit2.http.POST;
import retrofit2.http.PUT;

public class PasswordRecoveryApiContractTest {

    @Test
    public void serviceUsesSupabaseRecoveryEndpoints() throws Exception {
        assertEquals("auth/v1/recover",
                PasswordRecoveryApiService.class.getMethod(
                                "sendCode", String.class, PasswordRecoveryRequests.Email.class)
                        .getAnnotation(POST.class).value());
        assertEquals("auth/v1/verify",
                PasswordRecoveryApiService.class.getMethod(
                                "verifyOtp", String.class, PasswordRecoveryRequests.VerifyOtp.class)
                        .getAnnotation(POST.class).value());
        assertEquals("auth/v1/user",
                PasswordRecoveryApiService.class.getMethod(
                                "updatePassword", String.class, PasswordRecoveryRequests.NewPassword.class)
                        .getAnnotation(PUT.class).value());
    }

    @Test
    public void requestsMatchSupabaseJsonContract() {
        Gson gson = new Gson();

        assertEquals("{\"email\":\"user@example.com\"}",
                gson.toJson(new PasswordRecoveryRequests.Email("user@example.com")));
        assertEquals("{\"email\":\"user@example.com\",\"token\":\"123456\",\"type\":\"recovery\"}",
                gson.toJson(new PasswordRecoveryRequests.VerifyOtp("user@example.com", "123456")));
        assertEquals("{\"password\":\"NewPassword1!\"}",
                gson.toJson(new PasswordRecoveryRequests.NewPassword("NewPassword1!")));
    }
}
