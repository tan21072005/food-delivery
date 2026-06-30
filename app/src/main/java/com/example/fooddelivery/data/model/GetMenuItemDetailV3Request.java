package com.example.fooddelivery.data.model;

import com.google.gson.annotations.SerializedName;

public class GetMenuItemDetailV3Request {
    @SerializedName("p_menu_item_id")
    private final long menuItemId;

    public GetMenuItemDetailV3Request(long menuItemId) {
        this.menuItemId = menuItemId;
    }
}
