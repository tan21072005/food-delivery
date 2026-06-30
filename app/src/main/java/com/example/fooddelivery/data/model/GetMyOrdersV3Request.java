package com.example.fooddelivery.data.model;

import com.google.gson.annotations.SerializedName;

public class GetMyOrdersV3Request {
    @SerializedName("p_status")
    private final String status;

    public GetMyOrdersV3Request(String status) {
        this.status = status;
    }
}
