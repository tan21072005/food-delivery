package com.example.fooddelivery.data.remote.response;

import com.google.gson.annotations.SerializedName;

import kotlinx.serialization.Serializable;

// đóng gói dl và gủi lên superbase
 public  class AuthRequest{
     // @SerializedName khi convert sang Json dặt filde là email, passwword
    @SerializedName("email") private String email;
    @SerializedName("password") private  String password;

    // cóntructor để làm gì ?
     // để truyền email và pass
    public AuthRequest(String email, String password){
        this.email =email;
        this.password = password;
    }

}