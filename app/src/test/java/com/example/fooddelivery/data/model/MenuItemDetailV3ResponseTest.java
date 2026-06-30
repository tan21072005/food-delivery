package com.example.fooddelivery.data.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;

import org.junit.Test;

public class MenuItemDetailV3ResponseTest {
    @Test
    public void gsonParsesMenuItemDetailWithOptionGroups() {
        String json = "{"
                + "\"id\":101,"
                + "\"restaurant_id\":7,"
                + "\"category_id\":3,"
                + "\"item_name\":\"Pizza\","
                + "\"description\":\"Cheesy\","
                + "\"image_url\":\"https://example.com/pizza.jpg\","
                + "\"price\":50000,"
                + "\"rating\":4.5,"
                + "\"status\":\"active\","
                + "\"sold_count\":12,"
                + "\"restaurant\":{\"id\":7,\"name\":\"Bep nha\",\"logo_url\":\"logo.png\",\"is_open\":true},"
                + "\"option_groups\":[{"
                + "\"option_group_id\":21,"
                + "\"name\":\"Size\","
                + "\"selection_type\":\"single\","
                + "\"min_select\":1,"
                + "\"max_select\":1,"
                + "\"is_required\":true,"
                + "\"choices\":[{"
                + "\"option_choice_id\":31,"
                + "\"name\":\"Large\","
                + "\"price_delta\":15000,"
                + "\"is_available\":true"
                + "}]"
                + "}]"
                + "}";

        MenuItemDetailV3Response response = new Gson().fromJson(json, MenuItemDetailV3Response.class);

        assertEquals(101, response.getId());
        assertEquals(7, response.getRestaurantId());
        assertEquals(3, response.getCategoryId());
        assertEquals("Pizza", response.getItemName());
        assertEquals(50000, response.getPrice(), 0.001);
        assertEquals(4.5, response.getRating(), 0.001);
        assertEquals(12, response.getSoldCount());
        assertNotNull(response.getRestaurant());
        assertEquals("Bep nha", response.getRestaurant().getName());
        assertTrue(response.getRestaurant().isOpen());

        assertEquals(1, response.getOptionGroups().size());
        MenuItemDetailV3Response.MenuOptionGroup group = response.getOptionGroups().get(0);
        assertEquals(21, group.getOptionGroupId());
        assertEquals("Size", group.getName());
        assertEquals("single", group.getSelectionType());
        assertEquals(1, group.getMinSelect());
        assertEquals(1, group.getMaxSelect());
        assertTrue(group.isRequired());

        assertEquals(1, group.getChoices().size());
        MenuItemDetailV3Response.MenuOptionChoice choice = group.getChoices().get(0);
        assertEquals(31, choice.getOptionChoiceId());
        assertEquals("Large", choice.getName());
        assertEquals(15000, choice.getPriceDelta(), 0.001);
        assertTrue(choice.isAvailable());
    }

    @Test
    public void emptyCollectionsAreNeverNull() {
        MenuItemDetailV3Response response = new Gson().fromJson("{\"id\":1}", MenuItemDetailV3Response.class);

        assertNotNull(response.getOptionGroups());
        assertTrue(response.getOptionGroups().isEmpty());

        MenuItemDetailV3Response.MenuOptionGroup group =
                new Gson().fromJson("{\"option_group_id\":2}", MenuItemDetailV3Response.MenuOptionGroup.class);
        assertNotNull(group.getChoices());
        assertTrue(group.getChoices().isEmpty());
        assertFalse(group.isRequired());
    }
}
