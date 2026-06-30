package com.example.fooddelivery.ui.cart;

import com.example.fooddelivery.data.local.LocalCart;
import com.example.fooddelivery.data.model.CartOptionV3Response;
import com.example.fooddelivery.data.model.CartSummaryV3Response;
import com.example.fooddelivery.data.model.DraftCartV3Response;
import com.example.fooddelivery.data.model.FoodItem;

import java.util.ArrayList;
import java.util.List;

public final class RpcCartUiState {

    private RpcCartUiState() {
    }

    public static DraftCartV3Response selectActiveDraft(List<DraftCartV3Response> drafts,
                                                        long preferredRestaurantId) {
        if (drafts == null || drafts.isEmpty()) return null;

        DraftCartV3Response firstNonEmpty = null;
        for (DraftCartV3Response draft : drafts) {
            if (draft == null || draft.getCartId() <= 0) continue;
            if (draft.getItemCount() > 0 && firstNonEmpty == null) {
                firstNonEmpty = draft;
            }
            if (preferredRestaurantId > 0 && draft.getRestaurantId() == preferredRestaurantId) {
                return draft;
            }
        }
        return firstNonEmpty;
    }

    public static int itemCount(DraftCartV3Response draft) {
        return draft == null ? 0 : Math.max(0, draft.getItemCount());
    }

    public static int itemCount(CartSummaryV3Response summary) {
        if (summary == null || summary.getItems() == null) return 0;
        int count = 0;
        for (CartSummaryV3Response.Item item : summary.getItems()) {
            if (item != null) count += Math.max(0, item.getQuantity());
        }
        return count;
    }

    public static double totalAmount(DraftCartV3Response draft) {
        return draft == null ? 0 : Math.max(0, draft.getTotalAmount());
    }

    public static double totalAmount(CartSummaryV3Response summary) {
        return summary == null ? 0 : Math.max(0, summary.getTotalAmount());
    }

    public static List<LocalCart.CartEntry> mapSummaryItems(List<CartSummaryV3Response.Item> items) {
        List<LocalCart.CartEntry> entries = new ArrayList<>();
        if (items == null) return entries;

        for (CartSummaryV3Response.Item item : items) {
            if (item == null) continue;
            FoodItem foodItem = new FoodItem(
                    item.getMenuItemId(),
                    item.getItemName(),
                    optionSummary(item.getOptions(), item.getNote()),
                    0,
                    item.getBasePrice(),
                    item.getImageUrl()
            );
            entries.add(new LocalCart.CartEntry(foodItem, item.getQuantity(), item.getCartItemId()));
        }
        return entries;
    }

    public static String optionSummary(List<CartOptionV3Response> options, String note) {
        StringBuilder builder = new StringBuilder();
        if (options != null) {
            for (CartOptionV3Response option : options) {
                if (option == null || option.getName() == null || option.getName().trim().isEmpty()) {
                    continue;
                }
                if (builder.length() > 0) builder.append(", ");
                builder.append(option.getName().trim());
            }
        }
        if (note != null && !note.trim().isEmpty()) {
            if (builder.length() > 0) builder.append(", ");
            builder.append(note.trim());
        }
        return builder.length() == 0 ? "Tuy chon mac dinh" : builder.toString();
    }
}
