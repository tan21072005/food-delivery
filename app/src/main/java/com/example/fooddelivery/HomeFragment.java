package com.example.fooddelivery;

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

// tsao thêm mỗi .databinding là ko có lỗi nhỉ ???
//.databinding → sub-package do Android tự tạo
import com.example.fooddelivery.databinding.FragmentHomeBinding;


import com.example.fooddelivery.FoodItem;
import com.example.fooddelivery.SessionManager;
import com.example.fooddelivery.HomeViewModel;

import java.util.Arrays;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HomeViewModel       viewModel;
    private SessionManager      session;

    // Adapters
    private BannerAdapter        bannerAdapter;
    private CategoryAdapter      categoryAdapter;
    private TopSellingAdapter    topSellingAdapter;
    private FoodVerticalAdapter  foodVerticalAdapter;

    // Banner auto-slide
    private final Handler  bannerHandler  = new Handler(Looper.getMainLooper());
    private final int      SLIDE_DELAY_MS = 3000; // 3 giây
    private Runnable       bannerRunnable;
    private ImageView[]    dots;
    private int            currentBannerPage = 0;

    // 3 ảnh banner (thay bằng URL thật từ API)
    private final List<String> bannerUrls = Arrays.asList(
            "https://res.cloudinary.com/daakugdmw/image/upload/v1779354970/banner2.png",
            "https://res.cloudinary.com/daakugdmw/image/upload/v1779354937/banner1.png",
            "https://res.cloudinary.com/daakugdmw/image/upload/v1778937385/banner_food.jpg"
    );

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        session   = new SessionManager(requireContext());
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        setupBannerSlider();
        setupCategories();
        setupTopSelling();
        setupAllFoods();
        observeViewModel();
//        setupListeners();

        viewModel.loadHome();
    }

    // ─────────────────────────────────────────────────────────
    // Banner Slider + Auto-slide mỗi 3 giây + Dot indicator
    // ─────────────────────────────────────────────────────────
    private void setupBannerSlider() {
        bannerAdapter = new BannerAdapter(requireContext(), bannerUrls);
        binding.viewPagerBanner.setAdapter(bannerAdapter);

        // Bắt đầu từ giữa để scroll được cả 2 hướng
        int startPos = bannerAdapter.getStartPosition();
        binding.viewPagerBanner.setCurrentItem(startPos, false);
        currentBannerPage = startPos;

        // Tạo dot indicators
        setupDots(bannerUrls.size());
        updateDots(0);

        // Lắng nghe thay đổi trang
        binding.viewPagerBanner.registerOnPageChangeCallback(
                new ViewPager2.OnPageChangeCallback() {
                    @Override
                    public void onPageSelected(int position) {
                        currentBannerPage = position;
                        updateDots(position % bannerUrls.size());
                    }
                }
        );

        // Auto-slide mỗi 3 giây
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

    // ─────────────────────────────────────────────────────────
    // Category RecyclerView (ngang, trượt được)
    // ─────────────────────────────────────────────────────────
    private void setupCategories() {
        categoryAdapter = new CategoryAdapter(requireContext(), category -> {
            // Click danh mục → navigate sang màn hình Menu lọc theo category
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

    // ─────────────────────────────────────────────────────────
    // Top Selling (nằm ngang)
    // ─────────────────────────────────────────────────────────
    private void setupTopSelling() {
        topSellingAdapter = new TopSellingAdapter(
                requireContext(),
                item -> navigateToDetail(item),   // click item
                item -> addToCart(item)            // click nút +
        );

        binding.rvTopSelling.setAdapter(topSellingAdapter);
        binding.rvTopSelling.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        );
    }

    // ─────────────────────────────────────────────────────────
    // All Foods (dọc)
    // ─────────────────────────────────────────────────────────
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

    // ─────────────────────────────────────────────────────────
    // Observe LiveData từ ViewModel
    // ─────────────────────────────────────────────────────────
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
    }

    // ─────────────────────────────────────────────────────────
    // Listeners
    // ─────────────────────────────────────────────────────────
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

    // ─────────────────────────────────────────────────────────
    // Navigation helpers
    // ─────────────────────────────────────────────────────────
    private void navigateToDetail(FoodItem item) {
        Bundle args = new Bundle();
        args.putLong("food_id", item.getId());
        Navigation.findNavController(requireView())
                .navigate(R.id.action_home_to_foodDetail, args);
    }

    private void addToCart(FoodItem item) {
        if (!session.isLoggedIn()) {
            Toast.makeText(requireContext(),
                    "Vui lòng đăng nhập để thêm vào giỏ", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_home_to_login);
            return;
        }
        viewModel.addToCart(session.getBearerToken(), item.getId(), 1);
    }

    // ─────────────────────────────────────────────────────────
    // Lifecycle — dừng/tiếp tục auto-slide
    // ─────────────────────────────────────────────────────────
    @Override
    public void onPause() {
        super.onPause();
        bannerHandler.removeCallbacks(bannerRunnable); // dừng khi rời khỏi màn hình
    }

    @Override
    public void onResume() {
        super.onResume();
        bannerHandler.postDelayed(bannerRunnable, SLIDE_DELAY_MS); // tiếp tục khi quay lại
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        bannerHandler.removeCallbacks(bannerRunnable);
        binding = null;
    }
}