package com.example.fooddelivery.data.model;

public class Order {
    private int    id;
    private String restaurantId;
    private String restaurantName;
    private String foodName;
    private String tableInfo;
    private int    quantity;
    private long   totalPrice;
    private int    timeMinutes;
    private String status;        // "pending" | "completed" | "cancelled"
    private int    foodImageResId;
    private long   completedAt;

    public Order(int id, String foodName, String tableInfo,
                 int quantity, long totalPrice, int timeMinutes,
                 String status, int foodImageResId) {
        this(id, "unknown", "", foodName, tableInfo, quantity, totalPrice,
                timeMinutes, status, foodImageResId, 0L);
    }

    public Order(int id, String restaurantId, String restaurantName,
                 String foodName, String tableInfo, int quantity,
                 long totalPrice, int timeMinutes, String status,
                 int foodImageResId, long completedAt) {
        this.id             = id;
        this.restaurantId   = restaurantId;
        this.restaurantName = restaurantName;
        this.foodName       = foodName;
        this.tableInfo      = tableInfo;
        this.quantity       = quantity;
        this.totalPrice     = totalPrice;
        this.timeMinutes    = timeMinutes;
        this.status         = status;
        this.foodImageResId = foodImageResId;
        this.completedAt    = completedAt;
    }

    public int    getId()             { return id; }
    public String getRestaurantId()   { return restaurantId; }
    public String getRestaurantName() { return restaurantName; }
    public String getFoodName()       { return foodName; }
    public String getTableInfo()      { return tableInfo; }
    public int    getQuantity()       { return quantity; }
    public long   getTotalPrice()     { return totalPrice; }
    public int    getTimeMinutes()    { return timeMinutes; }
    public String getStatus()         { return status; }
    public int    getFoodImageResId() { return foodImageResId; }
    public long   getCompletedAt()    { return completedAt; }

    public String getQuantityAndPrice() {
        return quantity + " phần - "
                + String.format("%,d", totalPrice).replace(",", ".") + "đ";
    }
    public String getTimeLabel() { return timeMinutes + " phút"; }
}
