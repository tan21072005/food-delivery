package com.example.fooddelivery.data.model;

import com.google.gson.annotations.SerializedName;

public class SetDefaultDeliveryAddressRequest {
    @SerializedName("p_delivery_address_id")
    private final long deliveryAddressId;

    public SetDefaultDeliveryAddressRequest(long deliveryAddressId) {
        this.deliveryAddressId = deliveryAddressId;
    }
}
