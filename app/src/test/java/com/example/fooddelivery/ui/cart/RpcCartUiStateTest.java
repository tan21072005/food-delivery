package com.example.fooddelivery.ui.cart;

import static org.junit.Assert.assertEquals;

import com.example.fooddelivery.data.local.LocalCart;
import com.example.fooddelivery.data.model.CartSummaryV3Response;
import com.example.fooddelivery.data.model.DraftCartV3Response;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public class RpcCartUiStateTest {

    @Test
    public void selectActiveDraftPrefersRequestedRestaurant() {
        DraftCartV3Response other = draft(8L, 200L, 1, 45000);
        DraftCartV3Response current = draft(9L, 300L, 3, 120000);

        DraftCartV3Response selected = RpcCartUiState.selectActiveDraft(
                Arrays.asList(other, current),
                300L
        );

        assertEquals(9L, selected.getCartId());
        assertEquals(3, RpcCartUiState.itemCount(selected));
        assertEquals(120000, Math.round(RpcCartUiState.totalAmount(selected)));
    }

    @Test
    public void mapSummaryItemsIncludesNoteInDescription() {
        CartSummaryV3Response.Item item = new CartSummaryV3Response.Item();
        item.setMenuItemId(44L);
        item.setItemName("Com ga");
        item.setBasePrice(50000);
        item.setQuantity(2);
        item.setNote("It cay");

        LocalCart.CartEntry entry = RpcCartUiState.mapSummaryItems(
                Collections.singletonList(item)
        ).get(0);

        assertEquals(44L, entry.item.getId());
        assertEquals("Com ga", entry.item.getName());
        assertEquals("It cay", entry.item.getDescription());
        assertEquals(2, entry.quantity);
    }

    private DraftCartV3Response draft(long cartId, long restaurantId, int itemCount, double total) {
        DraftCartV3Response draft = new DraftCartV3Response();
        draft.setCartId(cartId);
        draft.setRestaurantId(restaurantId);
        draft.setItemCount(itemCount);
        draft.setTotalAmount(total);
        return draft;
    }
}
