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
import com.example.fooddelivery.data.model.FoodItem;
import com.example.fooddelivery.ui.cart.CartBottomSheet;
import com.example.fooddelivery.ui.home.ToppingBottomSheet;
import com.example.fooddelivery.ui.menu.adapters.MenuAdapter;
import com.example.fooddelivery.utils.MoneyFormatter;

import java.util.ArrayList;

public class MenuFragment extends Fragment {

    private MenuViewModel viewModel;
    private MenuAdapter adapter;
    private ProgressBar progressBar;

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
        progressBar = view.findViewById(R.id.progressBar);

        setupHeader(view);
        setupList(view);
        observeViewModel();

        String categorySlug = getArguments() != null ? getArguments().getString("category_slug", "") : "";
        viewModel.loadFoods(categorySlug);

        updateStickyCart(view);
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
                Bundle args = new Bundle();
                args.putLong("food_id", item.getId());
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_menu_to_foodDetail, args);
            }

            @Override
            public void onAddToCartClick(FoodItem item) {
                ToppingBottomSheet toppingSheet =
                        new ToppingBottomSheet(item, selectedItem ->
                                addItemToCartWithRestaurantGuard(selectedItem, 1, getView()));
                toppingSheet.show(getParentFragmentManager(), ToppingBottomSheet.TAG);
            }
        });

        rvMenuItems.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvMenuItems.setAdapter(adapter);
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

    private void addItemToCartWithRestaurantGuard(FoodItem item, int quantity, View view) {
        addItemToCart(item, quantity, view);
    }

    private void addItemToCart(FoodItem item, int quantity, View view) {
        LocalCart.getInstance().add(item, quantity);
        updateStickyCart(view);
        Toast.makeText(requireContext(),
                "Đã thêm " + item.getName() + " vào giỏ", Toast.LENGTH_SHORT).show();
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

        if (tvCount != null) tvCount.setText(String.valueOf(count));
        if (tvTotal != null) {
            double total = LocalCart.getInstance().getTotalPrice();
            tvTotal.setText(MoneyFormatter.format(total));
        }

        stickyCart.setOnClickListener(v -> {
            CartBottomSheet sheet = new CartBottomSheet(() -> updateStickyCart(view));
            sheet.show(getParentFragmentManager(), CartBottomSheet.TAG);
        });
    }
}
