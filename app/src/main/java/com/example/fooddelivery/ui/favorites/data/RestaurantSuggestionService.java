package com.example.fooddelivery.ui.favorites.data;

import com.example.fooddelivery.data.model.Order;
import com.example.fooddelivery.ui.favorites.model.FavoriteRestaurant;
import com.example.fooddelivery.ui.favorites.model.RestaurantSuggestion;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class RestaurantSuggestionService {
    public List<RestaurantSuggestion> suggest(List<Order> orders) {
        Map<String, Stats> statsByRestaurant = new LinkedHashMap<>();
        for (Order order : orders) {
            if (!"completed".equals(order.getStatus())) continue;
            FavoriteRestaurant restaurant = FavoriteRestaurantCatalog.findById(order.getRestaurantId());
            if (restaurant == null) continue;
            Stats stats = statsByRestaurant.computeIfAbsent(order.getRestaurantId(), ignored -> new Stats());
            stats.count++;
            stats.latest = Math.max(stats.latest, order.getCompletedAt());
        }
        List<RestaurantSuggestion> result = new ArrayList<>();
        for (Map.Entry<String, Stats> entry : statsByRestaurant.entrySet()) {
            FavoriteRestaurant restaurant = FavoriteRestaurantCatalog.findById(entry.getKey());
            Stats stats = entry.getValue();
            result.add(new RestaurantSuggestion(restaurant, stats.count, stats.latest));
        }
        result.sort(Comparator.comparingInt(RestaurantSuggestion::getCompletedOrderCount).reversed()
                .thenComparing(Comparator.comparingLong(RestaurantSuggestion::getLatestCompletedAt).reversed()));
        return result;
    }

    private static final class Stats { int count; long latest; }
}
