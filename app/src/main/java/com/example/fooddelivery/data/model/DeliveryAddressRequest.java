package com.example.fooddelivery.data.model;

import com.google.gson.annotations.SerializedName;

public class DeliveryAddressRequest {
    @SerializedName("label")
    private String label;

    @SerializedName("recipient_name")
    private String recipientName;

    @SerializedName("recipient_phone")
    private String recipientPhone;

    @SerializedName("address_detail")
    private String fullAddress;

    @SerializedName("latitude")
    private Double latitude;

    @SerializedName("longitude")
    private Double longitude;

    @SerializedName("is_default")
    private Boolean isDefault;

    @SerializedName("deleted_at")
    private String deletedAt;

    public DeliveryAddressRequest(String label, String recipientName, String recipientPhone,
                                  String fullAddress, Double latitude, Double longitude,
                                  Boolean isDefault) {
        this.label = label;
        this.recipientName = recipientName;
        this.recipientPhone = recipientPhone;
        this.fullAddress = fullAddress;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isDefault = isDefault;
    }

    public static DeliveryAddressRequest softDelete(String deletedAt) {
        DeliveryAddressRequest request = new DeliveryAddressRequest(null, null, null, null, null, null, null);
        request.deletedAt = deletedAt;
        return request;
    }
}
