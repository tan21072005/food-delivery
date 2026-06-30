package com.example.fooddelivery.data.model;

import com.google.gson.annotations.SerializedName;

public class RestaurantInfo {
    @SerializedName("id")
    private long id;

    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("address")
    private String address;

    @SerializedName("logo_url")
    private String logoUrl;

    @SerializedName("cover_url")
    private String coverUrl;

    @SerializedName("avg_rating")
    private Double avgRating;

    @SerializedName("total_reviews")
    private Integer totalReviews;

    @SerializedName("is_open")
    private Boolean open;

    @SerializedName("status")
    private String status;

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getAddress() {
        return address;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public double getAvgRating() {
        return avgRating == null ? 0 : avgRating;
    }

    public int getTotalReviews() {
        return totalReviews == null ? 0 : totalReviews;
    }

    public boolean isOpen() {
        return Boolean.TRUE.equals(open);
    }

    public String getStatus() {
        return status;
    }
}
