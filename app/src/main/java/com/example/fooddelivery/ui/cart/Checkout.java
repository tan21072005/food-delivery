package com.example.fooddelivery.ui.cart;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
import com.example.fooddelivery.data.local.prefs.SessionManager;
import com.example.fooddelivery.data.model.CartItem;
import com.example.fooddelivery.ui.cart.adapters.CartAdapter;

import java.text.NumberFormat;
import java.util.Locale;

public class Checkout extends AppCompatActivity {

    private CheckoutViewModel viewModel;
    private CartAdapter cartAdapter;
    private SessionManager sessionManager;

    private TextView tvTotalPrice, tvRestaurantName;
    private Button btnOrder;
    private EditText edNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cart_activity_checkout);

        sessionManager = new SessionManager(this);
        viewModel = new ViewModelProvider(this).get(CheckoutViewModel.class);

        initViews();
        setupRecyclerView();
        observeViewModel();

        // Tải giỏ hàng
        viewModel.loadCartSummary();
    }

    private void initViews() {
        ImageView ivBack = findViewById(R.id.ivBack);
        btnOrder = findViewById(R.id.btnOrder);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        tvRestaurantName = findViewById(R.id.tvRestaurantName);
        edNote = findViewById(R.id.edNote);
        TextView tvAddMore = findViewById(R.id.tvAddMore);

        ivBack.setOnClickListener(v -> finish());
        
        tvAddMore.setOnClickListener(v -> finish()); // Quay lại để thêm món

        btnOrder.setOnClickListener(v -> {
            String note = edNote.getText().toString();
            // Địa chỉ cứng tạm thời, có thể lấy từ bảng addresses sau
            String deliveryAddress = "Bàn số 5 tầng 2"; 
            viewModel.checkout(deliveryAddress, note);
        });
    }

    private void setupRecyclerView() {
        RecyclerView rvCartItems = findViewById(R.id.rvCartItems);
        rvCartItems.setLayoutManager(new LinearLayoutManager(this));

        cartAdapter = new CartAdapter(this, new CartAdapter.OnCartActionListener() {
            @Override
            public void onIncrease(CartItem item) {
                viewModel.updateQuantity(String.valueOf(item.getCartId()), sessionManager.getUserId(), item.getMenuId(), item.getQuantity() + 1);
            }

            @Override
            public void onDecrease(CartItem item) {
                viewModel.updateQuantity(String.valueOf(item.getCartId()), sessionManager.getUserId(), item.getMenuId(), item.getQuantity() - 1);
            }

            @Override
            public void onDelete(CartItem item) {
                viewModel.deleteItem(String.valueOf(item.getCartId()));
            }
        });
        rvCartItems.setAdapter(cartAdapter);
    }

    private void observeViewModel() {
        viewModel.getCartSummary().observe(this, summary -> {
            if (summary != null) {
                cartAdapter.setCartItems(summary.getItems());
                
                NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
                tvTotalPrice.setText(formatter.format(summary.getNetTotal()));
                
                if (summary.getItems().isEmpty()) {
                    btnOrder.setEnabled(false);
                    btnOrder.setText("Giỏ hàng trống");
                } else {
                    btnOrder.setEnabled(true);
                    btnOrder.setText("Đặt món");
                }
            }
        });

        viewModel.getErrorMsg().observe(this, msg -> {
            if (msg != null) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getOrderSuccess().observe(this, orderId -> {
            if (orderId != null) {
                new AlertDialog.Builder(this)
                        .setTitle("Thông báo")
                        .setMessage("Đặt món thành công! Mã đơn: #" + orderId)
                        .setPositiveButton("OK", (dialog, which) -> {
                            dialog.dismiss();
                            Intent intent = new Intent(Checkout.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        })
                        .setCancelable(false)
                        .show();
            }
        });
    }
}