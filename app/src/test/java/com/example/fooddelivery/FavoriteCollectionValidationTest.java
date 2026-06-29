package com.example.fooddelivery;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.fooddelivery.ui.favorites.FavoriteCollectionDraftViewModel;
import org.junit.Test;

public class FavoriteCollectionValidationTest {
    @Test public void nameMustBeBetweenOneAndSixtyTrimmedCharacters() {
        FavoriteCollectionDraftViewModel viewModel = new FavoriteCollectionDraftViewModel();
        viewModel.setName(" "); assertFalse(viewModel.isNameValid());
        viewModel.setName(repeat("a", 60)); assertTrue(viewModel.isNameValid());
        viewModel.setName(repeat("a", 61)); assertFalse(viewModel.isNameValid());
    }

    private String repeat(String value, int count) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < count; i++) result.append(value);
        return result.toString();
    }
}
