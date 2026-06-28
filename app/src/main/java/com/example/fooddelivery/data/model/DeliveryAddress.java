package com.example.fooddelivery.data.model;

import com.google.gson.annotations.SerializedName;

public class DeliveryAddress {
    @SerializedName("id")
    private Long id;

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
    private boolean isDefault;

    @SerializedName("deleted_at")
    private String deletedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
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

    public String getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(String deletedAt) {
        this.deletedAt = deletedAt;
    }

    public String toCheckoutDisplayText() {
        String person = joinNonEmpty(" - ", recipientName, recipientPhone);
        if (person.isEmpty()) {
            return safe(fullAddress);
        }
        String address = safe(fullAddress);
        return address.isEmpty() ? person : person + "\n" + address;
    }

    public String toSingleLineDisplayText() {
        String checkoutText = toCheckoutDisplayText();
        return checkoutText.replace('\n', ' ').trim();
    }

    private String joinNonEmpty(String separator, String first, String second) {
        String safeFirst = safe(first);
        String safeSecond = safe(second);
        if (safeFirst.isEmpty()) {
            return safeSecond;
        }
        if (safeSecond.isEmpty()) {
            return safeFirst;
        }
        return safeFirst + separator + safeSecond;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
