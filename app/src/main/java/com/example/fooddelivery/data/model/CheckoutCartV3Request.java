package com.example.fooddelivery.data.model;

import com.google.gson.annotations.SerializedName;

public class CheckoutCartV3Request {
    @SerializedName("p_cart_id")
    private long cartId;

    @SerializedName("p_delivery_address_id")
    private long deliveryAddressId;

    @SerializedName("p_payment_method")
    private String paymentMethod;

    @SerializedName("p_note")
    private String note;

    public CheckoutCartV3Request(long cartId, long deliveryAddressId, String paymentMethod, String note) {
        this.cartId = cartId;
        this.deliveryAddressId = deliveryAddressId;
        this.paymentMethod = paymentMethod;
        this.note = note;
    }

    public long getCartId() {
        return cartId;
    }

    public void setCartId(long cartId) {
        this.cartId = cartId;
    }

    public long getDeliveryAddressId() {
        return deliveryAddressId;
    }

    public void setDeliveryAddressId(long deliveryAddressId) {
        this.deliveryAddressId = deliveryAddressId;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
