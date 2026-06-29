package com.example.fooddelivery.data.model;

import com.google.gson.annotations.SerializedName;

public class FoodItem {
    @SerializedName("id")
    private long id;

    @SerializedName("restaurant_id")
    private long restaurantId;

    @SerializedName("category_id")
    private long categoryId;

    @SerializedName("item_name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("price")
    private double price;

    @SerializedName("rating")
    private double rating;

    @SerializedName("image_url")
    private String imageUrl;

    @SerializedName("status")
    private String status;

    @SerializedName("sold_count")
    private int soldCount;

    // Kept for backward compatibility with older local mock UI, not from DB.
    private int imageResId;

    public FoodItem(long id, String name, String description,
                    int soldCount, double price, String imageUrl) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.soldCount = soldCount;
        this.price = price;
        this.imageUrl = imageUrl;
    }

    // --- Getters & Setters ---

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getRestaurantId() { return restaurantId; }
    public void setRestaurantId(long restaurantId) { this.restaurantId = restaurantId; }

    public long getCategoryId() { return categoryId; }
    public void setCategoryId(long categoryId) { this.categoryId = categoryId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getSoldCount() { return soldCount; }
    public void setSoldCount(int soldCount) { this.soldCount = soldCount; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public int getImageResId() { return imageResId; }
    public void setImageResId(int imageResId) { this.imageResId = imageResId; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    /** Returns formatted price string, e.g. "35.000đ" */
    public String getFormattedPrice() {
        long rounded = Math.round(price);
        // Format with dot thousands separator
        String raw = String.valueOf(rounded);
        StringBuilder sb = new StringBuilder();
        int offset = raw.length() % 3;
        for (int i = 0; i < raw.length(); i++) {
            if (i != 0 && (i - offset) % 3 == 0) sb.append('.');
            sb.append(raw.charAt(i));
        }
        return sb + "đ";
    }

    /** Returns sold count label, e.g. "14 đã bán" */
    public String getSoldCountLabel() {
        return soldCount + " đã bán";
    }
}
