package com.example.fooddelivery;

import com.google.gson.annotations.SerializedName;

public class AuthError{
    @SerializedName("eror_description") public String errorDescription;
    @SerializedName("msg") public String msg;

}