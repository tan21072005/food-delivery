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
import com.example.fooddelivery.data.model.Order;
import com.example.fooddelivery.ui.cart.Checkout;

import java.util.ArrayList;
import java.util.List;

public class OrderListFragment extends Fragment
        implements OrderAdapter.OnOrderActionListener {

    private static final String ARG_STATUS = "status";

    private String       tabStatus;
    private RecyclerView recyclerView;
    private LinearLayout layoutEmpty;
    private OrderAdapter adapter;

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
        List<Order> list = getMockOrders(tabStatus);
        if (list.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
            adapter.updateData(list);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload để hiển thị đơn hàng mới đặt (từ LocalOrderStore)
        if (adapter != null) loadOrders();
    }

    // ── Orders data ─────────────────────────────────────────────────────────────

    private List<Order> getMockOrders(String status) {
        switch (status) {
            case "draft":
                return com.example.fooddelivery.data.local.LocalOrderStore.getInstance().getDraftOrders();
            case "completed":
                return com.example.fooddelivery.data.local.LocalOrderStore.getInstance().getCompletedOrders();
            case "cancelled":
                return com.example.fooddelivery.data.local.LocalOrderStore.getInstance().getCancelledOrders();
            default:
                List<Order> processingOrders = new ArrayList<>();
                processingOrders.addAll(com.example.fooddelivery.data.local.LocalOrderStore.getInstance().getPendingOrders());
                processingOrders.addAll(com.example.fooddelivery.data.local.LocalOrderStore.getInstance().getConfirmedOrders());
                processingOrders.addAll(com.example.fooddelivery.data.local.LocalOrderStore.getInstance().getPreparingOrders());
                processingOrders.addAll(com.example.fooddelivery.data.local.LocalOrderStore.getInstance().getDeliveringOrders());
                return processingOrders;
        }
    }

    @Override
    public void onViewDetailClick(Order order) {
        if ("draft".equals(order.getStatus())) {
            Intent intent = new Intent(requireContext(), Checkout.class);
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

        com.example.fooddelivery.data.local.LocalCart.getInstance().clear();
        com.example.fooddelivery.data.local.LocalCart.getInstance().add(food, order.getQuantity());

        Intent intent = new Intent(requireContext(), Checkout.class);
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
