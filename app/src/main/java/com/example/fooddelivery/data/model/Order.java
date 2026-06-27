package com.example.fooddelivery.data.model;

public class Order {
    private int    id;
    private String foodName;
    private String tableInfo;
    private int    quantity;
    private long   totalPrice;
    private int    timeMinutes;
    private String status;        // "pending" | "completed" | "cancelled"
    private int    foodImageResId;
    private boolean isReviewed;
    private String orderDate;

    public Order(int id, String foodName, String tableInfo,
                 int quantity, long totalPrice, int timeMinutes,
                 String status, int foodImageResId, boolean isReviewed, String orderDate) {
        this.id             = id;
        this.foodName       = foodName;
        this.tableInfo      = tableInfo;
        this.quantity       = quantity;
        this.totalPrice     = totalPrice;
        this.timeMinutes    = timeMinutes;
        this.status         = status;
        this.foodImageResId = foodImageResId;
        this.isReviewed     = isReviewed;
        this.orderDate      = orderDate;
    }

    public int    getId()             { return id; }
    public String getFoodName()       { return foodName; }
    public String getTableInfo()      { return tableInfo; }
    public int    getQuantity()       { return quantity; }
    public long   getTotalPrice()     { return totalPrice; }
    public int    getTimeMinutes()    { return timeMinutes; }
    public String getStatus()         { return status; }
    public int    getFoodImageResId() { return foodImageResId; }
    public boolean isReviewed()       { return isReviewed; }
    public void setReviewed(boolean r) { this.isReviewed = r; }
    public String getOrderDate()      { return orderDate; }

    public String getQuantityAndPrice() {
        return quantity + " phần - "
                + String.format("%,d", totalPrice).replace(",", ".") + "đ";
    }
    public String getTimeLabel() { return timeMinutes + " phút"; }
}
