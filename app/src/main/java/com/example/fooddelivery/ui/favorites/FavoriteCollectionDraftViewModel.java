package com.example.fooddelivery.ui.favorites;

import androidx.lifecycle.ViewModel;

import com.example.fooddelivery.ui.favorites.model.FavoriteCollection;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public class FavoriteCollectionDraftViewModel extends ViewModel {
    private String id;
    private String name = "";
    private final Set<String> selectedIds = new LinkedHashSet<>();

    public void startNew() {
        id = UUID.randomUUID().toString();
        name = "";
        selectedIds.clear();
    }

    public void startEditing(FavoriteCollection collection) {
        id = collection.getId();
        name = collection.getName();
        selectedIds.clear();
        selectedIds.addAll(collection.getRestaurantIds());
    }

    public void setName(String value) { name = value == null ? "" : value; }
    public String getName() { return name; }
    public String getId() { return id; }
    public boolean isNameValid() { return !name.trim().isEmpty(); }
    public boolean isSelected(String restaurantId) { return selectedIds.contains(restaurantId); }
    public Set<String> getSelectedIds() { return new LinkedHashSet<>(selectedIds); }

    public void toggleRestaurant(String restaurantId) {
        if (!selectedIds.remove(restaurantId)) selectedIds.add(restaurantId);
    }

    public FavoriteCollection toCollection() {
        if (id == null) id = UUID.randomUUID().toString();
        return new FavoriteCollection(id, name, new ArrayList<>(selectedIds));
    }
}
