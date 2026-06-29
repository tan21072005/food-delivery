package com.example.fooddelivery.data.local;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.fooddelivery.data.model.Order;
import com.example.fooddelivery.data.repository.OrderHistoryRepository;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public final class SharedPreferencesOrderHistoryRepository implements OrderHistoryRepository {
    private static final String PREFS = "order_history";
    private static final String KEY = "orders_json";
    private static final Type TYPE = new TypeToken<List<Order>>() {}.getType();
    private static final int CURRENT_VERSION = 1;
    private final SharedPreferences preferences;
    private final Gson gson = new Gson();

    public SharedPreferencesOrderHistoryRepository(Context context) {
        preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    @Override public List<Order> getCompletedOrders() {
        List<Order> completed = new ArrayList<>();
        for (Order order : readAll()) {
            String restaurantId = order.getRestaurantId();
            if ("completed".equals(order.getStatus()) && restaurantId != null
                    && !restaurantId.trim().isEmpty() && !"unknown".equals(restaurantId)) {
                completed.add(order);
            }
        }
        return completed;
    }

    @Override public void save(Order order) {
        List<Order> values = readAll();
        values.removeIf(existing -> existing.getId() == order.getId());
        values.add(order);
        preferences.edit().putString(KEY,
                gson.toJson(new StoredOrders(CURRENT_VERSION, values))).apply();
    }

    private List<Order> readAll() {
        String json = preferences.getString(KEY, null);
        if (json == null) return new ArrayList<>();
        try {
            JsonElement root = new JsonParser().parse(json);
            List<Order> values;
            if (root.isJsonArray()) {
                values = gson.fromJson(root, TYPE);
            } else {
                StoredOrders stored = gson.fromJson(root, StoredOrders.class);
                if (stored == null || stored.version != CURRENT_VERSION) return new ArrayList<>();
                values = stored.orders;
            }
            return values == null ? new ArrayList<>() : new ArrayList<>(values);
        } catch (JsonParseException exception) {
            return new ArrayList<>();
        }
    }

    private static final class StoredOrders {
        final int version;
        final List<Order> orders;
        StoredOrders(int version, List<Order> orders) {
            this.version = version;
            this.orders = new ArrayList<>(orders);
        }
    }
}
