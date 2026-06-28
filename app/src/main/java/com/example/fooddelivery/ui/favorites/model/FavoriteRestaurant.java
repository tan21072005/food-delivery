package com.example.fooddelivery.ui.favorites.model;

import androidx.annotation.DrawableRes;

public final class FavoriteRestaurant {
    private final String id;
    private final String name;
    private final String rating;
    private final String sold;
    private final String distance;
    @DrawableRes private final int imageRes;

    public FavoriteRestaurant(String id, String name, String rating, String sold,
                              String distance, @DrawableRes int imageRes) {
        this.id = id;
        this.name = name;
        this.rating = rating;
        this.sold = sold;
        this.distance = distance;
        this.imageRes = imageRes;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getRating() { return rating; }
    public String getSold() { return sold; }
    public String getDistance() { return distance; }
    public int getImageRes() { return imageRes; }
}
