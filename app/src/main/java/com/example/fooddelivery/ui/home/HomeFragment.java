package com.example.fooddelivery.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager2.widget.ViewPager2;

import com.example.fooddelivery.R;

// tsao thÃªm má»—i .databinding lÃ  ko cÃ³ lá»—i nhá»‰ ???
//.databinding â†’ sub-package do Android tá»± táº¡o
import com.example.fooddelivery.databinding.HomeFragmentBinding;


import com.example.fooddelivery.data.model.FoodItem;
import com.example.fooddelivery.data.model.DeliveryAddress;
import com.example.fooddelivery.data.local.prefs.SessionManager;
import com.example.fooddelivery.data.repository.DeliveryAddressRepository;
import com.example.fooddelivery.ui.cart.Checkout;
import com.example.fooddelivery.ui.home.HomeViewModel;
import com.example.fooddelivery.ui.home.adapters.BannerAdapter;
import com.example.fooddelivery.ui.home.adapters.CategoryAdapter;
import com.example.fooddelivery.ui.home.adapters.TopSellingAdapter;
import com.example.fooddelivery.ui.home.adapters.FoodVerticalAdapter;

import java.util.Arrays;
import java.util.List;

public class HomeFragment extends Fragment {

    private HomeFragmentBinding binding;
    private HomeViewModel       viewModel;
    private SessionManager      session;
    private DeliveryAddressRepository deliveryAddressRepository;

    // Adapters
    private BannerAdapter        bannerAdapter;
    private CategoryAdapter      categoryAdapter;
    private TopSellingAdapter    topSellingAdapter;
    private FoodVerticalAdapter  foodVerticalAdapter;

    // Banner auto-slide
    private final Handler  bannerHandler  = new Handler(Looper.getMainLooper());
    private final int      SLIDE_DELAY_MS = 3000; // 3 giÃ¢y
    private Runnable       bannerRunnable;
    private ImageView[]    dots;
    private int            currentBannerPage = 0;

    // 3 áº£nh banner (thay báº±ng URL tháº­t tá»« API)
    private final List<String> bannerUrls = Arrays.asList(
            "https://res.cloudinary.com/daakugdmw/image/upload/v1779354970/banner2.png",
            "https://res.cloudinary.com/daakugdmw/image/upload/v1779354937/banner1.png",
            "https://res.cloudinary.com/daakugdmw/image/upload/v1778937385/banner_food.jpg"
    );

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = HomeFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        session   = new SessionManager(requireContext());
        deliveryAddressRepository = new DeliveryAddressRepository(requireContext());
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        setupBannerSlider();
        setupCategories();
        setupTopSelling();
        setupAllFoods();
        observeViewModel();
//        setupListeners();
        setupDeliveryAddressEntry();

        View menuRow = binding.getRoot().findViewById(R.id.layoutMenuRow);
        if (menuRow != null) {
            menuRow.setOnClickListener(v -> {
                MenuBottomSheet sheet = new MenuBottomSheet();
                sheet.setOnCategorySelectListener((slug, name) -> {
                    Bundle args = new Bundle();
                    args.putString("category_slug", slug);
                    args.putString("category_name", name);
                    Navigation.findNavController(requireView())
                            .navigate(R.id.action_home_to_menu, args);
                });
                sheet.show(getParentFragmentManager(), MenuBottomSheet.TAG);
            });
        }

        viewModel.loadHome();
        
