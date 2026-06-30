package com.example.fooddelivery.data.model;

import com.google.gson.annotations.SerializedName;

public class RemoveCartItemV3Request {
    @SerializedName("p_cart_item_id")
    private long cartItemId;

    public RemoveCartItemV3Request(long cartItemId) {
        this.cartItemId = cartItemId;
    }

    public long getCartItemId() {
        return cartItemId;
    }

    public void setCartItemId(long cartItemId) {
        this.cartItemId = cartItemId;
    }
}
