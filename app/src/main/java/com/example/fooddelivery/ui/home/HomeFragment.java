package com.example.fooddelivery.ui.home;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager2.widget.ViewPager2;

import com.example.fooddelivery.R;
import com.example.fooddelivery.data.model.DeliveryAddress;
import com.example.fooddelivery.data.model.FoodItem;
import com.example.fooddelivery.data.repository.DeliveryAddressRepository;
import com.example.fooddelivery.databinding.HomeFragmentBinding;
import com.example.fooddelivery.ui.home.adapters.BannerAdapter;
import com.example.fooddelivery.ui.home.adapters.CategoryAdapter;
import com.example.fooddelivery.ui.home.adapters.FoodVerticalAdapter;
import com.example.fooddelivery.ui.home.adapters.TopSellingAdapter;

import java.util.Arrays;
import java.util.List;

public class HomeFragment extends Fragment {

    private static final int SLIDE_DELAY_MS = 3000;

    private HomeFragmentBinding binding;
    private HomeViewModel viewModel;
    private DeliveryAddressRepository deliveryAddressRepository;

    private BannerAdapter bannerAdapter;
    private CategoryAdapter categoryAdapter;
    private TopSellingAdapter topSellingAdapter;
    private FoodVerticalAdapter foodVerticalAdapter;

    private final Handler bannerHandler = new Handler(Looper.getMainLooper());
    private Runnable bannerRunnable;
    private ImageView[] dots;
    private int currentBannerPage = 0;

    private final List<String> bannerUrls = Arrays.asList(
            "https://res.cloudinary.com/daakugdmw/image/upload/v1779354970/banner2.png",
            "https://res.cloudinary.com/daakugdmw/image/upload/v1779354937/banner1.png",
            "https://res.cloudinary.com/daakugdmw/image/upload/v1778937385/banner_food.jpg"
    );

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = HomeFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        deliveryAddressRepository = new DeliveryAddressRepository(requireContext());
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        setupBannerSlider();
        setupCategories();
        setupTopSelling();
        setupAllFoods();
        setupDiscoveryActions();
        setupDeliveryAddressEntry();
        observeViewModel();

