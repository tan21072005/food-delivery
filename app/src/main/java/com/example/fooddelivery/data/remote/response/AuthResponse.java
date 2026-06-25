package com.example.fooddelivery.data.remote.response;


import com.google.gson.annotations.SerializedName;

import io.github.jan.supabase.gotrue.user.UserInfo;

public class AuthResponse {
    @SerializedName("access_token") public String accessToken;
    @SerializedName("refresh_token") public String refreshToken;
    @SerializedName("user") public UserInfo user;

    public static class UserInfo{
        @SerializedName("id") public String id ;
        @SerializedName("email") public String email;
    }
}