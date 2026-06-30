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
        String restaurantName = restaurantName(detail);
        String deliveryAddress = deliveryAddress(detail);
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

        OrderItemLines lines = orderItemLines(detail);
        setText(tvFoodQuantityLabel, lines.quantities);
        setText(tvFoodNameLabel, lines.names);
        setText(tvFoodPriceLabel, lines.prices);
    }

    public static String restaurantName(JsonObject detail) {
        String nestedName = objectString(detail, "restaurant", "name", "restaurant_name", "title");
        if (!isBlank(nestedName)) {
            return nestedName;
        }

        String topLevelName = firstString(detail, "restaurant_name", "restaurant");
        return isBlank(topLevelName) ? "Nha hang" : topLevelName;
    }

    public static String deliveryAddress(JsonObject detail) {
        JsonElement deliveryAddress = detail == null ? null : detail.get("delivery_address");
        if (deliveryAddress != null && !deliveryAddress.isJsonNull()) {
            if (deliveryAddress.isJsonObject()) {
                String formatted = formatDeliveryAddress(deliveryAddress.getAsJsonObject());
                if (!isBlank(formatted)) {
                    return formatted;
                }
            } else {
                String primitive = elementAsString(deliveryAddress);
                if (!isBlank(primitive)) {
                    return primitive;
                }
            }
        }

        return firstString(detail,
                "delivery_address_text",
                "address",
                "address_line");
    }

    public static OrderItemLines orderItemLines(JsonObject detail) {
        JsonArray items = orderItems(detail);
        if (items == null || items.size() == 0) {
            return new OrderItemLines("0x", "Chua co du lieu mon", MoneyFormatter.format(0));
        }

        StringBuilder quantities = new StringBuilder();
        StringBuilder names = new StringBuilder();
        StringBuilder prices = new StringBuilder();
        for (JsonElement itemElement : items) {
            if (itemElement == null || !itemElement.isJsonObject()) continue;
            JsonObject item = itemElement.getAsJsonObject();
            int quantity = (int) Math.max(1, Math.round(numberValue(item, "quantity", "qty")));
            String itemName = firstNonBlank(
                    firstString(item, "item_name", "menu_item_name", "name"),
                    objectString(item, "menu_item", "name", "item_name", "menu_item_name")
            );
            long itemPrice = Math.round(numberValue(item, "line_total", "total_price", "base_price", "unit_price"));
            appendLine(quantities, quantity + "x");
            appendLine(names, itemName == null ? "Mon an" : itemName);
            appendLine(prices, MoneyFormatter.format(itemPrice));
        }

        return new OrderItemLines(
                quantities.length() == 0 ? "0x" : quantities.toString(),
                names.length() == 0 ? "Chua co du lieu mon" : names.toString(),
                prices.length() == 0 ? MoneyFormatter.format(0) : prices.toString()
        );
    }

    private static String formatDeliveryAddress(JsonObject address) {
        StringBuilder builder = new StringBuilder();
        appendPart(builder, firstString(address, "recipient_name", "receiver_name", "name"));
        appendPart(builder, firstString(address, "phone", "phone_number", "recipient_phone"));
        appendPart(builder, firstString(address, "address_line", "street", "address"));
        appendPart(builder, firstString(address, "ward"));
        appendPart(builder, firstString(address, "district"));
        appendPart(builder, firstString(address, "city", "province"));
        return builder.toString();
    }

    private static void appendPart(StringBuilder builder, String value) {
        if (isBlank(value)) {
            return;
        }
        if (builder.length() > 0) {
            builder.append(", ");
        }
        builder.append(value.trim());
    }

    private static JsonArray orderItems(JsonObject detail) {
        JsonArray items = arrayValue(detail, "items");
        if (items == null) items = arrayValue(detail, "order_items");
        return items;
    }

    private static void appendLine(StringBuilder builder, String value) {
        if (builder.length() > 0) {
            builder.append('\n');
        }
        builder.append(value);
    }

    private static JsonArray arrayValue(JsonObject object, String key) {
        JsonElement value = object == null ? null : object.get(key);
        return value != null && value.isJsonArray() ? value.getAsJsonArray() : null;
    }

    private static String stringValue(JsonObject object, String key, String fallback) {
        String value = firstString(object, key);
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    private static String firstString(JsonObject object, String... keys) {
        for (String key : keys) {
            JsonElement value = object == null ? null : object.get(key);
            String text = elementAsString(value);
            if (!isBlank(text)) {
                return text;
            }
        }
        return null;
    }

    private static String objectString(JsonObject object, String objectKey, String... nestedKeys) {
        JsonElement nested = object == null ? null : object.get(objectKey);
        if (nested == null || !nested.isJsonObject()) {
            return null;
        }
        return firstString(nested.getAsJsonObject(), nestedKeys);
    }

    private static String elementAsString(JsonElement value) {
        if (value == null || value.isJsonNull() || value.isJsonObject() || value.isJsonArray()) {
            return null;
        }
        try {
            return value.getAsString();
        } catch (UnsupportedOperationException | IllegalStateException ignored) {
            return null;
        }
    }

    private static String firstNonBlank(String first, String second) {
        return isBlank(first) ? second : first;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static double numberValue(JsonObject object, String... keys) {
        for (String key : keys) {
            JsonElement value = object == null ? null : object.get(key);
            if (value != null && !value.isJsonNull()) {
                try {
                    return value.getAsDouble();
                } catch (NumberFormatException | UnsupportedOperationException | IllegalStateException ignored) {
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

    public static class OrderItemLines {
        public final String quantities;
        public final String names;
        public final String prices;

        public OrderItemLines(String quantities, String names, String prices) {
            this.quantities = quantities;
            this.names = names;
            this.prices = prices;
        }
    }
}
