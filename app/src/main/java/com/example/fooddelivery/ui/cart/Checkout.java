package com.example.fooddelivery.ui.cart;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fooddelivery.MainActivity;
import com.example.fooddelivery.R;
import com.example.fooddelivery.data.model.CartSummaryResponse;
import com.example.fooddelivery.data.model.DeliveryAddress;
import com.example.fooddelivery.ui.cart.adapters.CartAdapter;

import java.text.NumberFormat;
import java.util.Locale;

public class Checkout extends AppCompatActivity {

    private CheckoutViewModel viewModel;
    private CartAdapter adapter;
    private DeliveryAddress selectedDeliveryAddress;
    private boolean hasCartItems;
    private TextView tvTotalPrice;
    private TextView tvRestaurantName;
    private TextView tvDeliveryAddress;
    private Button btnOrder;
    private EditText edNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cart_activity_checkout);
        initViews();
        initViewModel();
    }

    private void initViews() {
        ImageView ivBack = findViewById(R.id.ivBack);
        btnOrder = findViewById(R.id.btnOrder);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        tvRestaurantName = findViewById(R.id.tvRestaurantName);
        tvDeliveryAddress = findViewById(R.id.tvDeliveryAddress);
        edNote = findViewById(R.id.edNote);
        TextView tvAddMore = findViewById(R.id.tvAddMore);

        RecyclerView rvCartItems = findViewById(R.id.rvCartItems);
        rvCartItems.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CartAdapter(this, new CartAdapter.OnCartActionListener() {
            @Override
            public void onIncrease(com.example.fooddelivery.data.model.CartItem item) {
                viewModel.updateQuantity(String.valueOf(item.getCartId()), 0, item.getMenuId(), item.getQuantity() + 1);
            }

            @Override
            public void onDecrease(com.example.fooddelivery.data.model.CartItem item) {
                viewModel.updateQuantity(String.valueOf(item.getCartId()), 0, item.getMenuId(), item.getQuantity() - 1);
            }

            @Override
            public void onDelete(com.example.fooddelivery.data.model.CartItem item) {
                viewModel.deleteItem(String.valueOf(item.getCartId()));
            }
        });
        rvCartItems.setAdapter(adapter);

        ivBack.setOnClickListener(v -> finish());
        tvAddMore.setOnClickListener(v -> finish());
        btnOrder.setOnClickListener(v -> placeOrder());
    }

    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(CheckoutViewModel.class);
        viewModel.getCartSummary().observe(this, this::renderCartSummary);
        viewModel.getSelectedDeliveryAddress().observe(this, address -> {
            selectedDeliveryAddress = address;
            renderDeliveryAddress(address);
        });
        viewModel.getErrorMsg().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });
        viewModel.getOrderSuccess().observe(this, orderIds -> {
            if (orderIds != null && !orderIds.isEmpty()) {
                showCheckoutSuccess();
            }
        });
        viewModel.isLoading().observe(this, loading -> updateOrderButtonEnabled(Boolean.FALSE.equals(loading)));

        viewModel.loadCartSummary();
        viewModel.loadDefaultDeliveryAddress();
    }

    private void renderCartSummary(CartSummaryResponse summary) {
        if (summary == null) {
            return;
        }
        adapter.setCartItems(summary.getItems());
        tvTotalPrice.setText(formatPrice(summary.getNetTotal()));
        if (tvRestaurantName != null) {
            tvRestaurantName.setText("Don hang cua ban");
        }

        hasCartItems = summary.getItems() != null && !summary.getItems().isEmpty();
        updateOrderButtonEnabled(true);
        btnOrder.setText(hasCartItems ? "Dat mon" : "Gio hang trong");
    }

    private void renderDeliveryAddress(DeliveryAddress address) {
        if (address == null) {
            tvDeliveryAddress.setText("Chua co DeliveryAddress");
        } else {
            tvDeliveryAddress.setText(address.toCheckoutDisplayText());
        }
        updateOrderButtonEnabled(true);
    }

    private void placeOrder() {
        viewModel.checkout(selectedDeliveryAddress, edNote.getText().toString());
    }

    private void updateOrderButtonEnabled(boolean notLoading) {
        btnOrder.setEnabled(notLoading && hasCartItems && selectedDeliveryAddress != null);
    }

    private void showCheckoutSuccess() {
        new AlertDialog.Builder(this)
                .setTitle("Thong bao")
                .setMessage("Dat mon thanh cong!")
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    Intent intent = new Intent(Checkout.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    intent.putExtra("open_tab", "orders");
                    intent.putExtra("orders_tab", "pending");
                    startActivity(intent);
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    private String formatPrice(double price) {
        long rounded = Math.round(price);
        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        return nf.format(rounded) + "d";
    }

    // LocalCart is a legacy fallback from the old demo path; LocalOrderStore must not create real checkout Orders.
}
