package com.example.fooddelivery.data.local;

import com.example.fooddelivery.R;
import com.example.fooddelivery.data.model.Order;
import com.example.fooddelivery.ui.cart.CheckoutSummary;

import java.util.ArrayList;
import java.util.List;

public class LocalOrderStore {

    private static LocalOrderStore instance;

    public static LocalOrderStore getInstance() {
        if (instance == null) instance = new LocalOrderStore();
        return instance;
    }

    private final List<Order> pendingOrders = new ArrayList<>();
    private final List<Order> confirmedOrders = new ArrayList<>();
    private final List<Order> preparingOrders = new ArrayList<>();
    private final List<Order> deliveringOrders = new ArrayList<>();
    private final List<Order> completedOrders = new ArrayList<>();
    private final List<Order> cancelledOrders = new ArrayList<>();
    private int nextId = 100;

    private LocalOrderStore() {
        initMockData();
    }

    private void initMockData() {
        pendingOrders.add(new Order(1, "Bún thập cẩm", "Giao đến 25 Nguyễn Đình Chiểu, Quận 1", 4, 125000, 7, "pending", R.drawable.food_bun_thap_cam, false, "25/06/2026, 13:12"));
        pendingOrders.add(new Order(2, "Bún riêu cua", "Giao đến 14 Lý Thường Kiệt, Hoàn Kiếm", 2, 70000, 7, "pending", R.drawable.food_bun_rieu_cua, false, "25/06/2026, 13:12"));
        pendingOrders.add(new Order(3, "Bún bò Huế", "Giao đến 88 Nguyễn Trãi, Thanh Xuân", 3, 105000, 7, "pending", R.drawable.food_bun_bo_hue, false, "25/06/2026, 13:12"));

        confirmedOrders.add(new Order(4, "Bún giò heo", "Giao đến 32 Trần Phú, Ba Đình", 2, 90000, 12, "confirmed", R.drawable.food_bun_gio_heo, false, "25/06/2026, 13:18"));
        preparingOrders.add(new Order(5, "Bún bò Huế", "Giao đến 7 Phan Chu Trinh, Hoàn Kiếm", 1, 45000, 18, "preparing", R.drawable.food_bun_bo_hue, false, "25/06/2026, 13:22"));
        deliveringOrders.add(new Order(6, "Lotteria - Bách Mai", "Bách Khoa", 1, 78000, 19, "delivering", R.drawable.food_bun_thap_cam, false, "13/06/2026, 11:15"));

        completedOrders.add(new Order(11, "Lotteria - Bách Mai", "Giao đến 25 Nguyễn Đình Chiểu, Quận 1", 1, 78000, 7, "completed", R.drawable.food_bun_thap_cam, false, "25/06/2026, 13:12"));
        completedOrders.add(new Order(12, "Bún riêu cua", "Giao đến 14 Lý Thường Kiệt, Hoàn Kiếm", 2, 70000, 7, "completed", R.drawable.food_bun_rieu_cua, true, "25/06/2026, 12:00"));
        completedOrders.add(new Order(13, "Bún bò Huế", "Giao đến 88 Nguyễn Trãi, Thanh Xuân", 3, 105000, 7, "completed", R.drawable.food_bun_bo_hue, false, "24/06/2026, 11:30"));
        completedOrders.add(new Order(16, "Bún thập cẩm", "Giao đến 25 Nguyễn Đình Chiểu, Quận 1", 4, 125000, 7, "completed", R.drawable.food_bun_thap_cam, true, "14/06/2026, 13:03"));

        cancelledOrders.add(new Order(17, "Lotteria - Bách Mai", "Giao đến 56 Bạch Mai, Hai Bà Trưng", 1, 78000, 7, "cancelled", R.drawable.food_bun_thap_cam, false, "25/06/2026, 13:11"));
        cancelledOrders.add(new Order(18, "Bún riêu cua", "Giao đến 19 Cầu Giấy, Cầu Giấy", 2, 70000, 7, "cancelled", R.drawable.food_bun_rieu_cua, false, "20/06/2026, 09:15"));
    }

