package com.example.fooddelivery.ui.search;

import static org.junit.Assert.assertEquals;

import com.example.fooddelivery.data.model.FoodItem;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class MenuItemSearchFilterTest {

    @Test
    public void filtersMenuItemsByNameOnlyIgnoringDescription() {
        FoodItem pho = item(1, 10, "Pho bo", "Popular soup");
        FoodItem hidden = item(2, 11, "Com tam", "Pho appears only here");

        List<FoodItem> results = MenuItemSearchFilter.filterByName(Arrays.asList(pho, hidden), "pho");

        assertEquals(1, results.size());
        assertEquals(10, results.get(0).getRestaurantId());
    }

    @Test
    public void emptyQueryKeepsWhitespaceEmpty() {
        FoodItem pho = item(1, 10, "Pho bo", "Popular soup");

        List<FoodItem> results = MenuItemSearchFilter.filterByName(Arrays.asList(pho), " ");

        assertEquals(0, results.size());
    }

    private FoodItem item(long id, long restaurantId, String name, String description) {
        FoodItem item = new FoodItem(id, name, description, 0, 0, "");
        item.setRestaurantId(restaurantId);
        return item;
    }
}
