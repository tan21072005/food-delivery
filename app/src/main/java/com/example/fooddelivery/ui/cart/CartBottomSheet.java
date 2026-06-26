package com.example.fooddelivery.ui.cart;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fooddelivery.R;
import com.example.fooddelivery.data.local.LocalCart;
import com.example.fooddelivery.ui.cart.adapters.CartBottomSheetAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * CartBottomSheet — hiện khi user ấn nút [+] trên bất kỳ màn hình nào.
 *
 * Cách dùng:
 *   CartBottomSheet sheet = new CartBottomSheet();
 *   sheet.show(getParentFragmentManager(), CartBottomSheet.TAG);
 */
public class CartBottomSheet extends BottomSheetDialogFragment {

    public static final String TAG = "CartBottomSheet";

    private CartBottomSheetAdapter adapter;
    private TextView tvViewOrder;
    private TextView tvCartCount;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.cart_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rv          = view.findViewById(R.id.rvCartItems);
        Button btnViewOrder      = view.findViewById(R.id.btnViewOrder);
        TextView tvClearAll      = view.findViewById(R.id.tvClearAll);
        TextView tvClose         = view.findViewById(R.id.tvClose);
        tvCartCount              = view.findViewById(R.id.tvCartCount);

        // Setup RecyclerView
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new CartBottomSheetAdapter(
                requireContext(),
                LocalCart.getInstance().getEntries(),
                new CartBottomSheetAdapter.Listener() {
                    @Override
                    public void onIncrease(LocalCart.CartEntry entry) {
                        LocalCart.getInstance().increase(entry.item.getId());
                        refreshCart();
                    }

                    @Override
                    public void onDecrease(LocalCart.CartEntry entry) {
                        LocalCart.getInstance().decrease(entry.item.getId());
                        if (LocalCart.getInstance().isEmpty()) {
                            dismiss();
                            return;
                        }
                        refreshCart();
                    }
                });
        rv.setAdapter(adapter);

        // "Xóa hết"
        tvClearAll.setOnClickListener(v -> {
            LocalCart.getInstance().clear();
            dismiss();
        });

        // "✕ Đóng"
        tvClose.setOnClickListener(v -> dismiss());

        // "Xem đơn hàng"
        btnViewOrder.setOnClickListener(v -> {
            dismiss();
            Intent intent = new Intent(requireContext(), Checkout.class);
            startActivity(intent);
        });

        refreshCart();
    }

    /** Cập nhật giao diện sau khi thay đổi giỏ hàng. */
    private void refreshCart() {
        LocalCart cart = LocalCart.getInstance();
        adapter.updateData(cart.getEntries());

        int count = cart.getTotalCount();
        tvCartCount.setText(String.valueOf(count));

        // Cập nhật text nút "Xem đơn hàng"
        Button btnViewOrder = requireView().findViewById(R.id.btnViewOrder);
        String priceText = formatPrice(cart.getTotalPrice());
        btnViewOrder.setText("Xem đơn hàng  •  " + priceText);
    }

    private String formatPrice(double price) {
        long rounded = Math.round(price);
        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        return nf.format(rounded) + "đ";
    }
}
