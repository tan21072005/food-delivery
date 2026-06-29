package com.example.fooddelivery.ui.profile;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fooddelivery.R;
import com.example.fooddelivery.data.model.DeliveryAddress;
import com.example.fooddelivery.data.repository.DeliveryAddressRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AddressListFragment extends Fragment {

    private AddressAdapter adapter;
    private DeliveryAddressRepository repository;
    private RecyclerView rvAddresses;
    private TextView tvEmptyState;
    private TextView tvCurrentAddress;
    private LinearLayout llEmptyShortcuts;
    private View llCurrentLocation;
    private String source = "profile";
    private final List<DeliveryAddress> allAddresses = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_address_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        repository = new DeliveryAddressRepository(requireContext());
        if (getArguments() != null) {
            source = getArguments().getString("source", "profile");
        }

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        rvAddresses = view.findViewById(R.id.rvAddresses);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        tvCurrentAddress = view.findViewById(R.id.tvCurrentAddress);
        llEmptyShortcuts = view.findViewById(R.id.llEmptyShortcuts);
        llCurrentLocation = view.findViewById(R.id.llCurrentLocation);
        EditText etSearchAddress = view.findViewById(R.id.etSearchAddress);

        rvAddresses.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new AddressAdapter(requireContext());
        adapter.setManagementActionsVisible(!"home".equals(source));
        rvAddresses.setAdapter(adapter);

        adapter.setListener(item -> {
            if ("home".equals(source)) {
                repository.select(item.getId());
                Navigation.findNavController(requireView()).popBackStack(R.id.homeFragment, false);
            } else {
                openForm(item.getId(), null);
            }
        });
        adapter.setEditListener(item -> openForm(item.getId(), null));
        adapter.setDefaultListener(item -> {
            repository.setDefault(item.getId(), new DeliveryAddressRepository.ResultCallback<Void>() {
                @Override
                public void onSuccess(Void value) {
                    Toast.makeText(requireContext(), "Da dat lam dia chi mac dinh", Toast.LENGTH_SHORT).show();
                    refresh();
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                }
            });
        });
        adapter.setDeleteListener(item -> new AlertDialog.Builder(requireContext())
                .setTitle("Xoa dia chi")
                .setMessage("Ban muon xoa dia chi nay?")
                .setNegativeButton("Huy", null)
                .setPositiveButton("Xoa", (dialog, which) -> {
                    repository.delete(item.getId(), new DeliveryAddressRepository.ResultCallback<Void>() {
                        @Override
                        public void onSuccess(Void value) {
                            Toast.makeText(requireContext(), "Da xoa dia chi", Toast.LENGTH_SHORT).show();
                            refresh();
                        }

                        @Override
                        public void onError(String message) {
                            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .show());

        etSearchAddress.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                renderList(s == null ? "" : s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        view.findViewById(R.id.btnAddAddress).setOnClickListener(v -> openForm(null, null));
        view.findViewById(R.id.btnAddHomeAddress).setOnClickListener(v -> openForm(null, "Nha"));
        view.findViewById(R.id.btnAddWorkAddress).setOnClickListener(v -> openForm(null, "Cong ty"));
        view.findViewById(R.id.llCurrentLocation).setOnClickListener(v -> selectCurrentAddress());

        refresh();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (repository != null) refresh();
    }

    private void refresh() {
        repository.list(new DeliveryAddressRepository.ResultCallback<List<DeliveryAddress>>() {
            @Override
            public void onSuccess(List<DeliveryAddress> addresses) {
                allAddresses.clear();
                allAddresses.addAll(addresses);
                repository.getCurrentAddress(new DeliveryAddressRepository.ResultCallback<DeliveryAddress>() {
                    @Override
                    public void onSuccess(DeliveryAddress current) {
                        llCurrentLocation.setVisibility(current == null ? View.GONE : View.VISIBLE);
                        tvCurrentAddress.setText(current == null
                                ? "Chua co dia chi dang chon"
                                : current.getDisplayLabel() + ": " + current.getFullAddress());
                        renderList("");
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                        renderList("");
                    }
                });
            }

            @Override
            public void onError(String message) {
                allAddresses.clear();
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                renderList("");
            }
        });
    }

    private void renderList(String query) {
        String normalized = query == null ? "" : query.toLowerCase(Locale.ROOT).trim();
        List<DeliveryAddress> filtered = new ArrayList<>();
        for (DeliveryAddress address : allAddresses) {
            String haystack = (address.getDisplayLabel() + " " + address.getFullAddress() + " " + address.getRecipientLine())
                    .toLowerCase(Locale.ROOT);
            if (normalized.isEmpty() || haystack.contains(normalized)) filtered.add(address);
        }
        adapter.submitList(filtered);
        boolean noVisibleAddresses = filtered.isEmpty();
        String emptyMessage = normalized.isEmpty()
                ? "Ban chua co dia chi da luu. Them dia chi de giao hang nhanh hon."
                : "Khong tim thay dia chi phu hop. Them dia chi moi de giao hang.";
        tvEmptyState.setText(emptyMessage);
        tvEmptyState.setVisibility(noVisibleAddresses ? View.VISIBLE : View.GONE);
        llEmptyShortcuts.setVisibility(noVisibleAddresses ? View.VISIBLE : View.GONE);
        rvAddresses.setVisibility(noVisibleAddresses ? View.GONE : View.VISIBLE);
    }

    private void selectCurrentAddress() {
        repository.getCurrentAddress(new DeliveryAddressRepository.ResultCallback<DeliveryAddress>() {
            @Override
            public void onSuccess(DeliveryAddress current) {
                if (current == null) return;

                repository.select(current.getId());
                Toast.makeText(requireContext(), "Da chon dia chi hien tai", Toast.LENGTH_SHORT).show();
                if ("home".equals(source)) {
                    Navigation.findNavController(requireView()).popBackStack(R.id.homeFragment, false);
                }
            }

            @Override
            public void onError(String message) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openForm(@Nullable String addressId, @Nullable String type) {
        Bundle args = new Bundle();
        args.putString("source", source);
        if (addressId != null) args.putString("addressId", addressId);
        if (type != null) args.putString("prefillType", type);
        Navigation.findNavController(requireView()).navigate(R.id.action_addressList_to_deliveryAddressForm, args);
    }
}
