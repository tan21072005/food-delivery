package com.example.fooddelivery.data.model;

import com.google.gson.annotations.SerializedName;

public class CartSummaryV3Request {
    @SerializedName("p_cart_id")
    private long cartId;

    public CartSummaryV3Request(long cartId) {
        this.cartId = cartId;
    }

    public long getCartId() {
        return cartId;
    }

    public void setCartId(long cartId) {
        this.cartId = cartId;
    }
}
