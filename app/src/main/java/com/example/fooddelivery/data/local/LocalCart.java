package com.example.fooddelivery.data.local;

import com.example.fooddelivery.data.model.FoodItem;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * In-memory per-Restaurant cart fallback for DEV/demo.
 * Each restaurant owns one draft cart bucket.
 */
public class LocalCart {

    public static class CartEntry {
        public final FoodItem item;
        public int quantity;

        public CartEntry(FoodItem item, int quantity) {
            this.item = item;
            this.quantity = quantity;
        }

        public double subtotal() {
            return item.getPrice() * quantity;
        }
    }

    private static LocalCart instance;

    public static LocalCart getInstance() {
        if (instance == null) instance = new LocalCart();
        return instance;
    }

    private final Map<Long, List<CartEntry>> cartsByRestaurant = new LinkedHashMap<>();
    private long activeRestaurantId = -1L;

    private LocalCart() {}

    public void addItem(FoodItem item) {
        add(item, 1);
    }

    public void add(FoodItem item, int quantity) {
        if (item == null || quantity <= 0) return;

        long restaurantId = restaurantIdFor(item);
        activeRestaurantId = restaurantId;
        List<CartEntry> entries = entriesForRestaurant(restaurantId);
        for (CartEntry entry : entries) {
            if (entry.item.getId() == item.getId()) {
                entry.quantity += quantity;
                return;
            }
        }
        entries.add(new CartEntry(item, quantity));
    }

    public long getRestaurantId() {
        return activeRestaurantId;
    }

    public void setActiveRestaurantId(long restaurantId) {
        if (cartsByRestaurant.containsKey(restaurantId)) {
            activeRestaurantId = restaurantId;
        }
    }

    public boolean hasDifferentRestaurant(FoodItem item) {
        return false;
    }

    public void increase(long itemId) {
        increase(activeRestaurantId, itemId);
    }

    public void increase(long restaurantId, long itemId) {
        for (CartEntry entry : entriesForRestaurant(restaurantId)) {
            if (entry.item.getId() == itemId) {
                entry.quantity++;
                return;
            }
        }
    }

    public void decrease(long itemId) {
        decrease(activeRestaurantId, itemId);
    }

    public void decrease(long restaurantId, long itemId) {
        List<CartEntry> entries = entriesForRestaurant(restaurantId);
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).item.getId() == itemId) {
                entries.get(i).quantity--;
                if (entries.get(i).quantity <= 0) entries.remove(i);
                removeRestaurantIfEmpty(restaurantId);
                return;
            }
        }
    }

    public void clear() {
        cartsByRestaurant.clear();
        activeRestaurantId = -1L;
    }

    public void clearRestaurant(long restaurantId) {
        cartsByRestaurant.remove(normalizeRestaurantId(restaurantId));
        if (activeRestaurantId == normalizeRestaurantId(restaurantId)) {
            activeRestaurantId = cartsByRestaurant.isEmpty()
                    ? -1L
                    : cartsByRestaurant.keySet().iterator().next();
        }
    }

    public List<CartEntry> getEntries() {
        return getEntries(activeRestaurantId);
    }

    public List<CartEntry> getEntries(long restaurantId) {
        return new ArrayList<>(existingEntriesForRestaurant(restaurantId));
    }

    public List<Long> getRestaurantIds() {
        return new ArrayList<>(cartsByRestaurant.keySet());
    }

    public int getTotalCount() {
        return getTotalCount(activeRestaurantId);
    }

    public int getTotalCount(long restaurantId) {
        int total = 0;
        for (CartEntry entry : existingEntriesForRestaurant(restaurantId)) {
            total += entry.quantity;
        }
        return total;
    }

    public double getTotalPrice() {
        return getTotalPrice(activeRestaurantId);
    }

    public double getTotalPrice(long restaurantId) {
        double total = 0;
        for (CartEntry entry : existingEntriesForRestaurant(restaurantId)) {
            total += entry.subtotal();
        }
        return total;
    }

    public boolean isEmpty() {
        return cartsByRestaurant.isEmpty();
    }

    public boolean isEmpty(long restaurantId) {
        return existingEntriesForRestaurant(restaurantId).isEmpty();
    }

    private List<CartEntry> entriesForRestaurant(long restaurantId) {
        long normalizedRestaurantId = normalizeRestaurantId(restaurantId);
        List<CartEntry> entries = cartsByRestaurant.get(normalizedRestaurantId);
        if (entries == null) {
            entries = new ArrayList<>();
            cartsByRestaurant.put(normalizedRestaurantId, entries);
        }
        return entries;
    }

    private List<CartEntry> existingEntriesForRestaurant(long restaurantId) {
        List<CartEntry> entries = cartsByRestaurant.get(normalizeRestaurantId(restaurantId));
        return entries == null ? new ArrayList<>() : entries;
    }

    private long restaurantIdFor(FoodItem item) {
        return normalizeRestaurantId(item.getRestaurantId());
    }

    private long normalizeRestaurantId(long restaurantId) {
        return restaurantId > 0 ? restaurantId : -1L;
    }

    private void removeRestaurantIfEmpty(long restaurantId) {
        long normalizedRestaurantId = normalizeRestaurantId(restaurantId);
        List<CartEntry> entries = cartsByRestaurant.get(normalizedRestaurantId);
        if (entries != null && entries.isEmpty()) {
            clearRestaurant(normalizedRestaurantId);
        }
    }
}
