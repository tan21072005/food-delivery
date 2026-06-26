package com.example.fooddelivery.data.model;

import com.google.gson.annotations.SerializedName;

public class CheckoutRequest {
    @SerializedName("p_delivery_address")
    private String deliveryAddress;

    @SerializedName("p_note")
    private String note;

    public CheckoutRequest(String deliveryAddress, String note) {
        this.deliveryAddress = deliveryAddress;
        this.note = note;
    }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
