package com.example.fooddelivery.data.repository;

import android.content.Context;

import com.example.fooddelivery.data.model.DeliveryAddress;
import com.example.fooddelivery.data.model.DeliveryAddressRequest;
import com.example.fooddelivery.data.model.SetDefaultDeliveryAddressRequest;
import com.example.fooddelivery.data.remote.SupabaseClient;
import com.example.fooddelivery.data.remote.apis.ApiService;

import java.util.List;

import retrofit2.Call;

public class DeliveryAddressRepository {
    private static final String SELECT_FIELDS = "id,label,recipient_name,recipient_phone,address_detail,latitude,longitude,is_default,deleted_at";

    private final ApiService apiService;

    public DeliveryAddressRepository(Context context) {
        apiService = SupabaseClient.getInstance(context).create(ApiService.class);
    }

    public Call<List<DeliveryAddress>> list() {
        return apiService.getDeliveryAddresses(SELECT_FIELDS, "is.null", "is_default.desc,updated_at.desc");
    }

    public Call<Void> create(DeliveryAddressRequest request) {
        return apiService.createDeliveryAddress(request);
    }

    public Call<Void> update(long id, DeliveryAddressRequest request) {
        return apiService.updateDeliveryAddress("eq." + id, request);
    }

    public Call<Void> softDelete(long id) {
        return apiService.softDeleteDeliveryAddress("eq." + id, DeliveryAddressRequest.softDelete(nowIso()));
    }

    public Call<Void> setDefault(long id) {
        return apiService.setDefaultDeliveryAddress(new SetDefaultDeliveryAddressRequest(id));
    }

    private String nowIso() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US)
                .format(new java.util.Date());
    }
}
