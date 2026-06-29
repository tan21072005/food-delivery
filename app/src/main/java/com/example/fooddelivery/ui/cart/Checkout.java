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
import com.example.fooddelivery.ui.cart.adapters.CartBottomSheetAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.text.NumberFormat;
import java.util.Locale;

public class Checkout extends AppCompatActivity {

    private static final String FALLBACK_ADDRESS_LABEL = "Nhà";
    private static final String FALLBACK_ADDRESS_DETAIL = "Số 1 Đại Cồ Việt, Hai Bà Trưng, Hà Nội";
    private static final String PAYMENT_COD = "Thanh toán khi nhận hàng";

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

    private long restaurantId = -1L;
    private boolean isSubmitting = false;
    private boolean hasDeliveryAddress = true;
    private String selectedPaymentMethod = PAYMENT_COD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cart_activity_checkout);
        restaurantId = getIntent().getLongExtra("restaurant_id", LocalCart.getInstance().getRestaurantId());
        LocalCart.getInstance().setActiveRestaurantId(restaurantId);
        initViews();
        renderCart();
    }

    private void initViews() {
        ImageView ivBack = findViewById(R.id.ivBack);
        TextView tvAddMore = findViewById(R.id.tvAddMore);
        TextView tvSchedule = findViewById(R.id.tvSchedule);
        TextView tvChangeAddress = findViewById(R.id.tvChangeAddress);
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

        ivBack.setOnClickListener(v -> finish());
        tvAddMore.setOnClickListener(v -> finish());
        tvSchedule.setOnClickListener(v ->
                Toast.makeText(this, "Sắp hỗ trợ hẹn giờ giao", Toast.LENGTH_SHORT).show());
        tvChangeAddress.setOnClickListener(v ->
                Toast.makeText(this, "Đang dùng địa chỉ giao hàng local demo", Toast.LENGTH_SHORT).show());
        sectionVoucher.setOnClickListener(v ->
                Toast.makeText(this, "Voucher sẽ được hỗ trợ khi backend sẵn sàng", Toast.LENGTH_SHORT).show());
        sectionPayment.setOnClickListener(v -> showPaymentMethodSheet());
        btnOrder.setOnClickListener(v -> placeOrder());

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
                LocalCart.getInstance().getEntries(restaurantId),
                new CartBottomSheetAdapter.Listener() {
                    @Override
                    public void onIncrease(LocalCart.CartEntry entry) {
                        LocalCart.getInstance().increase(restaurantId, entry.item.getId());
                        refreshCartUi();
                    }

                    @Override
                    public void onDecrease(LocalCart.CartEntry entry) {
                        LocalCart.getInstance().decrease(restaurantId, entry.item.getId());
                        refreshCartUi();
                    }
                });
        rvCartItems.setAdapter(adapter);

        tvRestaurantName.setText("Đơn hàng của bạn");
        refreshCartUi();
    }

    private void refreshCartUi() {
        LocalCart cart = LocalCart.getInstance();
        if (adapter != null) {
            adapter.updateData(cart.getEntries(restaurantId));
        }

        boolean cartEmpty = cart.isEmpty(restaurantId);
        tvEmptyCart.setVisibility(cartEmpty ? View.VISIBLE : View.GONE);
        CheckoutSummary summary = buildSummary(cart);

        tvSubtotalLabel.setText("Tạm tính (" + cart.getTotalCount(restaurantId) + " phần)");
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

    private void showPaymentMethodSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(24, 18, 24, 24);

        TextView title = new TextView(this);
        title.setText("Phương thức thanh toán");
        title.setTextColor(getColor(android.R.color.black));
        title.setTextSize(18);
        title.setTypeface(title.getTypeface(), android.graphics.Typeface.BOLD);
        content.addView(title);

        addPaymentOption(content, dialog, PAYMENT_COD, true, true);
        addPaymentOption(content, dialog, "QR ngân hàng", false, false);
        addPaymentOption(content, dialog, "MoMo", false, false);
        addPaymentOption(content, dialog, "ZaloPay", false, false);
        addPaymentOption(content, dialog, "Thẻ ngân hàng", false, false);

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
        labelView.setText(enabled ? label : label + " · Sắp hỗ trợ");
        labelView.setTextColor(getColor(android.R.color.black));
        labelView.setTextSize(16);

        TextView stateView = new TextView(this);
        stateView.setText(selected ? "✓" : "›");
        stateView.setTextColor(getColor(android.R.color.black));
        stateView.setTextSize(20);

        row.addView(labelView);
        row.addView(stateView);
        row.setOnClickListener(v -> {
            if (!enabled) {
                Toast.makeText(this, "Sắp hỗ trợ " + label, Toast.LENGTH_SHORT).show();
                return;
            }
            selectedPaymentMethod = label;
            refreshCartUi();
            dialog.dismiss();
        });
        parent.addView(row);
    }

    private void placeOrder() {
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
            Toast.makeText(this, "Không thể tạo đơn từ giỏ hàng trống", Toast.LENGTH_SHORT).show();
            return;
        }

        cart.clearRestaurant(restaurantId);
        showSuccessDialog();
    }

    private void showSuccessDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Thông báo")
                .setMessage("Đặt món thành công!")
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

    private String formatPrice(long price) {
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        return formatter.format(price) + "đ";
    }
}