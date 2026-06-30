package com.example.fooddelivery.data.remote.apis;

import com.example.fooddelivery.data.remote.request.PasswordRecoveryRequests;
import com.example.fooddelivery.data.remote.response.AuthResponse;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public interface PasswordRecoveryApiService {
    @POST("auth/v1/recover")
    Call<Void> sendCode(
            @Header("Authorization") String authorization,
            @Body PasswordRecoveryRequests.Email request);

    @POST("auth/v1/verify")
    Call<AuthResponse> verifyOtp(
            @Header("Authorization") String authorization,
            @Body PasswordRecoveryRequests.VerifyOtp request);

    @PUT("auth/v1/user")
    Call<ResponseBody> updatePassword(
            @Header("Authorization") String authorization,
            @Body PasswordRecoveryRequests.NewPassword request);
}
