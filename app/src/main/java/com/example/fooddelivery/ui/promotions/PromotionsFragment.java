package com.example.fooddelivery.ui.promotions;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fooddelivery.R;
import com.example.fooddelivery.ui.promotions.adapters.PromotionAdapter;
import com.example.fooddelivery.ui.promotions.model.PromotionItem;
import java.util.ArrayList;
import java.util.List;

/**
 * PromotionsFragment – Màn hình "Khuyến mại"
 * Chỉ hiển thị tab "Ưu đãi" (không có Gói tiết kiệm / Đổi ưu đãi).
 * Điều hướng tới từ RestaurantDetailFragment khi click vùng ưu đãi.
 */
public class PromotionsFragment extends Fragment {

    private PromotionAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_promotions, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Close (X) button
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v ->
                Navigation.findNavController(v).popBackStack());

        // Setup RecyclerView
        RecyclerView rvPromotions = view.findViewById(R.id.rvPromotions);
        rvPromotions.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new PromotionAdapter(requireContext(), getMockPromotions());
        rvPromotions.setAdapter(adapter);

        adapter.setOnDangKyClickListener(promo -> {
            Toast.makeText(requireContext(),
                    "Đăng ký: " + promo.getName(), Toast.LENGTH_SHORT).show();
        });
    }

    /** Dữ liệu mock – thay bằng Supabase khi cần */
    private List<PromotionItem> getMockPromotions() {
        List<PromotionItem> list = new ArrayList<>();
        list.add(new PromotionItem("🍱", "beFood - Ăn no giao rẻ mỗi ngày",
                "Trị giá đến 1.4TR • 30 ngày", "Ưu đãi 15K phí giao hàng, đơn từ 70K",
                "x99", "39.000 đ"));
        list.add(new PromotionItem("💰", "be Siêu Tiết Kiệm",
                "Trị giá đến 2TR • 30 ngày", "Ưu đãi 20% (Tối đa 50K) beBike",
                "x20", "30.000 đ"));
        list.add(new PromotionItem("🛵", "beBike - Thả ga di chuyển",
                "Trị giá đến 2.9TR • 30 ngày", "Ưu đãi 20% lên đến 30K beBike",
                "x99", "35.000 đ"));
        list.add(new PromotionItem("🚗", "beCar - Thả ga di chuyển",
                "Trị giá đến 5.2TR • 30 ngày", "Ưu đãi 15% (Tối đa 60K) beCar",
                "x10", "59.000 đ"));
        return list;
    }
}
