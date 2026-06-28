package com.example.fooddelivery.data.model;

import com.google.gson.annotations.SerializedName;

public class CheckoutRequest {
    @SerializedName("p_delivery_address_id")
    private Long deliveryAddressId;

    @SerializedName("p_note")
    private String note;

    public CheckoutRequest(Long deliveryAddressId, String note) {
        this.deliveryAddressId = deliveryAddressId;
        this.note = note;
    }

    public Long getDeliveryAddressId() {
        return deliveryAddressId;
    }

    public void setDeliveryAddressId(Long deliveryAddressId) {
        this.deliveryAddressId = deliveryAddressId;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
