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
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fooddelivery.R;

public class AddressListFragment extends Fragment {

    private AddressAdapter adapter;
    private DeliveryAddressViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_address_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        RecyclerView rvAddresses = view.findViewById(R.id.rvAddresses);
        rvAddresses.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new AddressAdapter(requireContext());
        rvAddresses.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(DeliveryAddressViewModel.class);
        viewModel.getAddresses().observe(getViewLifecycleOwner(), adapter::submitList);
        viewModel.getError().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });

        adapter.setListener(item -> {
            Toast.makeText(requireContext(), "Da chon: " + item.getLabel(), Toast.LENGTH_SHORT).show();
            viewModel.setDefault(item);
        });

        View btnAddAddress = view.findViewById(R.id.btnAddAddress);
        btnAddAddress.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Form them DeliveryAddress se duoc hoan thien tiep", Toast.LENGTH_SHORT).show());

        View llCurrentLocation = view.findViewById(R.id.llCurrentLocation);
        llCurrentLocation.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Dang lay vi tri hien tai...", Toast.LENGTH_SHORT).show());

        viewModel.loadAddresses();
    }
}
