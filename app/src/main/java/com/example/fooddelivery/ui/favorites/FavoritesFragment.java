package com.example.fooddelivery.ui.favorites;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.fooddelivery.R;
import com.example.fooddelivery.databinding.FavoritesFragmentBinding;
import com.example.fooddelivery.ui.favorites.adapters.FavoriteCollectionAdapter;
import com.example.fooddelivery.ui.favorites.data.FavoriteCollectionStore;
import com.example.fooddelivery.ui.favorites.model.FavoriteCollection;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class FavoritesFragment extends Fragment {
    private FavoritesFragmentBinding binding;
    private FavoriteCollectionAdapter adapter;
    private FavoriteCollectionStore store;
    private FavoriteCollectionDraftViewModel draft;

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        binding = FavoritesFragmentBinding.inflate(inflater, container, false);
        store = new FavoriteCollectionStore(requireContext());
        draft = new ViewModelProvider(requireActivity()).get(FavoriteCollectionDraftViewModel.class);
        adapter = new FavoriteCollectionAdapter(new FavoriteCollectionAdapter.Listener() {
            @Override public void onCollectionClick(String id) {
                if (store.findById(id) == null) return;
                draft.startEditing(store.findById(id));
                Bundle args = new Bundle(); args.putString("collection_id", id);
                NavHostFragment.findNavController(FavoritesFragment.this).navigate(R.id.action_favorites_to_collectionRestaurants, args);
            }
            @Override public void onRenameClick(String id) { showRenameDialog(id); }
            @Override public void onDeleteClick(String id) { showDeleteDialog(id); }
            @Override public void onAddClick() {
                draft.startNew();
                NavHostFragment.findNavController(FavoritesFragment.this).navigate(R.id.action_favorites_to_collectionName);
            }
        });
        binding.collectionsList.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.collectionsList.setAdapter(adapter);
        return binding.getRoot();
    }

    @Override public void onResume() { super.onResume(); if (adapter != null) adapter.submitList(store.getAll()); }

    private void showRenameDialog(String id) {
        FavoriteCollection collection = store.findById(id);
        if (collection == null) return;
        EditText input = new EditText(requireContext());
        input.setText(collection.getName()); input.setSelection(input.length());
        int padding = (int) (24 * getResources().getDisplayMetrics().density);
        input.setPadding(padding, padding / 2, padding, padding / 2);
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Đổi tên bộ sưu tập")
                .setView(input)
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    if (store.rename(id, input.getText().toString())) adapter.submitList(store.getAll());
                }).show();
    }

    private void showDeleteDialog(String id) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Xóa bộ sưu tập?")
                .setMessage("Các quán và lịch sử đặt món vẫn được giữ nguyên.")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (dialog, which) -> {
                    store.delete(id); adapter.submitList(store.getAll());
                }).show();
    }
    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }
}
