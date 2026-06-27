package com.example.fooddelivery.ui.reviews;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fooddelivery.R;
import com.example.fooddelivery.ui.reviews.adapters.ReviewAdapter;
import com.example.fooddelivery.ui.reviews.model.ReviewItem;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ReviewsFragment – Màn hình "Đánh giá của quán"
 * Tính năng:
 *  - Hiển thị biểu đồ tổng quan sao
 *  - Chip "Có ảnh" lọc review có ảnh/nội dung
 *  - Chip "⭐ Sao ▼" mở BottomSheet chọn số sao
 *  - Nút "Đặt lại" để xóa bộ lọc
 *  - Trạng thái rỗng khi không có kết quả
 */
public class ReviewsFragment extends Fragment {

    private ReviewAdapter adapter;
    private List<ReviewItem> allReviews;

    // Filter state
    private int selectedStar = -1;   // -1 = no filter
    private boolean photoFilter = false;

    // Views
    private Chip chipPhoto, chipStar;
    private TextView tvReset;
    private LinearLayout emptyState;
    private RecyclerView rvReviews;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reviews, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Toolbar back
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v ->
                Navigation.findNavController(v).popBackStack());

        // View refs
        chipPhoto  = view.findViewById(R.id.chipPhoto);
        chipStar   = view.findViewById(R.id.chipStar);
        tvReset    = view.findViewById(R.id.tvReset);
        emptyState = view.findViewById(R.id.emptyState);
        rvReviews  = view.findViewById(R.id.rvReviews);

        // Setup RecyclerView
        rvReviews.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvReviews.addItemDecoration(
                new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        allReviews = getMockReviews();
        adapter = new ReviewAdapter(requireContext(), new ArrayList<>(allReviews));
        rvReviews.setAdapter(adapter);

        // "Có ảnh" chip
        chipPhoto.setOnClickListener(v -> {
            photoFilter = !photoFilter;
            chipPhoto.setChecked(photoFilter);
            applyFilters();
        });

        // "⭐ Sao ▼" chip → mở bottom sheet
        chipStar.setOnClickListener(v -> showStarFilterBottomSheet());

        // "Đặt lại"
        tvReset.setOnClickListener(v -> resetFilters());

        // "Đặt lại bộ lọc" trong empty state
        view.findViewById(R.id.btnResetFilter).setOnClickListener(v -> resetFilters());
    }

    // ===== Star Filter Bottom Sheet =====
    private void showStarFilterBottomSheet() {
        BottomSheetDialog sheet = new BottomSheetDialog(requireContext());
        View sheetView = LayoutInflater.from(requireContext())
                .inflate(R.layout.bottom_sheet_star_filter, null);
        sheet.setContentView(sheetView);

        // Close button
        sheetView.findViewById(R.id.btnClose).setOnClickListener(v -> sheet.dismiss());

        // Highlight currently selected option
        refreshStarOptionsUI(sheetView);

        // Star options click
        int[] optionIds = {R.id.option5Star, R.id.option4Star, R.id.option3Star,
                           R.id.option2Star, R.id.option1Star};
        int[] checkIds  = {R.id.check5, R.id.check4, R.id.check3, R.id.check2, R.id.check1};
        int[] starVals  = {5, 4, 3, 2, 1};

        for (int i = 0; i < optionIds.length; i++) {
            final int starVal = starVals[i];
            sheetView.findViewById(optionIds[i]).setOnClickListener(v -> {
                selectedStar = (selectedStar == starVal) ? -1 : starVal; // toggle
                sheet.dismiss();
                updateStarChipLabel();
                applyFilters();
            });
        }

        // "Bỏ lọc" button
        sheetView.findViewById(R.id.btnBoLoc).setOnClickListener(v -> {
            selectedStar = -1;
            sheet.dismiss();
            updateStarChipLabel();
            applyFilters();
        });

        sheet.show();
    }

    private void refreshStarOptionsUI(View sheetView) {
        int[] checkIds = {R.id.check5, R.id.check4, R.id.check3, R.id.check2, R.id.check1};
        int[] starVals = {5, 4, 3, 2, 1};
        for (int i = 0; i < checkIds.length; i++) {
            sheetView.findViewById(checkIds[i]).setVisibility(
                    selectedStar == starVals[i] ? View.VISIBLE : View.GONE);
        }
    }

    // ===== Filter logic =====
    private void applyFilters() {
        List<ReviewItem> filtered = allReviews.stream()
                .filter(r -> selectedStar == -1 || r.getStars() == selectedStar)
                .filter(r -> !photoFilter || r.isHasPhoto())
                .collect(Collectors.toList());

        adapter.updateData(filtered);

        boolean empty = filtered.isEmpty();
        rvReviews.setVisibility(empty ? View.GONE : View.VISIBLE);
        emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);

        updateResetVisibility();
    }

    private void resetFilters() {
        selectedStar = -1;
        photoFilter  = false;
        chipPhoto.setChecked(false);
        chipStar.setChecked(false);
        updateStarChipLabel();
        updateResetVisibility();
        adapter.updateData(new ArrayList<>(allReviews));
        rvReviews.setVisibility(View.VISIBLE);
        emptyState.setVisibility(View.GONE);
    }

    private void updateStarChipLabel() {
        boolean active = selectedStar != -1;
        chipStar.setChecked(active);
        chipStar.setText(active ? selectedStar + " ⭐ ▼" : "⭐ Sao ▼");
    }

    private void updateResetVisibility() {
        boolean hasFilter = selectedStar != -1 || photoFilter;
        tvReset.setVisibility(hasFilter ? View.VISIBLE : View.GONE);
    }

    // ===== Mock data =====
    private List<ReviewItem> getMockReviews() {
        List<ReviewItem> list = new ArrayList<>();
        list.add(new ReviewItem("N", 0xFF4CAF50, "Người dùng ẩn danh", 5, "2 ngày trước",
                null, null, false));
        list.add(new ReviewItem("P", 0xFF9C27B0, "phạm tuấn tùng", 5, "6 ngày trước",
                "Quán ăn ngon, giá hợp lý. Sẽ quay lại!", null, true));
        list.add(new ReviewItem("M", 0xFF009688, "Minh Ánh", 5, "10 ngày trước",
                null, new String[]{"Ngon đỉnh", "Giá phải chăng", "Đóng gói đẹp", "Phần ăn no căng"}, true));
        list.add(new ReviewItem("N", 0xFF4CAF50, "Người dùng ẩn danh", 5, "15 ngày trước",
                null, null, false));
        list.add(new ReviewItem("N", 0xFF2196F3, "Nguyễn Đăng Lộc", 5, "15 ngày trước",
                null, null, false));
        list.add(new ReviewItem("T", 0xFFFF5722, "Trần Thị Bích", 4, "20 ngày trước",
                "Đồ ăn khá ngon, giao hàng đúng giờ.", null, true));
        list.add(new ReviewItem("H", 0xFF607D8B, "Hoàng Minh", 3, "25 ngày trước",
                "Bình thường, không có gì đặc biệt.", null, false));
        return list;
    }
}
