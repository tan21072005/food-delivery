package com.example.fooddelivery.data.local;

import com.example.fooddelivery.data.model.DeliveryAddress;

import java.util.List;

public interface DeliveryAddressStore {
    List<DeliveryAddress> load();
    void save(List<DeliveryAddress> addresses);
    String newId();
    String getSelectedId();
    void setSelectedId(String id);
}
