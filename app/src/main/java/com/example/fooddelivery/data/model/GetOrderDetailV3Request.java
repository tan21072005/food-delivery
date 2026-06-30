package com.example.fooddelivery.data.model;

import com.google.gson.annotations.SerializedName;

public class GetOrderDetailV3Request {
    @SerializedName("p_order_id")
    private final long orderId;

    public GetOrderDetailV3Request(long orderId) {
        this.orderId = orderId;
    }
}
