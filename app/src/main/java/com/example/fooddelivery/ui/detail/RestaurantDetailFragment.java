package com.example.fooddelivery.ui.detail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fooddelivery.R;
import com.example.fooddelivery.data.local.LocalCart;
import com.example.fooddelivery.data.model.CartSummaryV3Response;
import com.example.fooddelivery.data.model.DraftCartV3Response;
import com.example.fooddelivery.data.model.FoodItem;
import com.example.fooddelivery.data.repository.OrderRepository;
import com.example.fooddelivery.ui.cart.CartBottomSheet;
import com.example.fooddelivery.ui.cart.RpcCartUiState;
import com.example.fooddelivery.ui.detail.adapters.StorefrontAdapter;
import com.example.fooddelivery.utils.MoneyFormatter;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RestaurantDetailFragment extends Fragment {

    private RestaurantDetailViewModel viewModel;
    private OrderRepository orderRepository;
    private StorefrontAdapter adapter;
    private long restaurantId = -1L;
    private long activeCartId = -1L;
    private long activeCartRestaurantId = -1L;
    private int activeCartItemCount = 0;
    private double activeCartTotal = 0;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_restaurant_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(RestaurantDetailViewModel.class);
        orderRepository = new OrderRepository(requireContext());

        setupToolbar(view);
        setupHeaderActions(view);
        setupFoodGrid(view);
        observeViewModel();
        getParentFragmentManager().setFragmentResultListener("cart_changed", getViewLifecycleOwner(),
                (requestKey, result) -> refreshDraftCartState(
                        view,
                        result.getLong("restaurant_id", restaurantId),
                        result.getLong("cart_id", -1L),
                        false
                ));

        restaurantId = getArguments() != null
                ? getArguments().getLong("restaurant_id", -1L)
                : -1L;
        viewModel.loadRestaurantFoods(restaurantId);
        refreshDraftCartState(view, restaurantId, -1L, false);
    }

    private void setupToolbar(View view) {
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).popBackStack());
    }

    private void setupHeaderActions(View view) {
        View tvRestaurantName = view.findViewById(R.id.tvRestaurantName);
        if (tvRestaurantName != null) {
            tvRestaurantName.setOnClickListener(v ->
                    Navigation.findNavController(v).navigate(R.id.action_restaurantDetail_to_info)
            );
        }

        View promoSection = view.findViewById(R.id.promoSection);
        if (promoSection != null) {
            promoSection.setOnClickListener(v ->
                    Navigation.findNavController(v).navigate(R.id.action_restaurantDetail_to_promotions)
            );
        }

        View layoutRatingReview = view.findViewById(R.id.layoutRatingReview);
        if (layoutRatingReview != null) {
            layoutRatingReview.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                if (getArguments() != null) {
                    bundle.putLong("restaurant_id", getArguments().getLong("restaurant_id", -1L));
                }
                Navigation.findNavController(v).navigate(R.id.action_restaurantDetail_to_reviews, bundle);
            });
        }
    }

    private void setupFoodGrid(View view) {
        RecyclerView rvFoods = view.findViewById(R.id.rvFoods);
        rvFoods.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        adapter = new StorefrontAdapter(requireContext());
        rvFoods.setAdapter(adapter);

        adapter.setOnItemClickListener(new StorefrontAdapter.OnItemClickListener() {
            @Override
            public void onFoodClick(FoodItem item) {
                openFoodDetail(view, item);
            }

            @Override
            public void onAddToCartClick(FoodItem item) {
                openFoodDetail(view, item);
            }
        });
    }

    private void openFoodDetail(View view, FoodItem item) {
        Bundle bundle = new Bundle();
        bundle.putLong("food_id", item.getId());
        Navigation.findNavController(view).navigate(R.id.action_restaurantDetail_to_foodDetail, bundle);
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
                    DraftCartV3Response draft = RpcCartUiState.selectDraftForRestaurant(
                            response.body(),
                            restaurantId
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
                clearActiveCartState();
                updateStickyCart(view);
            }

            @Override
            public void onFailure(@NonNull Call<List<DraftCartV3Response>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                if (fallbackCartId > 0) {
                    refreshStickyFromSummary(view, preferredRestaurantId, fallbackCartId, showAddedToast);
                } else if (showAddedToast) {
                    Toast.makeText(requireContext(), "Da them mon, nhung chua tai duoc gio", Toast.LENGTH_SHORT).show();
                } else {
                    clearActiveCartState();
                    updateStickyCart(view);
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

    private void clearActiveCartState() {
        activeCartId = -1L;
        activeCartRestaurantId = -1L;
        activeCartItemCount = 0;
        activeCartTotal = 0;
    }

    private void observeViewModel() {
        viewModel.getFoods().observe(getViewLifecycleOwner(), items -> adapter.submitList(items));
        viewModel.getErrorMsg().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.trim().isEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void updateStickyCart(View view) {
        if (view == null) return;

        View stickyCart = view.findViewById(R.id.layoutStickyCart);
        if (stickyCart == null) return;

        long stickyRestaurantId = activeCartRestaurantId > 0
                ? activeCartRestaurantId
                : (restaurantId > 0 ? restaurantId : LocalCart.getInstance().getRestaurantId());
        // TODO(cart-rpc): Remove this legacy local fallback after all restaurant detail callers pass a Supabase cart_id.
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

        if (tvCount != null) {
            tvCount.setText(String.valueOf(count));
        }
        if (tvTotal != null) {
            double total = activeCartId > 0
                    ? activeCartTotal
                    : LocalCart.getInstance().getTotalPrice(stickyRestaurantId);
            tvTotal.setText(MoneyFormatter.format(total));
        }

        stickyCart.setOnClickListener(v -> {
            if (activeCartId <= 0) {
                LocalCart.getInstance().setActiveRestaurantId(stickyRestaurantId);
            }
            CartBottomSheet sheet = new CartBottomSheet(() ->
                    refreshDraftCartState(view, stickyRestaurantId, activeCartId, false));
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
