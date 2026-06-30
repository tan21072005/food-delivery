package com.example.fooddelivery.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CartSummaryV3Response {
    @SerializedName("cart_id")
    private long cartId;

    @SerializedName("items")
    private List<Item> items;

    @SerializedName("subtotal")
    private double subtotal;

    @SerializedName("delivery_fee")
    private double deliveryFee;

    @SerializedName("discount_amount")
    private double discountAmount;

    @SerializedName("total_amount")
    private double totalAmount;

    public long getCartId() {
        return cartId;
    }

    public void setCartId(long cartId) {
        this.cartId = cartId;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getDeliveryFee() {
        return deliveryFee;
    }

    public void setDeliveryFee(double deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(double discountAmount) {
        this.discountAmount = discountAmount;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public static class Item {
        @SerializedName("cart_item_id")
        private long cartItemId;

        @SerializedName("menu_item_id")
        private long menuItemId;

        @SerializedName("item_name")
        private String itemName;

        @SerializedName("image_url")
        private String imageUrl;

        @SerializedName("quantity")
        private int quantity;

        @SerializedName("base_price")
        private double basePrice;

        @SerializedName("note")
        private String note;

        @SerializedName("options")
        private List<CartOptionV3Response> options;

        public long getCartItemId() {
            return cartItemId;
        }

        public void setCartItemId(long cartItemId) {
            this.cartItemId = cartItemId;
        }

        public long getMenuItemId() {
            return menuItemId;
        }

        public void setMenuItemId(long menuItemId) {
            this.menuItemId = menuItemId;
        }

        public String getItemName() {
            return itemName;
        }

        public void setItemName(String itemName) {
            this.itemName = itemName;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public double getBasePrice() {
            return basePrice;
        }

        public void setBasePrice(double basePrice) {
            this.basePrice = basePrice;
        }

        public String getNote() {
            return note;
        }

        public void setNote(String note) {
            this.note = note;
        }

        public List<CartOptionV3Response> getOptions() {
            return options;
        }

        public void setOptions(List<CartOptionV3Response> options) {
            this.options = options;
        }
    }
}
