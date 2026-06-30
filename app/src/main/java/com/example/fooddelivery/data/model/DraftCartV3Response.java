package com.example.fooddelivery.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DraftCartV3Response {
    @SerializedName("cart_id")
    private long cartId;

    @SerializedName("restaurant_id")
    private long restaurantId;

    @SerializedName("restaurant_name")
    private String restaurantName;

    @SerializedName("restaurant_logo_url")
    private String restaurantLogoUrl;

    @SerializedName("restaurant_cover_url")
    private String restaurantCoverUrl;

    @SerializedName("restaurant_rating")
    private double restaurantRating;

    @SerializedName("restaurant_is_open")
    private boolean restaurantOpen;

    @SerializedName("item_count")
    private int itemCount;

    @SerializedName("line_count")
    private int lineCount;

    @SerializedName("subtotal")
    private double subtotal;

    @SerializedName("delivery_fee")
    private double deliveryFee;

    @SerializedName("discount_amount")
    private double discountAmount;

    @SerializedName("total_amount")
    private double totalAmount;

    @SerializedName("updated_at")
    private String updatedAt;

    @SerializedName("preview_items")
    private List<PreviewItem> previewItems;

    public long getCartId() {
        return cartId;
    }

    public void setCartId(long cartId) {
        this.cartId = cartId;
    }

    public long getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(long restaurantId) {
        this.restaurantId = restaurantId;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public String getRestaurantLogoUrl() {
        return restaurantLogoUrl;
    }

    public void setRestaurantLogoUrl(String restaurantLogoUrl) {
        this.restaurantLogoUrl = restaurantLogoUrl;
    }

    public String getRestaurantCoverUrl() {
        return restaurantCoverUrl;
    }

    public void setRestaurantCoverUrl(String restaurantCoverUrl) {
        this.restaurantCoverUrl = restaurantCoverUrl;
    }

    public double getRestaurantRating() {
        return restaurantRating;
    }

    public void setRestaurantRating(double restaurantRating) {
        this.restaurantRating = restaurantRating;
    }

    public boolean isRestaurantOpen() {
        return restaurantOpen;
    }

    public void setRestaurantOpen(boolean restaurantOpen) {
        this.restaurantOpen = restaurantOpen;
    }

    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }

    public int getLineCount() {
        return lineCount;
    }

    public void setLineCount(int lineCount) {
        this.lineCount = lineCount;
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

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<PreviewItem> getPreviewItems() {
        return previewItems;
    }

    public void setPreviewItems(List<PreviewItem> previewItems) {
        this.previewItems = previewItems;
    }

    public static class PreviewItem {
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

        @SerializedName("unit_price")
        private double unitPrice;

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

        public double getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(double unitPrice) {
            this.unitPrice = unitPrice;
        }

        public List<CartOptionV3Response> getOptions() {
            return options;
        }

        public void setOptions(List<CartOptionV3Response> options) {
            this.options = options;
        }
    }
}
