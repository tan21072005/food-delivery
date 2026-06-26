package com.example.fooddelivery.data.model;

import com.google.gson.annotations.SerializedName;

public class CartAddRequest {
    @SerializedName("p_menu_id")
    private long menuId;

    @SerializedName("p_quantity")
    private int quantity;

    public CartAddRequest(long menuId, int quantity) {
        this.menuId = menuId;
        this.quantity = quantity;
    }

    public long getMenuId() {
        return menuId;
    }

    public void setMenuId(long menuId) {
        this.menuId = menuId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
