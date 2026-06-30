package com.example.fooddelivery.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AddToCartV3Request {
    @SerializedName("p_menu_item_id")
    private long menuItemId;

    @SerializedName("p_quantity")
    private int quantity;

    @SerializedName("p_note")
    private String note;

    @SerializedName("p_option_choice_ids")
    private List<Long> optionChoiceIds;

    public AddToCartV3Request(long menuItemId, int quantity, String note, List<Long> optionChoiceIds) {
        this.menuItemId = menuItemId;
        this.quantity = quantity;
        this.note = note;
        this.optionChoiceIds = optionChoiceIds;
    }

    public long getMenuItemId() {
        return menuItemId;
    }

    public void setMenuItemId(long menuItemId) {
        this.menuItemId = menuItemId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public List<Long> getOptionChoiceIds() {
        return optionChoiceIds;
    }

    public void setOptionChoiceIds(List<Long> optionChoiceIds) {
        this.optionChoiceIds = optionChoiceIds;
    }
}
