package com.example.fooddelivery.data.model;

import com.google.gson.annotations.SerializedName;

public class CartRequest {
    @SerializedName("user_id")
    private long userId;

    @SerializedName("menu_id")
    private long menuId;

    @SerializedName("quantity")
    private int quantity;

    public CartRequest(long userId, long menuId, int quantity) {
        this.userId = userId;
        this.menuId = menuId;
        this.quantity = quantity;
    }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public long getMenuId() { return menuId; }
    public void setMenuId(long menuId) { this.menuId = menuId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
