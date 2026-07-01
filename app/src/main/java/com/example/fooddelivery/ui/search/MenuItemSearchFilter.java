package com.example.fooddelivery.ui.search;

import com.example.fooddelivery.data.model.FoodItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class MenuItemSearchFilter {

    private MenuItemSearchFilter() {
    }

    public static List<FoodItem> filterByName(List<FoodItem> source, String query) {
        List<FoodItem> results = new ArrayList<>();
        if (source == null || query == null || query.trim().isEmpty()) {
            return results;
        }

        String normalizedQuery = query.trim().toLowerCase(Locale.ROOT);
        for (FoodItem item : source) {
            String name = item.getName() == null ? "" : item.getName().toLowerCase(Locale.ROOT);
            if (name.contains(normalizedQuery)) {
                results.add(item);
            }
        }
        return results;
    }
}
