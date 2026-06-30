package com.example.fooddelivery.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MyOrderV3Response {
    @SerializedName("order_id")
    private long orderId;

    @SerializedName("cart_id")
    private Long cartId;

    @SerializedName("restaurant_id")
    private long restaurantId;

    @SerializedName("restaurant_name")
    private String restaurantName;

    @SerializedName("restaurant_logo_url")
    private String restaurantLogoUrl;

    @SerializedName("status")
    private String status;

    @SerializedName("subtotal")
    private double subtotal;

    @SerializedName("delivery_fee")
    private double deliveryFee;

    @SerializedName("discount_amount")
    private double discountAmount;

    @SerializedName("total_amount")
    private double totalAmount;

    @SerializedName("payment_method")
    private String paymentMethod;

    @SerializedName("payment_status")
    private String paymentStatus;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    @SerializedName("item_count")
    private int itemCount;

    @SerializedName("preview_items")
    private List<PreviewItem> previewItems;

    public long getOrderId() {
        return orderId;
    }

    public Long getCartId() {
        return cartId;
    }

    public long getRestaurantId() {
        return restaurantId;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public String getRestaurantLogoUrl() {
        return restaurantLogoUrl;
    }

    public String getStatus() {
        return status;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public double getDeliveryFee() {
        return deliveryFee;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public int getItemCount() {
        return itemCount;
    }

    public List<PreviewItem> getPreviewItems() {
        return previewItems;
    }

    public static class PreviewItem {
        @SerializedName("item_name")
        private String itemName;

        @SerializedName("quantity")
        private int quantity;

        @SerializedName("image_url")
        private String imageUrl;

        public String getItemName() {
            return itemName;
        }

        public int getQuantity() {
            return quantity;
        }

        public String getImageUrl() {
            return imageUrl;
        }
    }
}
