package com.example.fooddelivery.ui.cart;

import android.content.Intent;
import android.app.Dialog;
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
import com.example.fooddelivery.data.model.CartSummaryV3Response;
import com.example.fooddelivery.data.repository.OrderRepository;
import com.example.fooddelivery.ui.cart.adapters.CartBottomSheetAdapter;
import com.example.fooddelivery.utils.MoneyFormatter;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartBottomSheet extends BottomSheetDialogFragment {

    public static final String TAG = "CartBottomSheet";

    private CartBottomSheetAdapter adapter;
    private TextView tvCartCount;
    private TextView tvClearAll;
    private Button btnViewOrder;
    private long restaurantId;
    private long cartId = -1L;
    private CartSummaryV3Response rpcSummary;
    private OrderRepository orderRepository;
    private OnCartChangedListener onCartChangedListener;
    private boolean isMutating = false;

    public interface OnCartChangedListener {
        void onCartChanged();
    }

    public CartBottomSheet() {
    }

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
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (!(dialog instanceof BottomSheetDialog)) return;

        BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialog;
        FrameLayout bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet == null) return;

        ViewGroup.LayoutParams params = bottomSheet.getLayoutParams();
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        bottomSheet.setLayoutParams(params);

        BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
        behavior.setSkipCollapsed(true);
        behavior.setPeekHeight(getResources().getDisplayMetrics().heightPixels);
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rv = view.findViewById(R.id.rvCartItems);
        btnViewOrder = view.findViewById(R.id.btnViewOrder);
        tvClearAll = view.findViewById(R.id.tvClearAll);
        TextView tvClose = view.findViewById(R.id.tvClose);
        tvCartCount = view.findViewById(R.id.tvCartCount);
        orderRepository = new OrderRepository(requireContext());

        restaurantId = LocalCart.getInstance().getRestaurantId();
        if (getArguments() != null) {
            cartId = getArguments().getLong("cart_id", -1L);
            restaurantId = getArguments().getLong("restaurant_id", restaurantId);
        }

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new CartBottomSheetAdapter(
                requireContext(),
                isRpcCart() ? new ArrayList<>() : LocalCart.getInstance().getEntries(restaurantId),
                new CartBottomSheetAdapter.Listener() {
                    @Override
                    public void onIncrease(LocalCart.CartEntry entry) {
                        if (isRpcCart()) {
                            updateRpcCartItemQuantity(entry, entry.quantity + 1);
                            return;
                        }
                        LocalCart.getInstance().increase(restaurantId, entry.item.getId());
                        notifyCartChanged();
                        refreshCart();
                    }

                    @Override
                    public void onDecrease(LocalCart.CartEntry entry) {
                        if (isRpcCart()) {
                            if (entry.quantity > 1) {
                                updateRpcCartItemQuantity(entry, entry.quantity - 1);
                            } else {
                                removeRpcCartItem(entry);
                            }
                            return;
                        }
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

        tvClearAll.setVisibility(isRpcCart() || !LocalCart.getInstance().isEmpty(restaurantId)
                ? View.VISIBLE
                : View.GONE);
        tvClearAll.setOnClickListener(v -> {
            if (isRpcCart()) {
                clearRpcCart();
                return;
            }
            LocalCart.getInstance().clearRestaurant(restaurantId);
            notifyCartChanged();
            dismiss();
        });

        tvClose.setOnClickListener(v -> dismiss());

        btnViewOrder.setOnClickListener(v -> {
            if (!isRpcCart() && LocalCart.getInstance().isEmpty(restaurantId)) {
                refreshCart();
                return;
            }
            if (isRpcCart() && RpcCartUiState.itemCount(rpcSummary) <= 0) {
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
        if (isRpcCart()) {
            if (rpcSummary == null) {
                loadCartSummaryV3();
            } else {
                refreshRpcCart();
            }
            return;
        }

        LocalCart cart = LocalCart.getInstance();
        int count = cart.getTotalCount(restaurantId);
        if (adapter != null) {
            adapter.updateData(cart.getEntries(restaurantId));
        }
        tvCartCount.setText(String.valueOf(count));

        Button btnViewOrder = requireView().findViewById(R.id.btnViewOrder);
        boolean canViewOrder = count > 0;
        btnViewOrder.setEnabled(count > 0);
        btnViewOrder.setAlpha(canViewOrder ? 1f : 0.55f);
        btnViewOrder.setText("Xem don hang - " + MoneyFormatter.format(cart.getTotalPrice(restaurantId)));
    }

    private void loadCartSummaryV3() {
        orderRepository.getCartSummaryV3(cartId).enqueue(new Callback<CartSummaryV3Response>() {
            @Override
            public void onResponse(@NonNull Call<CartSummaryV3Response> call,
                                   @NonNull Response<CartSummaryV3Response> response) {
                if (!isAdded()) return;
                rpcSummary = response.isSuccessful() ? response.body() : null;
                refreshRpcCart();
            }

            @Override
            public void onFailure(@NonNull Call<CartSummaryV3Response> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                rpcSummary = null;
                refreshRpcCart();
            }
        });
    }

    private void refreshRpcCart() {
        int count = RpcCartUiState.itemCount(rpcSummary);
        if (adapter != null) {
            adapter.updateData(rpcSummary == null
                    ? new ArrayList<>()
                    : RpcCartUiState.mapSummaryItems(rpcSummary.getItems()));
        }
        tvCartCount.setText(String.valueOf(count));

        Button btnViewOrder = requireView().findViewById(R.id.btnViewOrder);
        boolean canViewOrder = count > 0;
        btnViewOrder.setEnabled(count > 0);
        btnViewOrder.setAlpha(canViewOrder ? 1f : 0.55f);
        btnViewOrder.setText("Xem don hang - " + MoneyFormatter.format(RpcCartUiState.totalAmount(rpcSummary)));
    }

    private void updateRpcCartItemQuantity(LocalCart.CartEntry entry, int quantity) {
        if (isMutating || entry == null || entry.cartItemId <= 0 || quantity <= 0) return;
        isMutating = true;
        setCartActionsEnabled(false);
        orderRepository.updateCartItemQuantityV3(entry.cartItemId, quantity).enqueue(new Callback<Long>() {
            @Override
            public void onResponse(@NonNull Call<Long> call, @NonNull Response<Long> response) {
                if (!isAdded()) return;
                isMutating = false;
                setCartActionsEnabled(true);
                if (response.isSuccessful()) {
                    notifyCartChanged();
                    loadCartSummaryV3();
                } else {
                    refreshRpcCart();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Long> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                isMutating = false;
                setCartActionsEnabled(true);
                refreshRpcCart();
            }
        });
    }

    private void removeRpcCartItem(LocalCart.CartEntry entry) {
        if (isMutating || entry == null || entry.cartItemId <= 0) return;
        isMutating = true;
        setCartActionsEnabled(false);
        orderRepository.removeCartItemV3(entry.cartItemId).enqueue(new Callback<Long>() {
            @Override
            public void onResponse(@NonNull Call<Long> call, @NonNull Response<Long> response) {
                if (!isAdded()) return;
                isMutating = false;
                setCartActionsEnabled(true);
                if (response.isSuccessful()) {
                    notifyCartChanged();
                    loadCartSummaryV3();
                } else {
                    refreshRpcCart();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Long> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                isMutating = false;
                setCartActionsEnabled(true);
                refreshRpcCart();
            }
        });
    }

    private void clearRpcCart() {
        if (isMutating || cartId <= 0) return;
        isMutating = true;
        setCartActionsEnabled(false);
        orderRepository.clearCartV3(cartId).enqueue(new Callback<Long>() {
            @Override
            public void onResponse(@NonNull Call<Long> call, @NonNull Response<Long> response) {
                if (!isAdded()) return;
                isMutating = false;
                setCartActionsEnabled(true);
                if (response.isSuccessful()) {
                    notifyCartChanged();
                    dismiss();
                } else {
                    refreshRpcCart();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Long> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                isMutating = false;
                setCartActionsEnabled(true);
                refreshRpcCart();
            }
        });
    }

    private void setCartActionsEnabled(boolean enabled) {
        if (btnViewOrder != null) {
            btnViewOrder.setEnabled(enabled && RpcCartUiState.itemCount(rpcSummary) > 0);
            btnViewOrder.setAlpha(enabled ? 1f : 0.55f);
        }
        if (tvClearAll != null) {
            tvClearAll.setEnabled(enabled);
            tvClearAll.setAlpha(enabled ? 1f : 0.45f);
        }
    }

    private boolean isRpcCart() {
        return cartId > 0;
    }

    private void notifyCartChanged() {
        if (onCartChangedListener != null) {
            onCartChangedListener.onCartChanged();
        }
    }
}
