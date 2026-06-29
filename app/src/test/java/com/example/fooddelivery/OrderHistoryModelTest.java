package com.example.fooddelivery;

import static org.junit.Assert.assertEquals;

import com.example.fooddelivery.data.model.Order;

import org.junit.Test;

public class OrderHistoryModelTest {
    @Test public void orderExposesRestaurantHistoryFields() {
        Order order = new Order(1, "restaurant-7", "Guu Chicken", "Cơm gà", "Nhà",
                1, 65000, 7, "completed", 0, 1719550000000L);
        assertEquals("restaurant-7", order.getRestaurantId());
        assertEquals("Guu Chicken", order.getRestaurantName());
        assertEquals(1719550000000L, order.getCompletedAt());
    }
}
