package com.example.fooddelivery;

import static org.junit.Assert.assertEquals;

import com.example.fooddelivery.data.model.Order;
import com.example.fooddelivery.ui.favorites.data.RestaurantSuggestionService;
import com.example.fooddelivery.ui.favorites.model.RestaurantSuggestion;

import org.junit.Test;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RestaurantSuggestionServiceTest {
    private final RestaurantSuggestionService service = new RestaurantSuggestionService();

    @Test public void ranksByFrequencyThenMostRecentCompletion() {
        List<Order> orders = Arrays.asList(completed("guu_chicken", 100), completed("lotteria", 400),
                completed("guu_chicken", 200), completed("kfc", 500));
        assertEquals(Arrays.asList("guu_chicken", "kfc", "lotteria"), ids(service.suggest(orders)));
    }

    @Test public void ignoresNonCompletedAndDeduplicatesRestaurants() {
        List<Order> orders = Arrays.asList(completed("guu_chicken", 100), completed("guu_chicken", 200),
                order("lotteria", "cancelled", 300));
        assertEquals(Arrays.asList("guu_chicken"), ids(service.suggest(orders)));
    }

    private Order completed(String id, long time) { return order(id, "completed", time); }
    private Order order(String id, String status, long time) {
        return new Order((int) time, id, id, "Món", "Nhà", 1, 1, 1, status, 0, time);
    }
    private List<String> ids(List<RestaurantSuggestion> values) {
        return values.stream().map(RestaurantSuggestion::getRestaurantId).collect(Collectors.toList());
    }
}
