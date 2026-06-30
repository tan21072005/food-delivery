package com.example.fooddelivery.ui.order;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.fooddelivery.R;
import com.example.fooddelivery.data.repository.OrderRepository;
import com.example.fooddelivery.utils.MoneyFormatter;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderDetailFragment extends Fragment {

    private OrderRepository orderRepository;
    private long orderId = -1L;

    private TextView tvOrderStatusTitle;
    private TextView tvOrderStatusSubtitle;
    private TextView tvRestaurantNameLabel;
    private TextView tvDeliveryAddress;
    private TextView tvRestaurantNameSummary;
    private TextView tvFoodQuantityLabel;
    private TextView tvFoodNameLabel;
    private TextView tvFoodPriceLabel;
    private TextView tvTotalPrice;
    private TextView tvOrderCode;
    private TextView tvOrderTimeLong;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.order_fragment_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        orderRepository = new OrderRepository(requireContext());
        orderId = getArguments() == null ? -1L : getArguments().getLong("order_id", -1L);

        bindViews(view);
        setupActions(view);
        loadOrderDetailV3();
    }

    private void bindViews(View view) {
        tvOrderStatusTitle = view.findViewById(R.id.tvOrderStatusTitle);
        tvOrderStatusSubtitle = view.findViewById(R.id.tvOrderStatusSubtitle);
        tvRestaurantNameLabel = view.findViewById(R.id.tvResNameLabel);
        tvDeliveryAddress = view.findViewById(R.id.tvDeliveryAddress);
        tvRestaurantNameSummary = view.findViewById(R.id.tvResNameSum);
        tvFoodQuantityLabel = view.findViewById(R.id.tvFoodQuantityLabel);
        tvFoodNameLabel = view.findViewById(R.id.tvFoodNameLabel);
        tvFoodPriceLabel = view.findViewById(R.id.tvFoodPriceLabel);
        tvTotalPrice = view.findViewById(R.id.tvTotalPrice);
        tvOrderCode = view.findViewById(R.id.tvOrderCode);
        tvOrderTimeLong = view.findViewById(R.id.tvOrderTimeLong);
    }

    private void setupActions(View view) {
        view.findViewById(R.id.ivBack).setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.popBackStack();
        });

        view.findViewById(R.id.btnReviewOrder).setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putLong("order_id", orderId);
            Navigation.findNavController(v).navigate(R.id.action_orderDetailFragment_to_orderReviewFragment, bundle);
        });

        view.findViewById(R.id.btnReorderBottom).setOnClickListener(v ->
                Toast.makeText(requireContext(),
                        "Tinh nang dat lai se duoc ho tro khi RPC tra du du lieu mon",
                        Toast.LENGTH_SHORT).show()
        );
    }

    private void loadOrderDetailV3() {
        if (orderId <= 0) {
            Toast.makeText(requireContext(), "Khong tim thay ma don hang", Toast.LENGTH_SHORT).show();
            return;
        }

        orderRepository.getOrderDetailV3(orderId).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (!isAdded()) return;
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(requireContext(), "Khong the tai chi tiet don hang", Toast.LENGTH_SHORT).show();
                    return;
                }
                renderOrderDetail(response.body());
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(),
                        "Khong the tai chi tiet don hang: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void renderOrderDetail(JsonObject detail) {
        String status = stringValue(detail, "status", "Trang thai don hang");
        String restaurantName = stringValue(detail, "restaurant_name", "Nha hang");
        String deliveryAddress = firstString(detail,
                "delivery_address",
                "delivery_address_text",
                "address",
                "address_line");
        String createdAt = stringValue(detail, "created_at", "");
        long totalAmount = Math.round(numberValue(detail, "total_amount", "total", "grand_total"));

        setText(tvOrderStatusTitle, statusLabel(status));
        setText(tvOrderStatusSubtitle, statusSubtitle(status));
        setText(tvRestaurantNameLabel, "Tu " + restaurantName);
        setText(tvDeliveryAddress, deliveryAddress == null ? "Den dia chi giao hang" : "Den " + deliveryAddress);
        setText(tvRestaurantNameSummary, restaurantName);
        setText(tvTotalPrice, MoneyFormatter.format(totalAmount));
        setText(tvOrderCode, "#" + orderId);
        setText(tvOrderTimeLong, createdAt);

        JsonArray items = orderItems(detail);
        if (items == null || items.size() == 0) {
            setText(tvFoodQuantityLabel, "0x");
            setText(tvFoodNameLabel, "Chua co du lieu mon");
            setText(tvFoodPriceLabel, MoneyFormatter.format(0));
            return;
        }

        StringBuilder quantities = new StringBuilder();
        StringBuilder names = new StringBuilder();
        StringBuilder prices = new StringBuilder();
        for (JsonElement itemElement : items) {
            if (itemElement == null || !itemElement.isJsonObject()) continue;
            JsonObject item = itemElement.getAsJsonObject();
            int quantity = (int) Math.max(1, Math.round(numberValue(item, "quantity", "qty")));
            String itemName = firstString(item, "item_name", "menu_item_name", "name");
            long itemPrice = Math.round(numberValue(item, "line_total", "total_price", "base_price", "unit_price"));
            appendLine(quantities, quantity + "x");
            appendLine(names, itemName == null ? "Mon an" : itemName);
            appendLine(prices, MoneyFormatter.format(itemPrice));
        }

        setText(tvFoodQuantityLabel, quantities.length() == 0 ? "0x" : quantities.toString());
        setText(tvFoodNameLabel, names.length() == 0 ? "Chua co du lieu mon" : names.toString());
        setText(tvFoodPriceLabel, prices.length() == 0 ? MoneyFormatter.format(0) : prices.toString());
    }

    private JsonArray orderItems(JsonObject detail) {
        JsonArray items = arrayValue(detail, "items");
        if (items == null) items = arrayValue(detail, "order_items");
        return items;
    }

    private void appendLine(StringBuilder builder, String value) {
        if (builder.length() > 0) {
            builder.append('\n');
        }
        builder.append(value);
    }

    private JsonArray arrayValue(JsonObject object, String key) {
        JsonElement value = object.get(key);
        return value != null && value.isJsonArray() ? value.getAsJsonArray() : null;
    }

    private String stringValue(JsonObject object, String key, String fallback) {
        String value = firstString(object, key);
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    private String firstString(JsonObject object, String... keys) {
        for (String key : keys) {
            JsonElement value = object.get(key);
            if (value != null && !value.isJsonNull()) {
                return value.getAsString();
            }
        }
        return null;
    }

    private double numberValue(JsonObject object, String... keys) {
        for (String key : keys) {
            JsonElement value = object.get(key);
            if (value != null && !value.isJsonNull()) {
                try {
                    return value.getAsDouble();
                } catch (NumberFormatException ignored) {
                    return 0;
                }
            }
        }
        return 0;
    }

    private String statusLabel(String status) {
        if ("completed".equals(status)) return "Da giao";
        if ("cancelled".equals(status)) return "Da huy";
        if ("pending".equals(status)) return "Dang xu ly";
        if ("confirmed".equals(status)) return "Da xac nhan";
        if ("preparing".equals(status)) return "Dang chuan bi";
        if ("ready_for_pickup".equals(status)) return "San sang giao";
        if ("delivering".equals(status)) return "Dang giao";
        return status;
    }

    private String statusSubtitle(String status) {
        if ("completed".equals(status)) return "Don hang da hoan tat";
        if ("cancelled".equals(status)) return "Don hang da bi huy";
        if ("delivering".equals(status)) return "Tai xe dang giao don den ban";
        return "Don hang dang duoc cap nhat";
    }

    private void setText(TextView textView, String text) {
        if (textView != null) {
            textView.setText(text);
        }
    }
}
