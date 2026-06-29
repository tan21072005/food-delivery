package com.example.fooddelivery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import com.example.fooddelivery.data.local.LocalCart;
import com.example.fooddelivery.data.local.LocalOrderStore;
import com.example.fooddelivery.data.model.FoodItem;
import com.example.fooddelivery.data.model.Order;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class PerRestaurantCartTest {

    private final LocalCart cart = LocalCart.getInstance();

    @Before
    public void resetCart() {
        cart.clear();
    }

    @Test
    public void addingItemsFromDifferentRestaurantsCreatesSeparateDraftCarts() {
        FoodItem phoA = food(101, 1, "Pho bo", 45_000);
        FoodItem bunA = food(102, 1, "Bun cha", 55_000);
        FoodItem pizzaB = food(201, 2, "Pizza", 90_000);

        cart.add(phoA, 1);
        cart.add(phoA, 1);
        cart.add(bunA, 1);
        cart.add(pizzaB, 1);

        List<Order> draftOrders = LocalOrderStore.getInstance().getDraftOrders();

        assertEquals(2, draftOrders.size());
        assertEquals("Pho bo + 1 món khác", draftOrders.get(0).getFoodName());
        assertEquals(3, draftOrders.get(0).getQuantity());
        assertEquals(145_000, draftOrders.get(0).getTotalPrice());
        assertEquals("Pizza", draftOrders.get(1).getFoodName());
        assertEquals(1, draftOrders.get(1).getQuantity());
        assertEquals(90_000, draftOrders.get(1).getTotalPrice());
    }

    @Test
    public void addingDifferentRestaurantDoesNotRequireDeletingExistingCart() {
        cart.add(food(101, 1, "Pho bo", 45_000), 1);

        assertFalse(cart.hasDifferentRestaurant(food(201, 2, "Pizza", 90_000)));
    }

    private FoodItem food(long id, long restaurantId, String name, double price) {
        FoodItem item = new FoodItem(id, name, "", 0, price, "");
        item.setRestaurantId(restaurantId);
        return item;
    }
}
