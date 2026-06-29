package com.example.fooddelivery.ui.favorites.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.fooddelivery.ui.favorites.model.FavoriteCollection;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public final class FavoriteCollectionStore {
    private static final String PREFS = "favorite_collections";
    private static final String KEY = "collections_json";
    private static final Type LIST_TYPE = new TypeToken<List<FavoriteCollection>>() {}.getType();
    private static final int CURRENT_VERSION = 1;
    private final SharedPreferences preferences;
    private final Gson gson = new Gson();

    public FavoriteCollectionStore(Context context) {
        preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public List<FavoriteCollection> getAll() {
        String json = preferences.getString(KEY, null);
        if (json == null) return new ArrayList<>();
        try {
            JsonElement root = new JsonParser().parse(json);
            List<FavoriteCollection> values;
            if (root.isJsonArray()) {
                values = gson.fromJson(root, LIST_TYPE);
            } else {
                StoredCollections stored = gson.fromJson(root, StoredCollections.class);
                if (stored == null || stored.version != CURRENT_VERSION) return new ArrayList<>();
                values = stored.collections;
            }
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
        write(values);
    }

    public boolean rename(String id, String name) {
        if (name == null || name.trim().isEmpty() || name.trim().length() > 60) return false;
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
        preferences.edit().putString(KEY,
                gson.toJson(new StoredCollections(CURRENT_VERSION, values))).apply();
    }

    private static final class StoredCollections {
        final int version;
        final List<FavoriteCollection> collections;
        StoredCollections(int version, List<FavoriteCollection> collections) {
            this.version = version;
            this.collections = new ArrayList<>(collections);
        }
    }
}
