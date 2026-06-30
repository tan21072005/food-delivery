package com.example.fooddelivery.data.model;

import com.google.gson.annotations.SerializedName;

public class ClearCartV3Request {
    @SerializedName("p_cart_id")
    private long cartId;

    public ClearCartV3Request(long cartId) {
        this.cartId = cartId;
    }

    public long getCartId() {
        return cartId;
    }

    public void setCartId(long cartId) {
        this.cartId = cartId;
    }
}
