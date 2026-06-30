package com.example.fooddelivery.ui.order;

import com.example.fooddelivery.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fooddelivery.ui.order.adapters.OrderAdapter;
import com.example.fooddelivery.data.model.DraftCartV3Response;
import com.example.fooddelivery.data.model.MyOrderV3Response;
import com.example.fooddelivery.data.model.Order;
import com.example.fooddelivery.data.repository.OrderRepository;
import com.example.fooddelivery.ui.cart.Checkout;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderListFragment extends Fragment
        implements OrderAdapter.OnOrderActionListener {

    private static final String ARG_STATUS = "status";

    private String       tabStatus;
    private RecyclerView recyclerView;
    private LinearLayout layoutEmpty;
    private OrderAdapter adapter;
    private OrderRepository orderRepository;

    public static OrderListFragment newInstance(String status) {
        OrderListFragment f = new OrderListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_STATUS, status);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tabStatus = getArguments() != null
                ? getArguments().getString(ARG_STATUS, "processing")
                : "processing";
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.order_fragment_list, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewOrders);
        layoutEmpty  = view.findViewById(R.id.layoutEmpty);
        orderRepository = new OrderRepository(requireContext());
        setupRecyclerView();
        loadOrders();
        return view;
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new OrderAdapter(getContext(), new ArrayList<>(), tabStatus, this);
        recyclerView.setAdapter(adapter);
    }

    private void loadOrders() {
        if ("draft".equals(tabStatus)) {
            loadDraftCartsV3();
            return;
        }

        loadMyOrdersV3();
    }

    private void loadDraftCartsV3() {
        orderRepository.getDraftCartsV3().enqueue(new Callback<List<DraftCartV3Response>>() {
            @Override
            public void onResponse(@NonNull Call<List<DraftCartV3Response>> call,
                                   @NonNull Response<List<DraftCartV3Response>> response) {
                if (!isAdded()) return;
                if (!response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Khong the tai gio hang nhap", Toast.LENGTH_SHORT).show();
                    renderOrders(new ArrayList<>());
                    return;
                }
                renderOrders(mapDraftCarts(response.body()));
            }

            @Override
            public void onFailure(@NonNull Call<List<DraftCartV3Response>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(),
                        "Khong the tai gio hang nhap: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                renderOrders(new ArrayList<>());
            }
        });
    }

    private void renderOrders(List<Order> list) {
        if (list.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
            adapter.updateData(list);
        }
    }

    private void loadMyOrdersV3() {
        String rpcStatus = "processing".equals(tabStatus) ? null : tabStatus;
        orderRepository.getMyOrdersV3(rpcStatus).enqueue(new Callback<List<MyOrderV3Response>>() {
            @Override
            public void onResponse(@NonNull Call<List<MyOrderV3Response>> call,
                                   @NonNull Response<List<MyOrderV3Response>> response) {
                if (!isAdded()) return;
                if (!response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Khong the tai don hang", Toast.LENGTH_SHORT).show();
                    renderOrders(new ArrayList<>());
                    return;
                }
                renderOrders(mapMyOrders(response.body()));
            }

            @Override
            public void onFailure(@NonNull Call<List<MyOrderV3Response>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(),
                        "Khong the tai don hang: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                renderOrders(new ArrayList<>());
            }
        });
    }

    private List<Order> mapDraftCarts(List<DraftCartV3Response> draftCarts) {
        List<Order> orders = new ArrayList<>();
        if (draftCarts == null) return orders;

        for (DraftCartV3Response draftCart : draftCarts) {
            if (draftCart == null) continue;
            String title = draftCartTitle(draftCart);
            String restaurantName = safeText(draftCart.getRestaurantName(), "Cart #" + draftCart.getCartId());
            Order order = new Order(
                    safeIntId(draftCart.getCartId()),
                    String.valueOf(draftCart.getRestaurantId()),
                    restaurantName,
                    title,
                    restaurantName,
                    draftCart.getItemCount(),
                    Math.round(draftCart.getTotalAmount()),
                    0,
                    "draft",
                    R.drawable.ic_food_placeholder,
                    0L,
                    false,
                    safeText(draftCart.getUpdatedAt(), "Chua dat")
            );
            order.setRpcCartId(draftCart.getCartId());
            orders.add(order);
        }
        return orders;
    }

    private String draftCartTitle(DraftCartV3Response draftCart) {
        List<DraftCartV3Response.PreviewItem> previewItems = draftCart.getPreviewItems();
        if (previewItems == null || previewItems.isEmpty()) {
            return safeText(draftCart.getRestaurantName(), "Gio hang nhap");
        }

        DraftCartV3Response.PreviewItem first = previewItems.get(0);
        String title = safeText(first.getItemName(), "Mon an");
        if (previewItems.size() > 1) {
            title += " + " + (previewItems.size() - 1) + " mon khac";
        }
        return title;
    }

    private List<Order> mapMyOrders(List<MyOrderV3Response> responses) {
        List<Order> orders = new ArrayList<>();
        if (responses == null) return orders;

        for (MyOrderV3Response response : responses) {
            if (response == null) continue;
            String status = safeText(response.getStatus(), "pending");
            if ("processing".equals(tabStatus) && !isProcessingStatus(status)) {
                continue;
            }

            String restaurantName = safeText(response.getRestaurantName(), "Don hang #" + response.getOrderId());
            Order order = new Order(
                    safeIntId(response.getOrderId()),
                    String.valueOf(response.getRestaurantId()),
                    restaurantName,
                    orderTitle(response),
                    restaurantName,
                    response.getItemCount(),
                    Math.round(response.getTotalAmount()),
                    0,
                    status,
                    R.drawable.ic_food_placeholder,
                    0L,
                    false,
                    safeText(response.getCreatedAt(), "")
            );
            if (response.getCartId() != null) {
                order.setRpcCartId(response.getCartId());
            }
            orders.add(order);
        }
        return orders;
    }

    private String orderTitle(MyOrderV3Response order) {
        List<MyOrderV3Response.PreviewItem> previewItems = order.getPreviewItems();
        if (previewItems == null || previewItems.isEmpty()) {
            return safeText(order.getRestaurantName(), "Don hang");
        }

        MyOrderV3Response.PreviewItem first = previewItems.get(0);
        String title = safeText(first.getItemName(), "Mon an");
        if (previewItems.size() > 1) {
            title += " + " + (previewItems.size() - 1) + " mon khac";
        }
        return title;
    }

    private boolean isProcessingStatus(String status) {
        return "pending".equals(status)
                || "confirmed".equals(status)
                || "preparing".equals(status)
                || "ready_for_pickup".equals(status)
                || "delivering".equals(status);
    }

    private int safeIntId(long value) {
        if (value > Integer.MAX_VALUE) return Integer.MAX_VALUE;
        if (value < Integer.MIN_VALUE) return Integer.MIN_VALUE;
        return (int) value;
    }

    private String safeText(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload orders after checkout returns to this tab.
        if (adapter != null) loadOrders();
    }

    // ── Orders data ─────────────────────────────────────────────────────────────

    @Override
    public void onViewDetailClick(Order order) {
        if ("draft".equals(order.getStatus())) {
            Intent intent = new Intent(requireContext(), Checkout.class);
            intent.putExtra("cart_id", order.getRpcCartId() > 0 ? order.getRpcCartId() : (long) order.getId());
            try {
                intent.putExtra("restaurant_id", Long.parseLong(order.getRestaurantId()));
            } catch (NumberFormatException ignored) {
                intent.putExtra("restaurant_id", -1L);
            }
            startActivity(intent);
            return;
        }

        try {
            androidx.navigation.NavController navController = androidx.navigation.fragment.NavHostFragment.findNavController(getParentFragment());
            navController.navigate(R.id.action_orderManagement_to_orderDetail);
        } catch (Exception e) {
            android.widget.Toast.makeText(getContext(), "Nav Error: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onReorderClick(Order order) {
        double unitPrice = order.getTotalPrice() / (double) order.getQuantity();
        com.example.fooddelivery.data.model.FoodItem food = new com.example.fooddelivery.data.model.FoodItem(
                System.currentTimeMillis(), order.getFoodName(), "Reordered Item", 0, unitPrice, null);
        food.setImageResId(order.getFoodImageResId());

        com.example.fooddelivery.data.local.LocalCart.getInstance().add(food, order.getQuantity());

        Intent intent = new Intent(requireContext(), Checkout.class);
        intent.putExtra("restaurant_id", food.getRestaurantId());
        startActivity(intent);
    }

    @Override
    public void onReviewClick(Order order) {
        Bundle bundle = new Bundle();
        bundle.putLong("order_id", order.getId());
        try {
            androidx.navigation.NavController navController = androidx.navigation.fragment.NavHostFragment.findNavController(getParentFragment());
            navController.navigate(R.id.action_orderManagement_to_orderReview, bundle);
        } catch (Exception e) {
            android.widget.Toast.makeText(getContext(), "Nav Error: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
}
