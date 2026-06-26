package com.example.fooddelivery.data.repository;

import android.content.Context;

import com.example.fooddelivery.data.model.User;
import com.example.fooddelivery.data.remote.SupabaseClient;
import com.example.fooddelivery.data.remote.apis.ApiService;

import java.util.List;

import okhttp3.RequestBody;
import retrofit2.Call;

public class UserRepository {
    private final ApiService apiService;

    public UserRepository(Context context) {
        apiService = SupabaseClient.getInstance(context).create(ApiService.class);
    }

    public Call<List<User>> getUserById(String idFilter) {
        return apiService.getUserById(idFilter);
    }

    public Call<List<User>> getUserByAuthUid(String authUidFilter) {
        return apiService.getUserByAuthUid(authUidFilter);
    }

    public Call<List<User>> getUserByEmail(String emailFilter) {
        return apiService.getUserByEmail(emailFilter);
    }

    public Call<Void> uploadFile(String bucket, String fileName, RequestBody file) {
        return apiService.uploadFile(bucket, fileName, file);
    }

    public Call<Void> updateUser(String idFilter, User user) {
        return apiService.updateUser(idFilter, user);
    }
}
