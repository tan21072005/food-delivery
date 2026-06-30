package com.example.fooddelivery.ui.cart;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fooddelivery.MainActivity;
import com.example.fooddelivery.R;
import com.example.fooddelivery.data.local.LocalCart;
import com.example.fooddelivery.data.local.LocalOrderStore;
import com.example.fooddelivery.data.model.CartSummaryV3Response;
import com.example.fooddelivery.data.model.DeliveryAddress;
import com.example.fooddelivery.data.repository.DeliveryAddressRepository;
import com.example.fooddelivery.data.repository.OrderRepository;
import com.example.fooddelivery.ui.cart.adapters.CartBottomSheetAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Checkout extends AppCompatActivity {

    private static final String FALLBACK_ADDRESS_LABEL = "Nha";
    private static final String FALLBACK_ADDRESS_DETAIL = "So 1 Dai Co Viet, Hai Ba Trung, Ha Noi";
    private static final String PAYMENT_COD = "Thanh toan khi nhan hang";

    private CartBottomSheetAdapter adapter;
    private TextView tvAddressTitle;
    private TextView tvAddressSubtitle;
    private TextView tvRestaurantName;
    private TextView tvEmptyCart;
    private TextView tvSubtotalLabel;
    private TextView tvSubtotal;
    private TextView tvDeliveryFee;
    private TextView tvServiceFee;
    private TextView tvVoucherDiscount;
    private TextView tvTotalPrice;
    private TextView tvStickyTotal;
    private TextView tvStickyPayment;
    private TextView tvPaymentMethod;
    private Button btnOrder;
    private EditText edNote;
    private View loadingOverlay;

    private OrderRepository orderRepository;
    private DeliveryAddressRepository deliveryAddressRepository;
    private long restaurantId = -1L;
    private long cartId = -1L;
    private long deliveryAddressId = -1L;
    private CartSummaryV3Response rpcSummary;
    private boolean isSubmitting = false;
    private boolean hasDeliveryAddress = true;
    private String selectedPaymentMethod = PAYMENT_COD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cart_activity_checkout);

        orderRepository = new OrderRepository(this);
        deliveryAddressRepository = new DeliveryAddressRepository(this);
        cartId = getIntent().getLongExtra("cart_id", -1L);
        restaurantId = getIntent().getLongExtra("restaurant_id", LocalCart.getInstance().getRestaurantId());
        if (isRpcCheckout()) {
            hasDeliveryAddress = false;
        } else {
            LocalCart.getInstance().setActiveRestaurantId(restaurantId);
        }

        initViews();
        loadCurrentDeliveryAddress();
        renderCart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (deliveryAddressRepository != null && tvAddressTitle != null) {
            loadCurrentDeliveryAddress();
        }
    }

    public static Intent buildCheckoutAddressIntent(android.content.Context context,
                                                    long cartId,
                                                    long restaurantId) {
        Intent intent = context == null
                ? new Intent()
                : new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("open_address_source", "checkout");
        intent.putExtra("cart_id", cartId);
        intent.putExtra("restaurant_id", restaurantId);
        return intent;
    }

    private void initViews() {
        ImageView ivBack = findViewById(R.id.ivBack);
        TextView tvAddMore = findViewById(R.id.tvAddMore);
        TextView tvSchedule = findViewById(R.id.tvSchedule);
        TextView tvChangeAddress = findViewById(R.id.tvChangeAddress);
        View sectionAddress = findViewById(R.id.sectionAddress);
        View sectionVoucher = findViewById(R.id.sectionVoucher);
        View sectionPayment = findViewById(R.id.sectionPayment);

        tvAddressTitle = findViewById(R.id.tvAddressTitle);
        tvAddressSubtitle = findViewById(R.id.tvAddressSubtitle);
        tvRestaurantName = findViewById(R.id.tvRestaurantName);
        tvEmptyCart = findViewById(R.id.tvEmptyCart);
        tvSubtotalLabel = findViewById(R.id.tvSubtotalLabel);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvDeliveryFee = findViewById(R.id.tvDeliveryFee);
        tvServiceFee = findViewById(R.id.tvServiceFee);
        tvVoucherDiscount = findViewById(R.id.tvVoucherDiscount);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        tvStickyTotal = findViewById(R.id.tvStickyTotal);
        tvStickyPayment = findViewById(R.id.tvStickyPayment);
        tvPaymentMethod = findViewById(R.id.tvPaymentMethod);
        btnOrder = findViewById(R.id.btnOrder);
        edNote = findViewById(R.id.edNote);
        loadingOverlay = findViewById(R.id.loadingOverlay);

        tvAddMore.setText("Thêm món");
        ivBack.setOnClickListener(v -> finish());
        tvAddMore.setOnClickListener(v -> finish());
        tvSchedule.setOnClickListener(v ->
                Toast.makeText(this, "Sap ho tro hen gio giao", Toast.LENGTH_SHORT).show());
        tvChangeAddress.setOnClickListener(v -> openAddressFlow());
        sectionAddress.setOnClickListener(v -> openAddressFlow());
        sectionVoucher.setOnClickListener(v ->
                Toast.makeText(this, "Voucher se duoc ho tro khi backend san sang", Toast.LENGTH_SHORT).show());
        sectionPayment.setOnClickListener(v -> showPaymentMethodSheet());
        btnOrder.setOnClickListener(v -> {
            if (!hasDeliveryAddress) {
                openAddressFlow();
                return;
            }
            placeOrder();
        });

        tvAddressTitle.setText(FALLBACK_ADDRESS_LABEL);
        tvAddressSubtitle.setText(FALLBACK_ADDRESS_DETAIL);
        tvPaymentMethod.setText(selectedPaymentMethod);
        tvStickyPayment.setText(selectedPaymentMethod);
    }

    private void renderCart() {
        RecyclerView rvCartItems = findViewById(R.id.rvCartItems);
        rvCartItems.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CartBottomSheetAdapter(
                this,
                isRpcCheckout() ? new ArrayList<>() : LocalCart.getInstance().getEntries(restaurantId),
                new CartBottomSheetAdapter.Listener() {
                    @Override
                    public void onIncrease(LocalCart.CartEntry entry) {
                        if (isRpcCheckout()) return;
                        LocalCart.getInstance().increase(restaurantId, entry.item.getId());
                        refreshCartUi();
                    }

                    @Override
                    public void onDecrease(LocalCart.CartEntry entry) {
                        if (isRpcCheckout()) return;
                        LocalCart.getInstance().decrease(restaurantId, entry.item.getId());
                        refreshCartUi();
                    }

                    @Override
                    public void onEdit(LocalCart.CartEntry entry) {
                        openCartEditor();
                    }
                });
        rvCartItems.setAdapter(adapter);

        tvRestaurantName.setText("Don hang cua ban");
        if (isRpcCheckout()) {
            loadCartSummaryV3();
        } else {
            refreshCartUi();
        }
    }

    private void openAddressFlow() {
        startActivity(buildCheckoutAddressIntent(this, cartId, restaurantId));
    }

    private void openCartEditor() {
        CartBottomSheet sheet = new CartBottomSheet(() -> {
            if (isRpcCheckout()) {
                loadCartSummaryV3();
            } else {
                refreshCartUi();
            }
        });
        Bundle args = new Bundle();
        args.putLong("restaurant_id", restaurantId);
        if (cartId > 0) {
            args.putLong("cart_id", cartId);
        }
        sheet.setArguments(args);
        sheet.show(getSupportFragmentManager(), CartBottomSheet.TAG);
    }

    private void refreshCartUi() {
        if (isRpcCheckout()) {
            refreshRpcCartUi();
            return;
        }

        LocalCart cart = LocalCart.getInstance();
        if (adapter != null) {
            adapter.updateData(cart.getEntries(restaurantId));
        }

        boolean cartEmpty = cart.isEmpty(restaurantId);
        tvEmptyCart.setVisibility(cartEmpty ? View.VISIBLE : View.GONE);
        CheckoutSummary summary = buildSummary(cart);

        tvSubtotalLabel.setText("Tam tinh (" + cart.getTotalCount(restaurantId) + " phan)");
        tvSubtotal.setText(formatPrice(summary.getSubtotal()));
        tvDeliveryFee.setText(formatPrice(summary.getDeliveryFee()));
        tvServiceFee.setText(formatPrice(summary.getServiceFee()));
        tvVoucherDiscount.setText("-" + formatPrice(summary.getVoucherDiscount()));
        tvTotalPrice.setText(formatPrice(summary.getTotal()));
        tvStickyTotal.setText(formatPrice(summary.getTotal()));
        tvPaymentMethod.setText(selectedPaymentMethod);
        tvStickyPayment.setText(selectedPaymentMethod);

        boolean canPlaceOrder = CheckoutSummary.canPlaceOrder(
                cartEmpty,
                hasDeliveryAddress,
                selectedPaymentMethod != null && !selectedPaymentMethod.trim().isEmpty(),
                isSubmitting
        );
        btnOrder.setEnabled(canPlaceOrder);
        btnOrder.setAlpha(canPlaceOrder ? 1f : 0.55f);
        if (isSubmitting) {
            btnOrder.setText("Đang đặt món...");
        } else if (cartEmpty) {
            btnOrder.setText("Giỏ hàng trống");
        } else if (!hasDeliveryAddress) {
            btnOrder.setText("Chọn địa chỉ");
        } else {
            btnOrder.setText("Đặt món");
        }
        setLoadingOverlayVisible(isSubmitting);
    }

    private void loadCartSummaryV3() {
        orderRepository.getCartSummaryV3(cartId).enqueue(new Callback<CartSummaryV3Response>() {
            @Override
            public void onResponse(Call<CartSummaryV3Response> call, Response<CartSummaryV3Response> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    tvEmptyCart.setVisibility(View.VISIBLE);
                    Toast.makeText(Checkout.this, "Khong the tai gio hang", Toast.LENGTH_SHORT).show();
                    refreshRpcCartUi();
                    return;
                }
                rpcSummary = response.body();
                refreshRpcCartUi();
            }

            @Override
            public void onFailure(Call<CartSummaryV3Response> call, Throwable t) {
                tvEmptyCart.setVisibility(View.VISIBLE);
                Toast.makeText(Checkout.this,
                        "Khong the tai gio hang: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                refreshRpcCartUi();
            }
        });
    }

    private void refreshRpcCartUi() {
        boolean cartEmpty = rpcSummary == null
                || rpcSummary.getItems() == null
                || rpcSummary.getItems().isEmpty();

        if (adapter != null) {
            adapter.updateData(cartEmpty ? new ArrayList<>() : RpcCartUiState.mapSummaryItems(rpcSummary.getItems()));
        }

        tvEmptyCart.setVisibility(cartEmpty ? View.VISIBLE : View.GONE);
        CheckoutSummary summary = buildSummary(rpcSummary);
        int itemCount = cartEmpty ? 0 : RpcCartUiState.itemCount(rpcSummary);

        tvSubtotalLabel.setText("Tam tinh (" + itemCount + " phan)");
        tvSubtotal.setText(formatPrice(summary.getSubtotal()));
        tvDeliveryFee.setText(formatPrice(summary.getDeliveryFee()));
        tvServiceFee.setText(formatPrice(summary.getServiceFee()));
        tvVoucherDiscount.setText("-" + formatPrice(summary.getVoucherDiscount()));
        tvTotalPrice.setText(formatPrice(summary.getTotal()));
        tvStickyTotal.setText(formatPrice(summary.getTotal()));
        tvPaymentMethod.setText(selectedPaymentMethod);
        tvStickyPayment.setText(selectedPaymentMethod);

        boolean canPlaceOrder = CheckoutSummary.canPlaceOrder(
                cartEmpty,
                hasDeliveryAddress,
                selectedPaymentMethod != null && !selectedPaymentMethod.trim().isEmpty(),
                isSubmitting
        );
        btnOrder.setEnabled(canPlaceOrder);
        btnOrder.setAlpha(canPlaceOrder ? 1f : 0.55f);
        if (isSubmitting) {
            btnOrder.setText("Đang đặt món...");
        } else if (cartEmpty) {
            btnOrder.setText("Giỏ hàng trống");
        } else if (!hasDeliveryAddress) {
            btnOrder.setText("Chọn địa chỉ");
        } else {
            btnOrder.setText("Đặt món");
        }
        setLoadingOverlayVisible(isSubmitting);
    }

    private CheckoutSummary buildSummary(LocalCart cart) {
        long subtotal = Math.round(cart.getTotalPrice(restaurantId));
        long deliveryFee = cart.isEmpty(restaurantId) ? 0 : CheckoutSummary.DEFAULT_DELIVERY_FEE;
        return new CheckoutSummary(
                subtotal,
                deliveryFee,
                CheckoutSummary.DEFAULT_SERVICE_FEE,
                CheckoutSummary.DEFAULT_VOUCHER_DISCOUNT
        );
    }

    private CheckoutSummary buildSummary(CartSummaryV3Response summary) {
        if (summary == null) {
            return new CheckoutSummary(0, 0, 0, 0);
        }
        return new CheckoutSummary(
                Math.round(summary.getSubtotal()),
                Math.round(summary.getDeliveryFee()),
                CheckoutSummary.DEFAULT_SERVICE_FEE,
                Math.round(summary.getDiscountAmount())
        );
    }

    private void showPaymentMethodSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(24, 18, 24, 24);

        TextView title = new TextView(this);
        title.setText("Phuong thuc thanh toan");
        title.setTextColor(getColor(android.R.color.black));
        title.setTextSize(18);
        title.setTypeface(title.getTypeface(), android.graphics.Typeface.BOLD);
        content.addView(title);

        addPaymentOption(content, dialog, PAYMENT_COD, true, true);
        addPaymentOption(content, dialog, "QR ngan hang", false, false);
        addPaymentOption(content, dialog, "MoMo", false, false);
        addPaymentOption(content, dialog, "ZaloPay", false, false);
        addPaymentOption(content, dialog, "The ngan hang", false, false);

        dialog.setContentView(content);
        dialog.show();
    }

    private void addPaymentOption(LinearLayout parent,
                                  BottomSheetDialog dialog,
                                  String label,
                                  boolean enabled,
                                  boolean selected) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        row.setPadding(0, 18, 0, 18);
        row.setAlpha(enabled ? 1f : 0.45f);

        TextView labelView = new TextView(this);
        labelView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        labelView.setText(enabled ? label : label + " - Sap ho tro");
        labelView.setTextColor(getColor(android.R.color.black));
        labelView.setTextSize(16);

        TextView stateView = new TextView(this);
        stateView.setText(selected ? "*" : ">");
        stateView.setTextColor(getColor(android.R.color.black));
        stateView.setTextSize(20);

        row.addView(labelView);
        row.addView(stateView);
        row.setOnClickListener(v -> {
            if (!enabled) {
                Toast.makeText(this, "Sap ho tro " + label, Toast.LENGTH_SHORT).show();
                return;
            }
            selectedPaymentMethod = label;
            refreshCartUi();
            dialog.dismiss();
        });
        parent.addView(row);
    }

    private void placeOrder() {
        if (isRpcCheckout()) {
            placeRpcOrder();
            return;
        }

        LocalCart cart = LocalCart.getInstance();
        if (!CheckoutSummary.canPlaceOrder(
                cart.isEmpty(restaurantId),
                hasDeliveryAddress,
                selectedPaymentMethod != null && !selectedPaymentMethod.trim().isEmpty(),
                isSubmitting)) {
            refreshCartUi();
            return;
        }

        isSubmitting = true;
        refreshCartUi();

        String deliveryAddress = FALLBACK_ADDRESS_LABEL + " - " + FALLBACK_ADDRESS_DETAIL;
        if (LocalOrderStore.getInstance().createFromCart(cart, restaurantId, deliveryAddress) == null) {
            isSubmitting = false;
            refreshCartUi();
            Toast.makeText(this, "Khong the tao don tu gio hang trong", Toast.LENGTH_SHORT).show();
            return;
        }

        cart.clearRestaurant(restaurantId);
        setLoadingOverlayVisible(false);
        showSuccessDialog(-1L);
    }

    private void placeRpcOrder() {
        boolean cartEmpty = rpcSummary == null
                || rpcSummary.getItems() == null
                || rpcSummary.getItems().isEmpty();
        if (!CheckoutSummary.canPlaceOrder(
                cartEmpty,
                hasDeliveryAddress,
                selectedPaymentMethod != null && !selectedPaymentMethod.trim().isEmpty(),
                isSubmitting)) {
            refreshRpcCartUi();
            return;
        }

        isSubmitting = true;
        refreshRpcCartUi();
        String note = edNote == null ? null : edNote.getText().toString();
        orderRepository.checkoutCartV3(cartId, deliveryAddressId, "COD", note)
                .enqueue(new Callback<Long>() {
                    @Override
                    public void onResponse(Call<Long> call, Response<Long> response) {
                        isSubmitting = false;
                        if (!response.isSuccessful() || response.body() == null) {
                            refreshRpcCartUi();
                            Toast.makeText(Checkout.this, "Khong the dat mon", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        setLoadingOverlayVisible(false);
                        showSuccessDialog(response.body());
                    }

                    @Override
                    public void onFailure(Call<Long> call, Throwable t) {
                        isSubmitting = false;
                        refreshRpcCartUi();
                        Toast.makeText(Checkout.this,
                                "Khong the dat mon: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showSuccessDialog(long orderId) {
        String message = orderId > 0
                ? "Dat mon thanh cong! Ma don hang: " + orderId
                : "Dat mon thanh cong!";
        new AlertDialog.Builder(this)
                .setTitle("Thong bao")
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    Intent intent = new Intent(Checkout.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    intent.putExtra("open_tab", "orders");
                    startActivity(intent);
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    private void setLoadingOverlayVisible(boolean visible) {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    private boolean isRpcCheckout() {
        return cartId > 0;
    }

    private void loadCurrentDeliveryAddress() {
        deliveryAddressRepository.getCurrentAddress(new DeliveryAddressRepository.ResultCallback<DeliveryAddress>() {
            @Override
            public void onSuccess(DeliveryAddress address) {
                if (address == null) {
                    hasDeliveryAddress = !isRpcCheckout();
                    refreshCartUi();
                    return;
                }

                tvAddressTitle.setText(address.getDisplayLabel());
                tvAddressSubtitle.setText(address.getFullAddress() == null
                        || address.getFullAddress().trim().isEmpty()
                        ? FALLBACK_ADDRESS_DETAIL
                        : address.getFullAddress());
                deliveryAddressId = parseAddressId(address.getId());
                hasDeliveryAddress = !isRpcCheckout() || deliveryAddressId > 0;
                refreshCartUi();
            }

            @Override
            public void onError(String message) {
                hasDeliveryAddress = !isRpcCheckout();
                refreshCartUi();
            }
        });
    }

    private long parseAddressId(String value) {
        if (value == null || value.trim().isEmpty()) return -1L;
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return -1L;
        }
    }

    private String formatPrice(long price) {
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        return formatter.format(price) + "d";
    }
}