    public void addOrder(Order order) {
        pendingOrders.add(0, order);
    }

    public Order createFromCart(LocalCart cart, String deliveryAddress) {
        return createFromCart(cart, cart.getRestaurantId(), deliveryAddress);
    }

    public Order createFromCart(LocalCart cart, long restaurantId, String deliveryAddress) {
        if (cart.isEmpty(restaurantId)) return null;

        List<LocalCart.CartEntry> entries = cart.getEntries(restaurantId);
        LocalCart.CartEntry first = entries.get(0);
        String foodName = draftTitle(entries);

        Order order = new Order(
                nextId++,
                foodName,
                deliveryAddress,
                cart.getTotalCount(restaurantId),
                Math.round(cart.getTotalPrice(restaurantId)) + CheckoutSummary.DEFAULT_DELIVERY_FEE,
                15,
                "pending",
                first.item.getImageResId(),
                false,
                "Vừa xong"
        );

        pendingOrders.add(0, order);
        return order;
    }

    public List<Order> getDraftOrders() {
        List<Order> draftOrders = new ArrayList<>();
        LocalCart cart = LocalCart.getInstance();
        if (cart.isEmpty()) return draftOrders;

        for (long restaurantId : cart.getRestaurantIds()) {
            List<LocalCart.CartEntry> entries = cart.getEntries(restaurantId);
            if (entries.isEmpty()) continue;
            LocalCart.CartEntry first = entries.get(0);

            draftOrders.add(new Order(
                    (int) -restaurantId,
                    draftTitle(entries),
                    "Cart Restaurant #" + restaurantId,
                    cart.getTotalCount(restaurantId),
                    Math.round(cart.getTotalPrice(restaurantId)),
                    0,
                    "draft",
                    first.item.getImageResId(),
                    false,
                    "Chưa đặt"
            ));
        }
        return draftOrders;
    }

    public List<Order> getPendingOrders() {
        return new ArrayList<>(pendingOrders);
    }

    public List<Order> getConfirmedOrders() {
        return new ArrayList<>(confirmedOrders);
    }

    public List<Order> getPreparingOrders() {
        return new ArrayList<>(preparingOrders);
    }

    public List<Order> getDeliveringOrders() {
        return new ArrayList<>(deliveringOrders);
    }

    public List<Order> getCompletedOrders() {
        return new ArrayList<>(completedOrders);
    }

    public List<Order> getCancelledOrders() {
        return new ArrayList<>(cancelledOrders);
    }

    public Order findOrderById(long orderId) {
        Order order = findOrderInList(pendingOrders, orderId);
        if (order != null) return order;
        order = findOrderInList(confirmedOrders, orderId);
        if (order != null) return order;
        order = findOrderInList(preparingOrders, orderId);
        if (order != null) return order;
        order = findOrderInList(deliveringOrders, orderId);
        if (order != null) return order;
        order = findOrderInList(completedOrders, orderId);
        if (order != null) return order;
        return findOrderInList(cancelledOrders, orderId);
    }

    private Order findOrderInList(List<Order> orders, long orderId) {
        for (Order order : orders) {
            if (order.getId() == orderId) {
                return order;
            }
        }
        return null;
    }

    public void markAsReviewed(long orderId) {
        for (Order order : completedOrders) {
            if (order.getId() == orderId) {
                order.setReviewed(true);
                return;
            }
        }
    }

    private String draftTitle(List<LocalCart.CartEntry> entries) {
        LocalCart.CartEntry first = entries.get(0);
        String foodName = first.item.getName();
        if (entries.size() > 1) {
            foodName += " + " + (entries.size() - 1) + " món khác";
        }
        return foodName;
    }
}
