package com.example.fooddelivery.data.model;

import com.google.gson.annotations.SerializedName;

public class CartQuantityRequest {
    @SerializedName("quantity")
    private int quantity;

    public CartQuantityRequest(int quantity) {
        this.quantity = quantity;
    }

    public int getQuantity() {
        return quantity;
    }
}
