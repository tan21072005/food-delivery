package com.example.fooddelivery.data.repository;

import android.content.Context;

import com.example.fooddelivery.data.remote.SupabaseClient;
import com.example.fooddelivery.data.remote.apis.AuthApiService;
import com.example.fooddelivery.data.remote.response.AuthRequest;
import com.example.fooddelivery.data.remote.response.AuthResponse;

import retrofit2.Call;

public class AuthRepository {
    private final AuthApiService authApiService;

    public AuthRepository(Context context) {
        authApiService = SupabaseClient.getInstance(context).create(AuthApiService.class);
    }

    public Call<AuthResponse> signUp(String email, String password) {
        AuthRequest request = new AuthRequest(email, password);
        return authApiService.signUp(request);
    }

    public Call<AuthResponse> signIn(String email, String password) {
        AuthRequest request = new AuthRequest(email, password);
        return authApiService.signIn(request);
    }
}
