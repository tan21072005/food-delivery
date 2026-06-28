package com.example.fooddelivery.data.model;

public class DeliveryAddress {
    private String id;
    private String type;
    private String recipientName;
    private String recipientPhone;
    private String fullAddress;
    private String buildingFloor;
    private String gate;
    private String customName;
    private String driverNote;
    private Double latitude;
    private Double longitude;
    private boolean isDefault;

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
