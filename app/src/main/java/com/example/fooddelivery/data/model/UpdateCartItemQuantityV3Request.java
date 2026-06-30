package com.example.fooddelivery.data.model;

import com.google.gson.annotations.SerializedName;

public class UpdateCartItemQuantityV3Request {
    @SerializedName("p_cart_item_id")
    private long cartItemId;

    @SerializedName("p_quantity")
    private int quantity;

    public UpdateCartItemQuantityV3Request(long cartItemId, int quantity) {
        this.cartItemId = cartItemId;
        this.quantity = quantity;
    }

    public long getCartItemId() {
        return cartItemId;
    }

    public void setCartItemId(long cartItemId) {
        this.cartItemId = cartItemId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
