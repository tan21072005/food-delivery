package com.example.fooddelivery.ui.detail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fooddelivery.R;
import com.example.fooddelivery.data.local.LocalCart;
import com.example.fooddelivery.data.model.FoodItem;
import com.example.fooddelivery.ui.cart.CartBottomSheet;
import com.example.fooddelivery.ui.detail.adapters.StorefrontAdapter;
import com.example.fooddelivery.ui.home.ToppingBottomSheet;

public class RestaurantDetailFragment extends Fragment {

    private RestaurantDetailViewModel viewModel;
    private StorefrontAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_restaurant_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(RestaurantDetailViewModel.class);

        setupToolbar(view);
        setupHeaderActions(view);
        setupFoodGrid(view);
        observeViewModel();

        long restaurantId = getArguments() != null
                ? getArguments().getLong("restaurant_id", -1L)
                : -1L;
        viewModel.loadRestaurantFoods(restaurantId);
        updateStickyCart(view);
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
                Bundle bundle = new Bundle();
                bundle.putLong("food_id", item.getId());
                Navigation.findNavController(view).navigate(R.id.action_restaurantDetail_to_foodDetail, bundle);
            }

            @Override
            public void onAddToCartClick(FoodItem item) {
                ToppingBottomSheet toppingSheet = new ToppingBottomSheet(item, selectedItem -> {
                    addItemToCartWithRestaurantGuard(selectedItem, 1, view);
                });
                toppingSheet.show(getParentFragmentManager(), ToppingBottomSheet.TAG);
            }
        });
    }

    private void addItemToCartWithRestaurantGuard(FoodItem item, int quantity, View view) {
        LocalCart cart = LocalCart.getInstance();
        if (!cart.hasDifferentRestaurant(item)) {
            addItemToCart(item, quantity, view);
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setMessage("Ban dang co mon tu quan khac. Xoa gio hien tai de dat mon tu quan nay?")
                .setNegativeButton("Giu gio cu", null)
                .setPositiveButton("Xoa va them mon moi", (dialog, which) -> {
                    cart.clear();
                    addItemToCart(item, quantity, view);
                })
                .show();
    }

    private void addItemToCart(FoodItem item, int quantity, View view) {
        LocalCart.getInstance().add(item, quantity);
        updateStickyCart(view);
        Toast.makeText(requireContext(),
                "Da them " + item.getName() + " vao gio", Toast.LENGTH_SHORT).show();
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

        int count = LocalCart.getInstance().getTotalCount();
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
            double total = LocalCart.getInstance().getTotalPrice();
            java.text.NumberFormat formatter = new java.text.DecimalFormat("#,###");
            tvTotal.setText(formatter.format(total) + "d");
        }

        stickyCart.setOnClickListener(v -> {
            CartBottomSheet sheet = new CartBottomSheet();
            sheet.show(getParentFragmentManager(), CartBottomSheet.TAG);
        });
    }
}
