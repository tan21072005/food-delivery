package com.example.fooddelivery.data.repository;

import com.example.fooddelivery.data.model.Order;
import java.util.List;

public interface OrderHistoryRepository {
    List<Order> getCompletedOrders();
    void save(Order order);
}
