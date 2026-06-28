package com.example.fooddelivery.ui.favorites;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.fooddelivery.R;
import com.example.fooddelivery.databinding.FragmentCollectionNameBinding;
import com.google.android.material.chip.Chip;

public class CollectionNameFragment extends Fragment {
    private FragmentCollectionNameBinding binding;
    private FavoriteCollectionDraftViewModel draft;

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        binding = FragmentCollectionNameBinding.inflate(inflater, container, false);
        draft = new ViewModelProvider(requireActivity()).get(FavoriteCollectionDraftViewModel.class);
        binding.nameInput.setText(draft.getName());
        binding.nameInput.setSelection(binding.nameInput.length());
        binding.nameInput.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) { draft.setName(s.toString()); render(); }
            public void afterTextChanged(Editable s) {}
        });
        for (int i = 0; i < binding.suggestions.getChildCount(); i++) {
            View child = binding.suggestions.getChildAt(i);
            if (child instanceof Chip) child.setOnClickListener(v -> binding.nameInput.setText(((Chip) v).getText()));
        }
        binding.clearButton.setOnClickListener(v -> binding.nameInput.setText(""));
        binding.backButton.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        binding.continueButton.setOnClickListener(v -> {
            if (draft.isNameValid()) NavHostFragment.findNavController(this).navigate(R.id.action_collectionName_to_collectionRestaurants);
        });
        render();
        return binding.getRoot();
    }
    private void render() { boolean valid = draft.isNameValid(); binding.clearButton.setVisibility(valid ? View.VISIBLE : View.GONE); binding.continueButton.setEnabled(valid); binding.continueButton.setTextColor(valid ? 0xFFFFFFFF : 0xFFCFD2D4); }
    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }
}
