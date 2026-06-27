package com.example.fooddelivery.ui.detail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.fooddelivery.R;

/**
 * RestaurantInfoFragment – Màn hình "Thông tin quán"
 * Hiển thị: tên quán, địa chỉ, biểu đồ phân bổ sao và giờ hoạt động.
 * Điều hướng tới từ RestaurantDetailFragment khi người dùng click tên nhà hàng.
 */
public class RestaurantInfoFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_restaurant_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Back button
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v ->
                Navigation.findNavController(v).popBackStack());

        // TODO: Khi kết nối Supabase, nhận restaurant_id từ arguments và load dữ liệu thực
        // long restaurantId = getArguments() != null ? getArguments().getLong("restaurant_id") : -1;
        // viewModel.loadRestaurantInfo(restaurantId);
    }
}
