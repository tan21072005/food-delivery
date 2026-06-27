package com.example.fooddelivery.data.local;

import com.example.fooddelivery.data.model.FoodItem;

import java.util.ArrayList;
import java.util.List;

/**
 * In-memory singleton cart — không cần Supabase, dùng trong DEV/demo.
 * Mỗi CartEntry lưu FoodItem + quantity.
 */
public class LocalCart {

    public static class CartEntry {
        public final FoodItem item;
        public int quantity;

        public CartEntry(FoodItem item, int quantity) {
            this.item = item;
            this.quantity = quantity;
        }

        public double subtotal() {
            return item.getPrice() * quantity;
        }
    }

    // ── Singleton ──────────────────────────────────────────────────
    private static LocalCart instance;

    public static LocalCart getInstance() {
        if (instance == null) instance = new LocalCart();
        return instance;
    }

    // ── Data ───────────────────────────────────────────────────────
    private final List<CartEntry> entries = new ArrayList<>();

    private LocalCart() {}

    // ── Operations ─────────────────────────────────────────────────

    /** Thêm 1 đơn vị FoodItem vào giỏ; nếu đã có thì tăng quantity. */
    public void addItem(FoodItem item) {
        add(item, 1);
    }

    public void add(FoodItem item, int quantity) {
        for (CartEntry e : entries) {
            if (e.item.getId() == item.getId()) {
                e.quantity += quantity;
                return;
            }
        }
        entries.add(new CartEntry(item, quantity));
    }

    /** Tăng quantity lên 1. */
    public void increase(long itemId) {
        for (CartEntry e : entries) {
            if (e.item.getId() == itemId) {
                e.quantity++;
                return;
            }
        }
    }

    /** Giảm quantity xuống 1; nếu về 0 thì xóa khỏi giỏ. */
    public void decrease(long itemId) {
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).item.getId() == itemId) {
                entries.get(i).quantity--;
                if (entries.get(i).quantity <= 0) entries.remove(i);
                return;
            }
        }
    }

    /** Xóa hết giỏ hàng. */
    public void clear() {
        entries.clear();
    }

    public List<CartEntry> getEntries() {
        return new ArrayList<>(entries);
    }

    public int getTotalCount() {
        int total = 0;
        for (CartEntry e : entries) total += e.quantity;
        return total;
    }

    public double getTotalPrice() {
        double total = 0;
        for (CartEntry e : entries) total += e.subtotal();
        return total;
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }
}
