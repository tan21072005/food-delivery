package com.example.fooddelivery.data.local;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.fooddelivery.data.model.DeliveryAddress;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SharedPreferencesDeliveryAddressStore implements DeliveryAddressStore {
    private static final String PREFS = "delivery_addresses";
    private static final String KEY_ADDRESSES = "addresses_json";
    private static final String KEY_SELECTED_ID = "selected_id";

    private final SharedPreferences prefs;
    private final Gson gson = new Gson();
    private final Type listType = new TypeToken<List<DeliveryAddress>>() {}.getType();

    public SharedPreferencesDeliveryAddressStore(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    @Override
    public List<DeliveryAddress> load() {
        String json = prefs.getString(KEY_ADDRESSES, "[]");
        List<DeliveryAddress> addresses = gson.fromJson(json, listType);
        return addresses == null ? new ArrayList<>() : new ArrayList<>(addresses);
    }

    @Override
    public void save(List<DeliveryAddress> addresses) {
        prefs.edit().putString(KEY_ADDRESSES, gson.toJson(addresses)).apply();
    }

    @Override
    public String newId() {
        return UUID.randomUUID().toString();
    }

    @Override
    public String getSelectedId() {
        return prefs.getString(KEY_SELECTED_ID, null);
    }

    @Override
    public void setSelectedId(String id) {
        SharedPreferences.Editor editor = prefs.edit();
        if (id == null) {
            editor.remove(KEY_SELECTED_ID);
        } else {
            editor.putString(KEY_SELECTED_ID, id);
        }
        editor.apply();
    }
}
