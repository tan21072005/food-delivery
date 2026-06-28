package com.example.fooddelivery.ui.search;

import android.os.Bundle;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.widget.Toast;

import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.fooddelivery.R;
import com.example.fooddelivery.databinding.SearchFragmentBinding;
import com.example.fooddelivery.data.local.prefs.SessionManager;
import com.example.fooddelivery.ui.home.adapters.FoodVerticalAdapter;
import com.example.fooddelivery.ui.cart.Checkout;
import com.example.fooddelivery.ui.menu.MenuViewModel;

public class SearchFragment extends Fragment {

    private SearchFragmentBinding binding;
    private MenuViewModel viewModel;
    private FoodVerticalAdapter adapter;
    private SessionManager session;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = SearchFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        session   = new SessionManager(requireContext());
        viewModel = new ViewModelProvider(this).get(MenuViewModel.class);

        setupRecyclerView();
        setupSearch();
        observeViewModel();

        // Focus vÃ o Ã´ search ngay khi má»Ÿ
        binding.etSearch.requestFocus();
    }

    private void setupRecyclerView() {
        adapter = new FoodVerticalAdapter(
                requireContext(),
                item -> {
                    Bundle args = new Bundle();
                    args.putLong("food_id", item.getId());
                    Navigation.findNavController(requireView())
                            .navigate(R.id.action_search_to_foodDetail, args);
                },
                item -> {
                    if (!session.isLoggedIn()) {
                        Toast.makeText(requireContext(),
                                "Vui lÃ²ng Ä‘Äƒng nháº­p", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    viewModel.addToCart(session.getUserId(), item.getId(), 1);
                }
        );
        binding.rvSearchResults.setAdapter(adapter);
        binding.rvSearchResults.setLayoutManager(
                new LinearLayoutManager(requireContext()));
    }

    private void setupSearch() {
        // NÃºt back
        binding.btnBack.setOnClickListener(v ->
                Navigation.findNavController(requireView()).navigateUp()
        );

        // Láº¯ng nghe gÃµ phÃ­m
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String keyword = s.toString().trim();
                if (keyword.isEmpty()) {
                    binding.tvEmpty.setVisibility(View.VISIBLE);
                    binding.tvEmpty.setText("Nháº­p tÃªn mÃ³n báº¡n muá»‘n tÃ¬m...");
                    adapter.submitList(null);
                } else {
                    viewModel.loadMenu("", keyword, "sold_count");
                }
            }
        });
    }

    private void observeViewModel() {
        viewModel.isLoading().observe(getViewLifecycleOwner(), loading ->
                binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE)
        );

        viewModel.getFoodItems().observe(getViewLifecycleOwner(), list -> {
            adapter.submitList(list);
            if (list == null || list.isEmpty()) {
                binding.tvEmpty.setVisibility(View.VISIBLE);
                binding.tvEmpty.setText("KhÃ´ng tÃ¬m tháº¥y mÃ³n Äƒn nÃ o");
            } else {
                binding.tvEmpty.setVisibility(View.GONE);
            }
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