        updateStickyCart();
    }

    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    // Banner Slider + Auto-slide má»—i 3 giÃ¢y + Dot indicator
    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
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
                if (binding != null) binding.tvAddress.setText("Them dia chi giao hang");
            }
        });
    }

    private void setupBannerSlider() {
        bannerAdapter = new BannerAdapter(requireContext(), bannerUrls);
        binding.viewPagerBanner.setAdapter(bannerAdapter);

        // Báº¯t Ä‘áº§u tá»« giá»¯a Ä‘á»ƒ scroll Ä‘Æ°á»£c cáº£ 2 hÆ°á»›ng
        int startPos = bannerAdapter.getStartPosition();
        binding.viewPagerBanner.setCurrentItem(startPos, false);
        currentBannerPage = startPos;

        // Táº¡o dot indicators
        setupDots(bannerUrls.size());
        updateDots(0);

        // Láº¯ng nghe thay Ä‘á»•i trang
        binding.viewPagerBanner.registerOnPageChangeCallback(
                new ViewPager2.OnPageChangeCallback() {
                    @Override
                    public void onPageSelected(int position) {
                        currentBannerPage = position;
                        updateDots(position % bannerUrls.size());
                    }
                }
        );

        // Auto-slide má»—i 3 giÃ¢y
        bannerRunnable = () -> {
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
            dots[i].setImageResource(
                    i == activeIndex ? R.drawable.dot_active : R.drawable.dot_inactive
            );
        }
    }

    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    // Category RecyclerView (ngang, trÆ°á»£t Ä‘Æ°á»£c)
    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    private void setupCategories() {
        categoryAdapter = new CategoryAdapter(requireContext(), category -> {
            // Click danh má»¥c â†’ navigate sang mÃ n hÃ¬nh Menu lá»c theo category
            Bundle args = new Bundle();
            args.putString("category_slug", category.getSlug());
            args.putString("category_name", category.getName());
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_home_to_menu, args);
        });

        binding.rvCategories.setAdapter(categoryAdapter);
        binding.rvCategories.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        );
    }

    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    // Top Selling (náº±m ngang)
    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    private void setupTopSelling() {
        topSellingAdapter = new TopSellingAdapter(
                requireContext(),
                item -> navigateToDetail(item),   // click item
                item -> addToCart(item)            // click nÃºt +
        );

        binding.rvTopSelling.setAdapter(topSellingAdapter);
        binding.rvTopSelling.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        );
    }

    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    // All Foods (dá»c)
    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    private void setupAllFoods() {
        foodVerticalAdapter = new FoodVerticalAdapter(
                requireContext(),
                item -> navigateToDetail(item),
                item -> addToCart(item)
        );

        binding.rvAllFoods.setAdapter(foodVerticalAdapter);
        binding.rvAllFoods.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvAllFoods.setNestedScrollingEnabled(false);
    }

    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    // Observe LiveData tá»« ViewModel
    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    private void observeViewModel() {
        viewModel.isLoading().observe(getViewLifecycleOwner(), loading ->
                binding.swipeRefresh.setRefreshing(loading)
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
            if (msg != null)
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
        });

        viewModel.getCartMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null)
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
        });

        viewModel.getCartAddedEvent().observe(getViewLifecycleOwner(), added -> {
            if (Boolean.TRUE.equals(added)) {
                startActivity(new Intent(requireContext(), Checkout.class));
                viewModel.consumeCartAddedEvent();
            }
        });
    }

    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    // Listeners
    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
//    private void setupListeners() {
//        // Search â†’ navigate sang mÃ n hÃ¬nh tÃ¬m kiáº¿m
//        binding.etSearch.setOnClickListener(v ->
//                Navigation.findNavController(requireView())
//                        .navigate(R.id.action_home_to_search)
//        );
//
//        // Xem táº¥t cáº£ top bÃ¡n cháº¡y
//        binding.tvSeeAll.setOnClickListener(v ->
//                Navigation.findNavController(requireView())
//                        .navigate(R.id.action_home_to_menu)
//        );
//
//        // Pull-to-refresh
//        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.loadHome());
//
//        // Äá»‹a chá»‰ giao â†’ má»Ÿ mÃ n hÃ¬nh chá»n Ä‘á»‹a chá»‰
//        binding.layoutAddress.setOnClickListener(v ->
//                Toast.makeText(requireContext(), "Chá»n Ä‘á»‹a chá»‰ giao hÃ ng", Toast.LENGTH_SHORT).show()
//        );

    // ————————————————————————————————————————————————————————
    // Listeners
    // ————————————————————————————————————————————————————————
