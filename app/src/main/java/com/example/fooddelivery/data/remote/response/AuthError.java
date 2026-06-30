package com.example.fooddelivery.data.remote.response;

import com.google.gson.annotations.SerializedName;

public class AuthError {
    @SerializedName("code") public String code;
    @SerializedName("error_description") public String errorDescription;
    @SerializedName("msg") public String msg;
    @SerializedName("message") public String message;
}
