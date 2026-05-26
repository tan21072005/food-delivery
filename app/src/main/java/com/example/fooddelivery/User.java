package com.example.fooddelivery;

import com.google.gson.annotations.SerializedName;

// User.java
public class User {
    @SerializedName("id")
    private int id;

    @SerializedName("username")
    private String username;

    @SerializedName("email")
    private String email;

    @SerializedName("full_name")
    private String fullName;

    // getters & setters...
}