//    private void setupListeners() {
//        // Search → navigate sang màn hình tìm kiếm
//        binding.etSearch.setOnClickListener(v ->
//                Navigation.findNavController(requireView())
//                        .navigate(R.id.action_home_to_search)
//        );
//
//        // Xem tất cả top bán chạy
//        binding.tvSeeAll.setOnClickListener(v ->
//                Navigation.findNavController(requireView())
//                        .navigate(R.id.action_home_to_menu)
//        );
//
//        // Pull-to-refresh
//        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.loadHome());
//
//        // Địa chỉ giao → mở màn hình chọn địa chỉ
//        binding.layoutAddress.setOnClickListener(v ->
//                Toast.makeText(requireContext(), "Chọn địa chỉ giao hàng", Toast.LENGTH_SHORT).show()
//        );
//    }

    // ————————————————————————————————————————————————————————
    // Navigation helpers
    // ————————————————————————————————————————————————————————
    private void navigateToDetail(FoodItem item) {
        Bundle args = new Bundle();
        args.putLong("restaurant_id", item.getRestaurantId());
        Navigation.findNavController(requireView())
                .navigate(R.id.action_home_to_restaurantDetail, args);
    }
    // addToCart — dùng LocalCart (không cần Supabase) và hiện ToppingBottomSheet
    private void addToCart(FoodItem item) {
        ToppingBottomSheet toppingSheet = new ToppingBottomSheet(item, selectedItem -> {
            com.example.fooddelivery.data.local.LocalCart.getInstance().add(selectedItem, 1);
            updateStickyCart();
        });
        toppingSheet.show(getParentFragmentManager(), ToppingBottomSheet.TAG);
    }

    public void updateStickyCart() {
        View stickyCart = binding.getRoot().findViewById(R.id.layoutStickyCart);
        if (stickyCart != null) {
            int count = com.example.fooddelivery.data.local.LocalCart.getInstance().getTotalCount();
            if (count > 0) {
                stickyCart.setVisibility(View.VISIBLE);
                TextView tvCount = stickyCart.findViewById(R.id.tvStickyCartCount);
                TextView tvTotal = stickyCart.findViewById(R.id.tvStickyCartTotal);
                
                if (tvCount != null) tvCount.setText(String.valueOf(count));
                if (tvTotal != null) {
                    double total = com.example.fooddelivery.data.local.LocalCart.getInstance().getTotalPrice();
                    java.text.NumberFormat formatter = new java.text.DecimalFormat("#,###");
                    tvTotal.setText(formatter.format(total) + "đ");
                }
                
                stickyCart.setOnClickListener(v -> {
                    com.example.fooddelivery.ui.cart.CartBottomSheet sheet =
                            new com.example.fooddelivery.ui.cart.CartBottomSheet();
                    sheet.show(getParentFragmentManager(),
                            com.example.fooddelivery.ui.cart.CartBottomSheet.TAG);
                });
            } else {
                stickyCart.setVisibility(View.GONE);
            }
        }
    }



    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    // Lifecycle â€” dá»«ng/tiáº¿p tá»¥c auto-slide
    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    @Override
    public void onPause() {
        super.onPause();
        bannerHandler.removeCallbacks(bannerRunnable); // dá»«ng khi rá»i khá»i mÃ n hÃ¬nh
    }

    @Override
    public void onResume() {
        super.onResume();
        bannerHandler.postDelayed(bannerRunnable, SLIDE_DELAY_MS); // tiáº¿p tá»¥c khi quay láº¡i
        updateDeliveryAddressPill();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        bannerHandler.removeCallbacks(bannerRunnable);
        binding = null;
    }
}
