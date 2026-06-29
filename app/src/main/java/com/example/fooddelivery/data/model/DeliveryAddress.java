package com.example.fooddelivery.data.model;

import com.google.gson.annotations.SerializedName;

public class DeliveryAddress {
    @SerializedName("id")
    private String id;

    @SerializedName("label")
    private String type;

    @SerializedName("receiver_name")
    private String recipientName;

    @SerializedName("receiver_phone")
    private String recipientPhone;

    @SerializedName("address_line")
    private String fullAddress;

    @SerializedName("floor")
    private String buildingFloor;

    @SerializedName("gate_note")
    private String gate;

    private String customName;
    private String driverNote;

    @SerializedName("latitude")
    private Double latitude;

    @SerializedName("longitude")
    private Double longitude;

    @SerializedName("is_default")
    private boolean isDefault;

    @SerializedName("customer_id")
    private long customerId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getRecipientPhone() {
        return recipientPhone;
    }

    public void setRecipientPhone(String recipientPhone) {
        this.recipientPhone = recipientPhone;
    }

    public String getFullAddress() {
        return fullAddress;
    }

    public void setFullAddress(String fullAddress) {
        this.fullAddress = fullAddress;
    }

    public String getBuildingFloor() {
        return buildingFloor;
    }

    public void setBuildingFloor(String buildingFloor) {
        this.buildingFloor = buildingFloor;
    }

    public String getGate() {
        return gate;
    }

    public void setGate(String gate) {
        this.gate = gate;
    }

    public String getCustomName() {
        return customName;
    }

    public void setCustomName(String customName) {
        this.customName = customName;
    }

    public String getDriverNote() {
        return driverNote;
    }

    public void setDriverNote(String driverNote) {
        this.driverNote = driverNote;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    public long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(long customerId) {
        this.customerId = customerId;
    }

    public String getDisplayLabel() {
        if ("Khac".equals(type) && customName != null && !customName.trim().isEmpty()) {
            return customName.trim();
        }
        return type == null || type.trim().isEmpty() ? "Nha" : type;
    }

    public String getRecipientLine() {
        String name = recipientName == null ? "" : recipientName.trim();
        String phone = recipientPhone == null ? "" : recipientPhone.trim();
        if (name.isEmpty()) return phone;
        if (phone.isEmpty()) return name;
        return name + " - " + phone;
    }
}
