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

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fooddelivery.MainActivity;
import com.example.fooddelivery.R;
import com.example.fooddelivery.data.local.LocalCart;
import com.example.fooddelivery.data.local.LocalOrderStore;
import com.example.fooddelivery.ui.cart.adapters.CartBottomSheetAdapter;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Màn hình Thanh toán — đọc dữ liệu trực tiếp từ LocalCart (không cần Supabase).
 * Khi ấn "Đặt món":
 *   1. Tạo Order trong LocalOrderStore
 *   2. Xóa giỏ hàng
 *   3. Hiện dialog "Đặt món thành công"
 *   4. Chuyển về MainActivity và focus tab Đơn hàng
 */
public class Checkout extends AppCompatActivity {

    private static final String TABLE_INFO = "Bàn số 5 tầng 2";

    private CartBottomSheetAdapter adapter;
    private TextView tvTotalPrice, tvSavings, tvRestaurantName;
    private Button btnOrder;
    private EditText edNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cart_activity_checkout);
        initViews();
        renderCart();
    }

    private void initViews() {
        ImageView ivBack      = findViewById(R.id.ivBack);
        btnOrder              = findViewById(R.id.btnOrder);
        tvTotalPrice          = findViewById(R.id.tvTotalPrice);
        tvRestaurantName      = findViewById(R.id.tvRestaurantName);
        edNote                = findViewById(R.id.edNote);
        TextView tvAddMore    = findViewById(R.id.tvAddMore);

        ivBack.setOnClickListener(v -> finish());
        tvAddMore.setOnClickListener(v -> finish());

        btnOrder.setOnClickListener(v -> placeOrder());
    }

    /** Hiển thị danh sách món từ LocalCart và tổng tiền. */
    private void renderCart() {
        LocalCart cart = LocalCart.getInstance();

        // RecyclerView danh sách món
        RecyclerView rvCartItems = findViewById(R.id.rvCartItems);
        rvCartItems.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CartBottomSheetAdapter(
                this,
                cart.getEntries(),
                new CartBottomSheetAdapter.Listener() {
                    @Override
                    public void onIncrease(LocalCart.CartEntry entry) {
                        LocalCart.getInstance().increase(entry.item.getId());
                        refreshTotals();
                        adapter.updateData(LocalCart.getInstance().getEntries());
                    }

                    @Override
                    public void onDecrease(LocalCart.CartEntry entry) {
                        LocalCart.getInstance().decrease(entry.item.getId());
                        if (LocalCart.getInstance().isEmpty()) {
                            finish();
                            return;
                        }
                        refreshTotals();
                        adapter.updateData(LocalCart.getInstance().getEntries());
                    }
                });
        rvCartItems.setAdapter(adapter);

        // Tên nhà hàng / tiêu đề đơn
        if (tvRestaurantName != null) {
            tvRestaurantName.setText("Đơn hàng của bạn");
        }

        refreshTotals();
    }

    private void refreshTotals() {
        LocalCart cart = LocalCart.getInstance();
        double total = cart.getTotalPrice();
        tvTotalPrice.setText(formatPrice(total));

        // Cập nhật text nút
        if (cart.isEmpty()) {
            btnOrder.setEnabled(false);
            btnOrder.setText("Giỏ hàng trống");
        } else {
            btnOrder.setEnabled(true);
            btnOrder.setText("Đặt món");
        }
    }

    /** Xử lý khi ấn "Đặt món". */
    private void placeOrder() {
        LocalCart cart = LocalCart.getInstance();
        if (cart.isEmpty()) return;

        // 1. Tạo đơn hàng trong LocalOrderStore
        LocalOrderStore.getInstance().createFromCart(cart, TABLE_INFO);

        // 2. Xóa giỏ hàng
        cart.clear();

        // 3. Hiện dialog "Đặt món thành công"
        new AlertDialog.Builder(this)
                .setTitle("Thông báo")
                .setMessage("Đặt món thành công!")
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    // 4. Chuyển về MainActivity, mở tab Đơn hàng (index 1 trong bottom nav)
                    Intent intent = new Intent(Checkout.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    intent.putExtra("open_tab", "orders");
                    startActivity(intent);
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    private String formatPrice(double price) {
        long rounded = Math.round(price);
        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        return nf.format(rounded) + "đ";
    }
}