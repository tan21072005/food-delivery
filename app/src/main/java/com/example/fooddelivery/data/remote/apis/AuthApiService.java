package com.example.fooddelivery.data.remote.apis;


import com.example.fooddelivery.data.remote.response.AuthRequest;
import com.example.fooddelivery.data.remote.response.AuthResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApiService {
    @POST("auth/v1/signup")
    Call<AuthResponse> signUp(@Body AuthRequest request);

    @POST("auth/v1/token?grant_type=password")
    Call<AuthResponse> signIn(@Body AuthRequest request);
}