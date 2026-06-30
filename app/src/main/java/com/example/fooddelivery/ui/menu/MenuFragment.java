package com.example.fooddelivery.ui.menu;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fooddelivery.R;
import com.example.fooddelivery.data.local.LocalCart;
import com.example.fooddelivery.data.model.CartSummaryV3Response;
import com.example.fooddelivery.data.model.DraftCartV3Response;
import com.example.fooddelivery.data.model.FoodItem;
import com.example.fooddelivery.data.repository.OrderRepository;
import com.example.fooddelivery.ui.cart.CartBottomSheet;
import com.example.fooddelivery.ui.cart.RpcCartUiState;
import com.example.fooddelivery.ui.menu.adapters.MenuAdapter;
import com.example.fooddelivery.utils.MoneyFormatter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MenuFragment extends Fragment {

    private MenuViewModel viewModel;
    private OrderRepository orderRepository;
    private MenuAdapter adapter;
    private ProgressBar progressBar;
    private long activeCartId = -1L;
    private long activeCartRestaurantId = -1L;
    private int activeCartItemCount = 0;
    private double activeCartTotal = 0;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.menu_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(MenuViewModel.class);
        orderRepository = new OrderRepository(requireContext());
        progressBar = view.findViewById(R.id.progressBar);

        setupHeader(view);
        setupList(view);
        observeViewModel();
        getParentFragmentManager().setFragmentResultListener("cart_changed", getViewLifecycleOwner(),
                (requestKey, result) -> refreshDraftCartState(
                        view,
                        result.getLong("restaurant_id", -1L),
                        result.getLong("cart_id", -1L),
                        false
                ));

        String categorySlug = getArguments() != null ? getArguments().getString("category_slug", "") : "";
        viewModel.loadFoods(categorySlug);

        refreshDraftCartState(view, -1L, -1L, false);
    }

    private void setupHeader(View view) {
        TextView title = view.findViewById(R.id.tvMenuTitle);
        String categoryName = getArguments() != null ? getArguments().getString("category_name", "Tất cả món") : "Tất cả món";
        title.setText(categoryName == null || categoryName.isEmpty() ? "Tất cả món" : categoryName);

        view.findViewById(R.id.btnBack).setOnClickListener(v ->
                Navigation.findNavController(requireView()).navigateUp()
        );
    }

    private void setupList(View view) {
        RecyclerView rvMenuItems = view.findViewById(R.id.rvMenuItems);
        adapter = new MenuAdapter(requireContext(), new ArrayList<>());
        adapter.setOnItemClickListener(new MenuAdapter.OnItemClickListener() {
            @Override
            public void onFoodClick(FoodItem item) {
                openFoodDetail(view, item);
            }

            @Override
            public void onAddToCartClick(FoodItem item) {
                openFoodDetail(view, item);
            }
        });

        rvMenuItems.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvMenuItems.setAdapter(adapter);
    }

    private void openFoodDetail(View view, FoodItem item) {
        Bundle args = new Bundle();
        args.putLong("food_id", item.getId());
        Navigation.findNavController(view)
                .navigate(R.id.action_menu_to_foodDetail, args);
    }

    private void observeViewModel() {
        viewModel.isLoading().observe(getViewLifecycleOwner(), loading ->
                progressBar.setVisibility(Boolean.TRUE.equals(loading) ? View.VISIBLE : View.GONE)
        );

        viewModel.getFoodItems().observe(getViewLifecycleOwner(), items ->
                adapter.setData(items == null ? new ArrayList<>() : items)
        );

        viewModel.getCartMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void refreshDraftCartState(View view,
                                       long preferredRestaurantId,
                                       long fallbackCartId,
                                       boolean showAddedToast) {
        orderRepository.getDraftCartsV3().enqueue(new Callback<List<DraftCartV3Response>>() {
            @Override
            public void onResponse(@NonNull Call<List<DraftCartV3Response>> call,
                                   @NonNull Response<List<DraftCartV3Response>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful()) {
                    DraftCartV3Response draft = RpcCartUiState.selectActiveDraft(
                            response.body(),
                            preferredRestaurantId
                    );
                    if (draft != null) {
                        activeCartId = draft.getCartId();
                        activeCartRestaurantId = draft.getRestaurantId();
                        activeCartItemCount = RpcCartUiState.itemCount(draft);
                        activeCartTotal = RpcCartUiState.totalAmount(draft);
                        updateStickyCart(view);
                        finishAddSuccess(showAddedToast);
                        return;
                    }
                }
                if (fallbackCartId > 0) {
                    refreshStickyFromSummary(view, preferredRestaurantId, fallbackCartId, showAddedToast);
                    return;
                }
                updateStickyCart(view);
            }

            @Override
            public void onFailure(@NonNull Call<List<DraftCartV3Response>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                if (fallbackCartId > 0) {
                    refreshStickyFromSummary(view, preferredRestaurantId, fallbackCartId, showAddedToast);
                } else if (showAddedToast) {
                    Toast.makeText(requireContext(), "Da them mon, nhung chua tai duoc gio", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void refreshStickyFromSummary(View view,
                                          long preferredRestaurantId,
                                          long cartId,
                                          boolean showAddedToast) {
        orderRepository.getCartSummaryV3(cartId).enqueue(new Callback<CartSummaryV3Response>() {
            @Override
            public void onResponse(@NonNull Call<CartSummaryV3Response> call,
                                   @NonNull Response<CartSummaryV3Response> response) {
                if (!isAdded()) return;
                activeCartId = cartId;
                activeCartRestaurantId = preferredRestaurantId;
                if (response.isSuccessful() && response.body() != null) {
                    activeCartItemCount = RpcCartUiState.itemCount(response.body());
                    activeCartTotal = RpcCartUiState.totalAmount(response.body());
                }
                updateStickyCart(view);
                finishAddSuccess(showAddedToast);
            }

            @Override
            public void onFailure(@NonNull Call<CartSummaryV3Response> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                activeCartId = cartId;
                activeCartRestaurantId = preferredRestaurantId;
                updateStickyCart(view);
                finishAddSuccess(showAddedToast);
            }
        });
    }

    private void finishAddSuccess(boolean showAddedToast) {
        if (showAddedToast) {
            Toast.makeText(requireContext(), "Da them mon vao gio", Toast.LENGTH_SHORT).show();
        }
    }

    public void updateStickyCart(View view) {
        if (view == null) return;
        View stickyCart = view.findViewById(R.id.layoutStickyCart);
        if (stickyCart == null) return;

        long stickyRestaurantId = activeCartRestaurantId > 0
                ? activeCartRestaurantId
                : LocalCart.getInstance().getRestaurantId();
        int count = activeCartId > 0
                ? activeCartItemCount
                : LocalCart.getInstance().getTotalCount(stickyRestaurantId);
        if (count <= 0) {
            stickyCart.setVisibility(View.GONE);
            return;
        }

        stickyCart.setVisibility(View.VISIBLE);
        TextView tvCount = stickyCart.findViewById(R.id.tvStickyCartCount);
        TextView tvTotal = stickyCart.findViewById(R.id.tvStickyCartTotal);

        if (tvCount != null) tvCount.setText(String.valueOf(count));
        if (tvTotal != null) {
            double total = activeCartId > 0
                    ? activeCartTotal
                    : LocalCart.getInstance().getTotalPrice(stickyRestaurantId);
            tvTotal.setText(MoneyFormatter.format(total));
        }

        stickyCart.setOnClickListener(v -> {
            LocalCart.getInstance().setActiveRestaurantId(stickyRestaurantId);
            CartBottomSheet sheet = new CartBottomSheet(() -> updateStickyCart(view));
            if (activeCartId > 0) {
                Bundle args = new Bundle();
                args.putLong("cart_id", activeCartId);
                args.putLong("restaurant_id", stickyRestaurantId);
                sheet.setArguments(args);
            }
            sheet.show(getParentFragmentManager(), CartBottomSheet.TAG);
        });
    }
}
