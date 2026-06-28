package com.example.fooddelivery.data.local;

import com.example.fooddelivery.data.model.Order;

import java.util.ArrayList;
import java.util.List;

/**
 * In-memory singleton lưu danh sách Order đã đặt trong phiên này.
 * Dùng trong DEV — chưa kết nối Supabase.
 */
public class LocalOrderStore {

    private static LocalOrderStore instance;

    public static LocalOrderStore getInstance() {
        if (instance == null) instance = new LocalOrderStore();
        return instance;
    }

    private final List<Order> pendingOrders = new ArrayList<>();
    private int nextId = 100; // bắt đầu từ ID cao để tránh trùng mock data

    private LocalOrderStore() {}

    /** Thêm đơn hàng mới vào danh sách đang chờ. */
    public void addOrder(Order order) {
        pendingOrders.add(0, order); // thêm vào đầu danh sách (mới nhất lên trên)
    }

    /** Tạo Order mới từ nội dung giỏ hàng LocalCart và thêm vào store. */
    public Order createFromCart(LocalCart cart, String tableInfo) {
        if (cart.isEmpty()) return null;

        // Lấy tên món đầu tiên làm tiêu đề đơn hàng
        LocalCart.CartEntry first = cart.getEntries().get(0);
        String foodName = first.item.getName();
        if (cart.getEntries().size() > 1) {
            foodName += " + " + (cart.getEntries().size() - 1) + " món khác";
        }

        int totalQty = cart.getTotalCount();
        long totalPrice = Math.round(cart.getTotalPrice());
        int imageResId = first.item.getImageResId(); // có thể 0 nếu dùng URL

        Order order = new Order(
                nextId++,
                foodName,
                tableInfo,
                totalQty,
                totalPrice,
                7,       // thời gian chờ cố định 7 phút
                "pending",
                imageResId
        );

        // Lưu URL ảnh vào order nếu cần (mở rộng sau)
        pendingOrders.add(0, order);
        return order;
    }

    /** Lấy tất cả đơn đang chờ. */
    public List<Order> getPendingOrders() {
        return new ArrayList<>(pendingOrders);
    }
}