        viewModel.loadHome();
    }

    private void setupDeliveryAddressEntry() {
        binding.layoutAddress.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("source", "home");
            Navigation.findNavController(requireView()).navigate(R.id.action_home_to_addressList, args);
        });
        updateDeliveryAddressPill();
    }

    private void updateDeliveryAddressPill() {
        if (binding == null || deliveryAddressRepository == null) return;
        deliveryAddressRepository.getCurrentAddress(new DeliveryAddressRepository.ResultCallback<DeliveryAddress>() {
            @Override
            public void onSuccess(DeliveryAddress current) {
                if (binding != null) {
                    binding.tvAddress.setText(current == null ? "Them dia chi giao hang" : current.getFullAddress());
                }
            }

            @Override
            public void onError(String message) {
                if (binding != null) {
                    binding.tvAddress.setText("Them dia chi giao hang");
                }
            }
        });
    }

    private void setupBannerSlider() {
        bannerAdapter = new BannerAdapter(requireContext(), bannerUrls);
        binding.viewPagerBanner.setAdapter(bannerAdapter);

        int startPos = bannerAdapter.getStartPosition();
        binding.viewPagerBanner.setCurrentItem(startPos, false);
        currentBannerPage = startPos;

        setupDots(bannerUrls.size());
        updateDots(0);

        binding.viewPagerBanner.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                currentBannerPage = position;
                updateDots(position % bannerUrls.size());
            }
        });

        bannerRunnable = () -> {
            if (binding == null) return;
            currentBannerPage++;
            binding.viewPagerBanner.setCurrentItem(currentBannerPage, true);
            bannerHandler.postDelayed(bannerRunnable, SLIDE_DELAY_MS);
        };
        bannerHandler.postDelayed(bannerRunnable, SLIDE_DELAY_MS);
    }

    private void setupDots(int count) {
        dots = new ImageView[count];
        binding.layoutDots.removeAllViews();

        for (int i = 0; i < count; i++) {
            dots[i] = new ImageView(requireContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(6, 0, 6, 0);
            dots[i].setLayoutParams(params);
            dots[i].setImageResource(R.drawable.dot_inactive);
            binding.layoutDots.addView(dots[i]);
        }
    }

    private void updateDots(int activeIndex) {
        if (dots == null) return;
        for (int i = 0; i < dots.length; i++) {
            dots[i].setImageResource(i == activeIndex ? R.drawable.dot_active : R.drawable.dot_inactive);
        }
    }

    private void setupCategories() {
        categoryAdapter = new CategoryAdapter(requireContext(), category -> {
            Bundle args = new Bundle();
            args.putString("category_slug", category.getSlug());
            args.putString("category_name", category.getName());
            Navigation.findNavController(requireView()).navigate(R.id.action_home_to_menu, args);
        });

        binding.rvCategories.setAdapter(categoryAdapter);
        binding.rvCategories.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        );
    }

    private void setupTopSelling() {
        topSellingAdapter = new TopSellingAdapter(
                requireContext(),
                this::navigateToRestaurantDetail,
                null,
                false // showAddButton: Home is discovery only.
        );

        binding.rvTopSelling.setAdapter(topSellingAdapter);
        binding.rvTopSelling.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        );
    }

    private void setupAllFoods() {
        foodVerticalAdapter = new FoodVerticalAdapter(
                requireContext(),
                this::navigateToRestaurantDetail,
                null,
                false // showAddButton: add-to-cart starts from Restaurant/Menu.
        );

        binding.rvAllFoods.setAdapter(foodVerticalAdapter);
        binding.rvAllFoods.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvAllFoods.setNestedScrollingEnabled(false);
    }

    private void setupDiscoveryActions() {
        binding.etSearch.setOnClickListener(v ->
                Navigation.findNavController(requireView()).navigate(R.id.action_home_to_search)
        );

        binding.tvSeeAll.setOnClickListener(v ->
                Navigation.findNavController(requireView()).navigate(R.id.action_home_to_menu)
        );

        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.loadHome());

        View menuRow = binding.getRoot().findViewById(R.id.layoutMenuRow);
        if (menuRow != null) {
            menuRow.setOnClickListener(v -> {
                MenuBottomSheet sheet = new MenuBottomSheet();
                sheet.setOnCategorySelectListener((slug, name) -> {
                    Bundle args = new Bundle();
                    args.putString("category_slug", slug);
                    args.putString("category_name", name);
                    Navigation.findNavController(requireView()).navigate(R.id.action_home_to_menu, args);
                });
                sheet.show(getParentFragmentManager(), MenuBottomSheet.TAG);
            });
        }
    }

    private void observeViewModel() {
        viewModel.isLoading().observe(getViewLifecycleOwner(), loading ->
                binding.swipeRefresh.setRefreshing(Boolean.TRUE.equals(loading))
        );

        viewModel.getCategories().observe(getViewLifecycleOwner(), list ->
                categoryAdapter.submitList(list)
        );

        viewModel.getTopSelling().observe(getViewLifecycleOwner(), list ->
                topSellingAdapter.submitList(list)
        );

        viewModel.getAllFoods().observe(getViewLifecycleOwner(), list ->
                foodVerticalAdapter.submitList(list)
        );

        viewModel.getErrorMsg().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToRestaurantDetail(FoodItem item) {
        Bundle args = new Bundle();
        args.putLong("restaurant_id", item.getRestaurantId());
        Navigation.findNavController(requireView()).navigate(R.id.action_home_to_restaurantDetail, args);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (bannerRunnable != null) {
            bannerHandler.removeCallbacks(bannerRunnable);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (bannerRunnable != null) {
            bannerHandler.postDelayed(bannerRunnable, SLIDE_DELAY_MS);
        }
        updateDeliveryAddressPill();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (bannerRunnable != null) {
            bannerHandler.removeCallbacks(bannerRunnable);
        }
        binding = null;
    }
}
