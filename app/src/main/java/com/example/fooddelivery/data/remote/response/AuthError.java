package com.example.fooddelivery.data.remote.response;

import com.google.gson.annotations.SerializedName;

public class AuthError{
    @SerializedName("error_description") public String errorDescription;
    @SerializedName("msg") public String msg;
    @SerializedName("message") public String message;
    @SerializedName("error") public String error;

}
