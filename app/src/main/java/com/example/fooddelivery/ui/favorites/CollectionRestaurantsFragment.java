package com.example.fooddelivery.ui.favorites;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.example.fooddelivery.R;
import com.example.fooddelivery.databinding.FragmentCollectionRestaurantsBinding;
import com.example.fooddelivery.ui.favorites.adapters.FavoriteRestaurantAdapter;
import com.example.fooddelivery.ui.favorites.data.FavoriteCollectionStore;
import com.example.fooddelivery.ui.favorites.data.FavoriteRestaurantCatalog;
import com.example.fooddelivery.ui.favorites.data.RestaurantSuggestionService;
import com.example.fooddelivery.data.local.SharedPreferencesOrderHistoryRepository;
import com.example.fooddelivery.data.repository.OrderHistoryRepository;
import com.example.fooddelivery.ui.favorites.model.RestaurantSuggestion;
import com.example.fooddelivery.ui.favorites.model.FavoriteCollection;
import java.util.List;

public class CollectionRestaurantsFragment extends Fragment {
    private FragmentCollectionRestaurantsBinding binding;
    private FavoriteCollectionDraftViewModel draft;
    private FavoriteCollectionStore store;
    private FavoriteRestaurantAdapter adapter;

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        binding = FragmentCollectionRestaurantsBinding.inflate(inflater, container, false);
        draft = new ViewModelProvider(requireActivity()).get(FavoriteCollectionDraftViewModel.class);
        store = new FavoriteCollectionStore(requireContext());
        String collectionId = getArguments() == null ? null : getArguments().getString("collection_id");
        if (collectionId != null && !collectionId.equals(draft.getId())) {
            FavoriteCollection collection = store.findById(collectionId);
            if (collection != null) draft.startEditing(collection);
        }
        OrderHistoryRepository historyRepository = new SharedPreferencesOrderHistoryRepository(requireContext());
        List<RestaurantSuggestion> suggestions = new RestaurantSuggestionService()
                .suggest(historyRepository.getCompletedOrders());
        boolean empty = suggestions.isEmpty();
        binding.restaurantsList.setVisibility(empty ? View.GONE : View.VISIBLE);
        binding.emptyHistory.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.suggestionBanner.setVisibility(empty ? View.GONE : View.VISIBLE);
        binding.completeButton.setVisibility(empty ? View.GONE : View.VISIBLE);
        adapter = new FavoriteRestaurantAdapter(suggestions, draft.getSelectedIds(), id -> {
            draft.toggleRestaurant(id); adapter.setSelected(draft.getSelectedIds());
        });
        binding.restaurantsList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.restaurantsList.setAdapter(adapter);
        binding.backButton.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        binding.completeButton.setOnClickListener(v -> {
            if (!draft.isNameValid()) return;
            store.save(draft.toCollection());
            NavHostFragment.findNavController(this).popBackStack(R.id.favoritesFragment, false);
        });
        binding.exploreButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).popBackStack(R.id.favoritesFragment, false);
            BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottomNav);
            bottomNav.setSelectedItemId(R.id.nav_home);
        });
        return binding.getRoot();
    }
    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }
}
