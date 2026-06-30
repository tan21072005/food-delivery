package com.example.fooddelivery.ui.home.options;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.fooddelivery.data.model.MenuItemDetailV3Response;
import com.google.gson.Gson;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class MenuOptionSelectionStateTest {
    @Test
    public void requiredSingleGroupDefaultsToFirstAvailableChoiceAndValidates() {
        MenuOptionSelectionState state = new MenuOptionSelectionState(50000, 1,
                groups("[{\"option_group_id\":1,\"name\":\"Size\",\"selection_type\":\"single\","
                        + "\"min_select\":1,\"max_select\":1,\"is_required\":true,"
                        + "\"choices\":["
                        + "{\"option_choice_id\":10,\"name\":\"M\",\"price_delta\":0,\"is_available\":true},"
                        + "{\"option_choice_id\":11,\"name\":\"L\",\"price_delta\":10000,\"is_available\":true}"
                        + "]}]"));

        assertTrue(state.validate().isValid());
        assertEquals(Arrays.asList(10L), state.getSelectedOptionChoiceIds());
    }

    @Test
    public void requiredSingleGroupWithoutAvailableChoiceIsInvalid() {
        MenuOptionSelectionState state = new MenuOptionSelectionState(50000, 1,
                groups("[{\"option_group_id\":1,\"name\":\"Size\",\"selection_type\":\"single\","
                        + "\"min_select\":1,\"max_select\":1,\"is_required\":true,"
                        + "\"choices\":[]}]"));

        MenuOptionSelectionState.ValidationResult result = state.validate();

        assertFalse(result.isValid());
        assertEquals("Vui long chon Size", result.getMessage());
    }

    @Test
    public void multipleGroupDoesNotAllowMoreThanMaxSelect() {
        MenuOptionSelectionState state = new MenuOptionSelectionState(50000, 1,
                groups("[{\"option_group_id\":2,\"name\":\"Topping\",\"selection_type\":\"multiple\","
                        + "\"min_select\":0,\"max_select\":2,\"is_required\":false,"
                        + "\"choices\":["
                        + "{\"option_choice_id\":20,\"name\":\"Cheese\",\"price_delta\":5000,\"is_available\":true},"
                        + "{\"option_choice_id\":21,\"name\":\"Egg\",\"price_delta\":7000,\"is_available\":true},"
                        + "{\"option_choice_id\":22,\"name\":\"Beef\",\"price_delta\":12000,\"is_available\":true}"
                        + "]}]"));

        assertTrue(state.toggleMultiple(2, 20));
        assertTrue(state.toggleMultiple(2, 21));
        assertFalse(state.toggleMultiple(2, 22));
        assertEquals(Arrays.asList(20L, 21L), state.getSelectedOptionChoiceIds());
    }

    @Test
    public void totalPriceIncludesSelectedPriceDeltaAndQuantity() {
        MenuOptionSelectionState state = new MenuOptionSelectionState(50000, 2,
                groups("["
                        + "{\"option_group_id\":1,\"name\":\"Size\",\"selection_type\":\"single\","
                        + "\"min_select\":1,\"max_select\":1,\"is_required\":true,"
                        + "\"choices\":["
                        + "{\"option_choice_id\":10,\"name\":\"M\",\"price_delta\":0,\"is_available\":true},"
                        + "{\"option_choice_id\":11,\"name\":\"L\",\"price_delta\":10000,\"is_available\":true}"
                        + "]},"
                        + "{\"option_group_id\":2,\"name\":\"Topping\",\"selection_type\":\"multiple\","
                        + "\"min_select\":0,\"max_select\":2,\"is_required\":false,"
                        + "\"choices\":["
                        + "{\"option_choice_id\":20,\"name\":\"Cheese\",\"price_delta\":5000,\"is_available\":true}"
                        + "]}"
                        + "]"));

        assertTrue(state.selectSingle(1, 11));
        assertTrue(state.toggleMultiple(2, 20));

        assertEquals(130000, state.getTotalPrice(), 0.001);
    }

    @Test
    public void totalPriceUpdatesWhenQuantityChangesWithoutLosingSelections() {
        MenuOptionSelectionState state = new MenuOptionSelectionState(50000, 1,
                groups("[{\"option_group_id\":1,\"name\":\"Size\",\"selection_type\":\"single\","
                        + "\"min_select\":1,\"max_select\":1,\"is_required\":true,"
                        + "\"choices\":["
                        + "{\"option_choice_id\":10,\"name\":\"M\",\"price_delta\":0,\"is_available\":true},"
                        + "{\"option_choice_id\":11,\"name\":\"L\",\"price_delta\":10000,\"is_available\":true}"
                        + "]}]"));

        state.selectSingle(1, 11);
        state.setQuantity(3);

        assertEquals(Arrays.asList(11L), state.getSelectedOptionChoiceIds());
        assertEquals(180000, state.getTotalPrice(), 0.001);
    }

    @Test
    public void selectedOptionIdsFollowGroupAndChoiceOrderForAddCart() {
        MenuOptionSelectionState state = new MenuOptionSelectionState(50000, 1,
                groups("["
                        + "{\"option_group_id\":1,\"name\":\"Size\",\"selection_type\":\"single\","
                        + "\"min_select\":1,\"max_select\":1,\"is_required\":true,"
                        + "\"choices\":["
                        + "{\"option_choice_id\":10,\"name\":\"M\",\"price_delta\":0,\"is_available\":true},"
                        + "{\"option_choice_id\":11,\"name\":\"L\",\"price_delta\":10000,\"is_available\":true}"
                        + "]},"
                        + "{\"option_group_id\":2,\"name\":\"Topping\",\"selection_type\":\"multiple\","
                        + "\"min_select\":0,\"max_select\":2,\"is_required\":false,"
                        + "\"choices\":["
                        + "{\"option_choice_id\":20,\"name\":\"Cheese\",\"price_delta\":5000,\"is_available\":true},"
                        + "{\"option_choice_id\":21,\"name\":\"Egg\",\"price_delta\":7000,\"is_available\":true}"
                        + "]}"
                        + "]"));

        state.selectSingle(1, 11);
        state.toggleMultiple(2, 21);
        state.toggleMultiple(2, 20);

        assertEquals(Arrays.asList(11L, 20L, 21L), state.getSelectedOptionChoiceIds());
    }

    private List<MenuItemDetailV3Response.MenuOptionGroup> groups(String json) {
        MenuItemDetailV3Response response = new Gson().fromJson(
                "{\"option_groups\":" + json + "}",
                MenuItemDetailV3Response.class
        );
        return response.getOptionGroups();
    }
}
