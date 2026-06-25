package com.example.fooddelivery.data.remote.response;

import com.google.gson.annotations.SerializedName;

public class AuthError{
    @SerializedName("eror_description") public String errorDescription;
    @SerializedName("msg") public String msg;

}