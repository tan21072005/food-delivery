package com.example.fooddelivery.ui.favorites.model;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public final class FavoriteCollection {
    private final String id;
    private final String name;
    private final List<String> restaurantIds;

    public FavoriteCollection(String id, String name, List<String> restaurantIds) {
        this.id = id;
        this.name = name == null ? "" : name.trim();
        this.restaurantIds = new ArrayList<>(new LinkedHashSet<>(restaurantIds));
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public List<String> getRestaurantIds() { return new ArrayList<>(restaurantIds); }
}
