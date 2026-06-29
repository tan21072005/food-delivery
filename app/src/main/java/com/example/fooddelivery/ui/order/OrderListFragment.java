package com.example.fooddelivery.ui.order;

import com.example.fooddelivery.R;

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
                ? getArguments().getString(ARG_STATUS, "pending")
                : "pending";
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
        if ("completed".equals(tabStatus)) {
            com.example.fooddelivery.data.repository.OrderHistoryRepository history =
                    new com.example.fooddelivery.data.local.SharedPreferencesOrderHistoryRepository(requireContext());
            for (Order order : list) history.save(order);
        }
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
        List<Order> list = new ArrayList<>();
        switch (status) {
            case "pending":
                // Đơn vừa đặt trong phiên này (LocalOrderStore) — xuất hiện đầu tiên
                list.addAll(com.example.fooddelivery.data.local.LocalOrderStore
                        .getInstance().getPendingOrders());
                // Dữ liệu mẫu cố định bên dưới
                list.add(new Order(1, "Bún thập cẩm", "Bàn 3, tầng 2", 4, 125000, 7, "pending", R.drawable.food_bun_thap_cam));
                list.add(new Order(2, "Bún riêu cua",  "Bàn 1, tầng 2", 2,  70000, 7, "pending", R.drawable.food_bun_rieu_cua));
                list.add(new Order(3, "Bún bò Huế",    "Bàn 2, tầng 3", 3, 105000, 7, "pending", R.drawable.food_bun_bo_hue));
                break;
            case "completed":
                list.add(new Order(1, "guu_chicken", "Guu Chicken", "Bún thập cẩm", "Bàn 3, tầng 2", 4, 125000, 7, "completed", R.drawable.food_bun_thap_cam, 1719550000000L));
                list.add(new Order(2, "lotteria", "Lotteria", "Bún riêu cua", "Bàn 1, tầng 2", 2, 70000, 7, "completed", R.drawable.food_bun_rieu_cua, 1719650000000L));
                list.add(new Order(3, "kfc", "KFC", "Bún bò Huế", "Bàn 2, tầng 3", 3, 105000, 7, "completed", R.drawable.food_bun_bo_hue, 1719750000000L));
                list.add(new Order(6, "guu_chicken", "Guu Chicken", "Bún thập cẩm", "Bàn 3, tầng 2", 4, 125000, 7, "completed", R.drawable.food_bun_thap_cam, 1719850000000L));
                break;
            case "cancelled":
                list.add(new Order(7, "Bún thập cẩm", "Bàn 4, tầng 2", 4, 125000, 7, "cancelled", R.drawable.food_bun_thap_cam));
                list.add(new Order(8, "Bún riêu cua",  "Bàn 8, tầng 2", 2,  70000, 7, "cancelled", R.drawable.food_bun_rieu_cua));
                break;
        }
        return list;
    }

    @Override
    public void onViewDetailClick(Order order) {
        Toast.makeText(getContext(), "Chi tiáº¿t: " + order.getFoodName(), Toast.LENGTH_SHORT).show();
        // TODO: navigate tá»›i OrderDetailFragment
        // Bundle b = new Bundle(); b.putInt("orderId", order.getId());
        // Navigation.findNavController(requireView()).navigate(R.id.action_orderManagement_to_orderDetail, b);
    }

    @Override
    public void onReorderClick(Order order) {
        Toast.makeText(getContext(), "Äáº·t láº¡i: " + order.getFoodName(), Toast.LENGTH_SHORT).show();
        // TODO: xá»­ lÃ½ thÃªm vÃ o giá» hÃ ng
    }
}


