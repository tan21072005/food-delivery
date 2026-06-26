package com.example.fooddelivery.ui.profile;

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
import com.example.fooddelivery.data.model.AddressItem;

import java.util.ArrayList;
import java.util.List;

public class AddressListFragment extends Fragment {

    private AddressAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_address_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            Navigation.findNavController(v).navigateUp();
        });

        RecyclerView rvAddresses = view.findViewById(R.id.rvAddresses);
        rvAddresses.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new AddressAdapter(requireContext());
        rvAddresses.setAdapter(adapter);

        // Dummy data
        List<AddressItem> dummyList = new ArrayList<>();
        dummyList.add(new AddressItem("1", "Nhà", "Phòng 605, Tòa nhà HH1A, Linh Đàm, Hoàng Mai, Hà Nội", "Nguyễn Văn A - 0901234567", true));
        dummyList.add(new AddressItem("2", "Công ty", "Tầng 3, Tòa nhà ABC, 123 Đường XYZ, Quận 1, TP.HCM", "Nguyễn Văn A - 0901234567", false));
        adapter.submitList(dummyList);

        adapter.setListener(item -> {
            Toast.makeText(requireContext(), "Đã chọn: " + item.getLabel(), Toast.LENGTH_SHORT).show();
            // TODO: Select address and go back
        });

        View btnAddAddress = view.findViewById(R.id.btnAddAddress);
        btnAddAddress.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Tính năng thêm địa chỉ đang được cập nhật", Toast.LENGTH_SHORT).show();
        });
        
        View llCurrentLocation = view.findViewById(R.id.llCurrentLocation);
        llCurrentLocation.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Đang lấy vị trí hiện tại...", Toast.LENGTH_SHORT).show();
        });
    }
}
