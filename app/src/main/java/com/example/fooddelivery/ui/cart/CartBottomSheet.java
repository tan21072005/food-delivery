package com.example.fooddelivery.ui.cart;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fooddelivery.R;
import com.example.fooddelivery.data.local.LocalCart;
import com.example.fooddelivery.ui.cart.adapters.CartBottomSheetAdapter;
import com.example.fooddelivery.utils.MoneyFormatter;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class CartBottomSheet extends BottomSheetDialogFragment {

    public static final String TAG = "CartBottomSheet";

    private CartBottomSheetAdapter adapter;
    private TextView tvCartCount;
    private long restaurantId;
    private long cartId = -1L;
    private OnCartChangedListener onCartChangedListener;

    public interface OnCartChangedListener {
        void onCartChanged();
    }

    public CartBottomSheet() {}

    public CartBottomSheet(OnCartChangedListener onCartChangedListener) {
        this.onCartChangedListener = onCartChangedListener;
    }

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

        RecyclerView rv = view.findViewById(R.id.rvCartItems);
        Button btnViewOrder = view.findViewById(R.id.btnViewOrder);
        TextView tvClearAll = view.findViewById(R.id.tvClearAll);
        TextView tvClose = view.findViewById(R.id.tvClose);
        tvCartCount = view.findViewById(R.id.tvCartCount);
        restaurantId = LocalCart.getInstance().getRestaurantId();
        if (getArguments() != null) {
            cartId = getArguments().getLong("cart_id", -1L);
        }

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new CartBottomSheetAdapter(
                requireContext(),
                LocalCart.getInstance().getEntries(restaurantId),
                new CartBottomSheetAdapter.Listener() {
                    @Override
                    public void onIncrease(LocalCart.CartEntry entry) {
                        LocalCart.getInstance().increase(restaurantId, entry.item.getId());
                        notifyCartChanged();
                        refreshCart();
                    }

                    @Override
                    public void onDecrease(LocalCart.CartEntry entry) {
                        LocalCart.getInstance().decrease(restaurantId, entry.item.getId());
                        notifyCartChanged();
                        if (LocalCart.getInstance().isEmpty(restaurantId)) {
                            dismiss();
                            return;
                        }
                        refreshCart();
                    }
                });
        rv.setAdapter(adapter);

        tvClearAll.setOnClickListener(v -> {
            LocalCart.getInstance().clearRestaurant(restaurantId);
            notifyCartChanged();
            dismiss();
        });

        tvClose.setOnClickListener(v -> dismiss());

        btnViewOrder.setOnClickListener(v -> {
            if (cartId <= 0 && LocalCart.getInstance().isEmpty(restaurantId)) {
                refreshCart();
                return;
            }
            dismiss();
            Intent intent = new Intent(requireContext(), Checkout.class);
            intent.putExtra("restaurant_id", restaurantId);
            if (cartId > 0) {
                intent.putExtra("cart_id", cartId);
            }
            startActivity(intent);
        });

        refreshCart();
    }

    private void refreshCart() {
        LocalCart cart = LocalCart.getInstance();
        int count = cart.getTotalCount(restaurantId);
        if (adapter != null) {
            adapter.updateData(cart.getEntries(restaurantId));
        }
        tvCartCount.setText(String.valueOf(count));

        Button btnViewOrder = requireView().findViewById(R.id.btnViewOrder);
        boolean canViewOrder = count > 0 || cartId > 0;
        btnViewOrder.setEnabled(canViewOrder);
        btnViewOrder.setAlpha(canViewOrder ? 1f : 0.55f);
        btnViewOrder.setText("Xem đơn hàng  •  " + MoneyFormatter.format(cart.getTotalPrice(restaurantId)));
    }

    private void notifyCartChanged() {
        if (onCartChangedListener != null) {
            onCartChangedListener.onCartChanged();
        }
    }
}
