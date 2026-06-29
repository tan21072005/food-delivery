package com.example.fooddelivery.ui.favorites.model;

public final class RestaurantSuggestion {
    private final FavoriteRestaurant restaurant;
    private final int completedOrderCount;
    private final long latestCompletedAt;

    public RestaurantSuggestion(FavoriteRestaurant restaurant, int completedOrderCount, long latestCompletedAt) {
        this.restaurant = restaurant;
        this.completedOrderCount = completedOrderCount;
        this.latestCompletedAt = latestCompletedAt;
    }

    public FavoriteRestaurant getRestaurant() { return restaurant; }
    public String getRestaurantId() { return restaurant.getId(); }
    public int getCompletedOrderCount() { return completedOrderCount; }
    public long getLatestCompletedAt() { return latestCompletedAt; }
}
