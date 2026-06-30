package com.example.fooddelivery.data.model;

public class CheckoutCartV3Response {
    private long orderId;

    public CheckoutCartV3Response(long orderId) {
        this.orderId = orderId;
    }

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }
}
