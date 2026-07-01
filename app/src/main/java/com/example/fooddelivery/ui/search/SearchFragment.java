package com.example.fooddelivery.ui.search;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.fooddelivery.R;
import com.example.fooddelivery.data.model.FoodItem;
import com.example.fooddelivery.databinding.SearchFragmentBinding;
import com.example.fooddelivery.ui.home.adapters.FoodVerticalAdapter;
import com.example.fooddelivery.ui.menu.MenuViewModel;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private SearchFragmentBinding binding;
    private MenuViewModel viewModel;
    private FoodVerticalAdapter adapter;
    private final List<FoodItem> allMenuItems = new ArrayList<>();
    private String currentQuery = "";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = SearchFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(MenuViewModel.class);

        setupRecyclerView();
        setupSearch();
        observeViewModel();
        focusSearchAndShowKeyboard();

        viewModel.loadFoods("");
    }

    private void setupRecyclerView() {
        adapter = new FoodVerticalAdapter(
                requireContext(),
                item -> {
                    Bundle args = new Bundle();
                    args.putLong("restaurant_id", item.getRestaurantId());
                    Navigation.findNavController(requireView())
                            .navigate(R.id.action_search_to_restaurantDetail, args);
                },
                null,
                false
        );
        binding.rvSearchResults.setAdapter(adapter);
        binding.rvSearchResults.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private void setupSearch() {
        binding.btnBack.setOnClickListener(v ->
                Navigation.findNavController(requireView()).navigateUp()
        );

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentQuery = s == null ? "" : s.toString();
                renderSearchResults();
            }
        });
    }

    private void observeViewModel() {
        viewModel.isLoading().observe(getViewLifecycleOwner(), loading ->
                binding.progressBar.setVisibility(Boolean.TRUE.equals(loading) ? View.VISIBLE : View.GONE)
        );

        viewModel.getFoodItems().observe(getViewLifecycleOwner(), list -> {
            allMenuItems.clear();
            if (list != null) {
                allMenuItems.addAll(list);
            }
            renderSearchResults();
        });
    }

    private void focusSearchAndShowKeyboard() {
        binding.etSearch.post(() -> {
            if (binding == null) return;
            binding.etSearch.requestFocus();
            InputMethodManager imm = (InputMethodManager) requireContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(binding.etSearch, InputMethodManager.SHOW_IMPLICIT);
            }
        });
    }

    private void renderSearchResults() {
        if (binding == null || adapter == null) return;

        List<FoodItem> results = MenuItemSearchFilter.filterByName(allMenuItems, currentQuery);
        adapter.submitList(results);

        String trimmed = currentQuery == null ? "" : currentQuery.trim();
        if (trimmed.isEmpty()) {
            binding.tvEmpty.setVisibility(View.GONE);
        } else if (results.isEmpty()) {
            binding.tvEmpty.setText("khong co ket qua");
            binding.tvEmpty.setVisibility(View.VISIBLE);
        } else {
            binding.tvEmpty.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
