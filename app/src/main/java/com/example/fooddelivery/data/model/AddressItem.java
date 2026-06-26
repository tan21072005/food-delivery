package com.example.fooddelivery.data.model;

public class AddressItem {
    private String id;
    private String label;
    private String detail;
    private String userInfo;
    private boolean isDefault;

    public AddressItem(String id, String label, String detail, String userInfo, boolean isDefault) {
        this.id = id;
        this.label = label;
        this.detail = detail;
        this.userInfo = userInfo;
        this.isDefault = isDefault;
    }

    public String getId() { return id; }
    public String getLabel() { return label; }
    public String getDetail() { return detail; }
    public String getUserInfo() { return userInfo; }
    public boolean isDefault() { return isDefault; }
}
