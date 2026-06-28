package com.example.fooddelivery.ui.favorites.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.fooddelivery.ui.favorites.model.FavoriteCollection;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public final class FavoriteCollectionStore {
    private static final String PREFS = "favorite_collections";
    private static final String KEY = "collections_json";
    private static final Type LIST_TYPE = new TypeToken<List<FavoriteCollection>>() {}.getType();
    private final SharedPreferences preferences;
    private final Gson gson = new Gson();

    public FavoriteCollectionStore(Context context) {
        preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public List<FavoriteCollection> getAll() {
        String json = preferences.getString(KEY, null);
        if (json == null) return new ArrayList<>();
        try {
            List<FavoriteCollection> values = gson.fromJson(json, LIST_TYPE);
            return values == null ? new ArrayList<>() : new ArrayList<>(values);
        } catch (JsonParseException exception) {
            return new ArrayList<>();
        }
    }

    public FavoriteCollection findById(String id) {
        for (FavoriteCollection collection : getAll()) {
            if (collection.getId().equals(id)) return collection;
        }
        return null;
    }

    public void save(FavoriteCollection value) {
        List<FavoriteCollection> values = getAll();
        values.removeIf(item -> item.getId().equals(value.getId()));
        values.add(value);
        preferences.edit().putString(KEY, gson.toJson(values)).apply();
    }

    public boolean rename(String id, String name) {
        if (name == null || name.trim().isEmpty()) return false;
        List<FavoriteCollection> values = getAll();
        for (int index = 0; index < values.size(); index++) {
            FavoriteCollection item = values.get(index);
            if (item.getId().equals(id)) {
                values.set(index, new FavoriteCollection(item.getId(), name, item.getRestaurantIds()));
                write(values);
                return true;
            }
        }
        return false;
    }

    public boolean delete(String id) {
        List<FavoriteCollection> values = getAll();
        boolean changed = values.removeIf(item -> item.getId().equals(id));
        if (changed) write(values);
        return changed;
    }

    private void write(List<FavoriteCollection> values) {
        preferences.edit().putString(KEY, gson.toJson(values)).apply();
    }
}
