package com.example.fooddelivery.ui.detail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.fooddelivery.R;
import com.example.fooddelivery.data.model.FoodItem;
import com.example.fooddelivery.data.model.MenuItemDetailV3Response;
import com.example.fooddelivery.data.repository.OrderRepository;
import com.example.fooddelivery.databinding.FoodFragmentDetailBinding;
import com.example.fooddelivery.ui.home.options.MenuOptionSelectionState;
import com.example.fooddelivery.utils.MoneyFormatter;

import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FoodDetailFragment extends Fragment {

    private FoodFragmentDetailBinding binding;
    private FoodDetailViewModel viewModel;
    private OrderRepository orderRepository;
    private long foodId;
    private int quantity = 1;
    private boolean isAdding = false;
    private MenuOptionSelectionState optionState;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FoodFragmentDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(FoodDetailViewModel.class);
        orderRepository = new OrderRepository(requireContext());
        foodId = getArguments() != null ? getArguments().getLong("food_id", -1) : -1;
        optionState = new MenuOptionSelectionState(0, quantity, Collections.emptyList());

        setupListeners();
        observeViewModel();

        if (foodId != -1) {
            viewModel.loadFoodDetail(foodId);
        }
    }

    private void setupListeners() {
        binding.toolbar.setNavigationOnClickListener(v ->
                Navigation.findNavController(requireView()).navigateUp()
        );

        binding.btnPlus.setOnClickListener(v -> {
            quantity++;
            binding.tvQuantity.setText(String.valueOf(quantity));
            optionState.setQuantity(quantity);
            updateTotalPrice();
        });

        binding.btnMinus.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                binding.tvQuantity.setText(String.valueOf(quantity));
                optionState.setQuantity(quantity);
                updateTotalPrice();
            }
        });

        binding.btnAddToCart.setOnClickListener(v -> {
            FoodItem item = viewModel.getFoodItem().getValue();
            if (item != null && !isAdding) {
                MenuOptionSelectionState.ValidationResult validation = optionState.validate();
                if (!validation.isValid()) {
                    Toast.makeText(requireContext(), validation.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }
                String note = binding.edNote.getText().toString();
                String safeNote = note == null || note.trim().isEmpty() ? null : note.trim();
                List<Long> selectedOptionIds = optionState.getSelectedOptionChoiceIds();
                addItemToCart(item, quantity, safeNote, selectedOptionIds);
            }
        });
    }

    private void addItemToCart(FoodItem item,
                               int quantity,
                               String safeNote,
                               List<Long> selectedOptionIds) {
        if (isAdding) return;
        isAdding = true;
        binding.btnAddToCart.setEnabled(false);
        binding.btnAddToCart.setText("Dang them...");

        orderRepository.addToCartV3(item.getId(), quantity, safeNote, selectedOptionIds)
                .enqueue(new Callback<Long>() {
                    @Override
                    public void onResponse(@NonNull Call<Long> call, @NonNull Response<Long> response) {
                        if (!isAdded()) return;
                        isAdding = false;
                        binding.btnAddToCart.setEnabled(true);
                        binding.btnAddToCart.setText("Them vao gio");
                        if (response.isSuccessful()) {
                            Bundle result = new Bundle();
                            result.putLong("cart_id", response.body() == null ? -1L : response.body());
                            result.putLong("restaurant_id", item.getRestaurantId());
                            getParentFragmentManager().setFragmentResult("cart_changed", result);
                            Toast.makeText(requireContext(), "Da them vao gio hang", Toast.LENGTH_SHORT).show();
                            boolean returnedHome = Navigation.findNavController(requireView())
                                    .popBackStack(R.id.homeFragment, false);
                            if (!returnedHome) {
                                Navigation.findNavController(requireView()).navigateUp();
                            }
                            return;
                        }
                        Toast.makeText(requireContext(), "Khong the them mon vao gio", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(@NonNull Call<Long> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        isAdding = false;
                        binding.btnAddToCart.setEnabled(true);
                        binding.btnAddToCart.setText("Them vao gio");
                        Toast.makeText(requireContext(),
                                "Khong the them mon vao gio: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void observeViewModel() {
        viewModel.getFoodItem().observe(getViewLifecycleOwner(), item -> {
            if (item == null) return;
            binding.tvFoodName.setText(item.getName());
            binding.tvDescription.setText(item.getDescription());
            optionState = new MenuOptionSelectionState(item.getPrice(), quantity, Collections.emptyList());
            showNoOptionsMessage();
            updateTotalPrice();
            loadOptions(item);

            Glide.with(requireContext())
                    .load(item.getImageUrl())
                    .placeholder(R.drawable.placeholder_food)
                    .into(binding.imgFood);
        });

        viewModel.getErrorMsg().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadOptions(FoodItem item) {
        orderRepository.getMenuItemDetailV3(item.getId()).enqueue(new Callback<MenuItemDetailV3Response>() {
            @Override
            public void onResponse(@NonNull Call<MenuItemDetailV3Response> call,
                                   @NonNull Response<MenuItemDetailV3Response> response) {
                if (!isAdded() || binding == null) return;
                if (response.isSuccessful() && response.body() != null) {
                    List<MenuItemDetailV3Response.MenuOptionGroup> groups = response.body().getOptionGroups();
                    optionState = new MenuOptionSelectionState(item.getPrice(), quantity, groups);
                    renderOptions(groups);
                    updateTotalPrice();
                    return;
                }
                showOptionsLoadError(item);
            }

            @Override
            public void onFailure(@NonNull Call<MenuItemDetailV3Response> call, @NonNull Throwable t) {
                if (!isAdded() || binding == null) return;
                showOptionsLoadError(item);
            }
        });
    }

    private void showOptionsLoadError(FoodItem item) {
        optionState = new MenuOptionSelectionState(item.getPrice(), quantity, Collections.emptyList());
        showNoOptionsMessage();
        updateTotalPrice();
        Toast.makeText(requireContext(), "Khong the tai tuy chon mon", Toast.LENGTH_SHORT).show();
    }

    private void renderOptions(List<MenuItemDetailV3Response.MenuOptionGroup> groups) {
        binding.foodDetailOptionsContainer.removeAllViews();
        if (groups == null || groups.isEmpty()) {
            showNoOptionsMessage();
            return;
        }

        binding.tvFoodDetailNoOptionsMessage.setVisibility(View.GONE);
        binding.foodDetailOptionsContainer.setVisibility(View.VISIBLE);
        for (MenuItemDetailV3Response.MenuOptionGroup group : groups) {
            binding.foodDetailOptionsContainer.addView(createGroupTitle(group));
            if (group.isSingleSelection()) {
                binding.foodDetailOptionsContainer.addView(createRadioGroup(group));
            } else if (group.isMultipleSelection()) {
                addCheckboxes(group);
            }
        }
    }

    private void showNoOptionsMessage() {
        if (binding == null) return;
        binding.foodDetailOptionsContainer.removeAllViews();
        binding.foodDetailOptionsContainer.setVisibility(View.GONE);
        binding.tvFoodDetailNoOptionsMessage.setVisibility(View.VISIBLE);
    }

    private TextView createGroupTitle(MenuItemDetailV3Response.MenuOptionGroup group) {
        TextView title = new TextView(requireContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, dp(18), 0, dp(6));
        title.setLayoutParams(params);
        title.setText(group.getName() + (group.isRequired() ? " (bat buoc)" : ""));
        title.setTextColor(0xFF111111);
        title.setTextSize(16);
        title.setTypeface(title.getTypeface(), android.graphics.Typeface.BOLD);
        return title;
    }

    private RadioGroup createRadioGroup(MenuItemDetailV3Response.MenuOptionGroup group) {
        RadioGroup radioGroup = new RadioGroup(requireContext());
        radioGroup.setOrientation(RadioGroup.VERTICAL);
        radioGroup.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        for (MenuItemDetailV3Response.MenuOptionChoice choice : group.getChoices()) {
            RadioButton radioButton = new RadioButton(requireContext());
            radioButton.setId(View.generateViewId());
            radioButton.setTag(choice.getOptionChoiceId());
            radioButton.setText(formatChoiceLabel(choice));
            radioButton.setTextSize(14);
            radioButton.setEnabled(choice.isAvailable());
            radioButton.setChecked(optionState.isSelected(group.getOptionGroupId(), choice.getOptionChoiceId()));
            radioGroup.addView(radioButton);
        }

        radioGroup.setOnCheckedChangeListener((buttonGroup, checkedId) -> {
            View checkedView = buttonGroup.findViewById(checkedId);
            if (checkedView == null || checkedView.getTag() == null) {
                return;
            }
            long choiceId = (Long) checkedView.getTag();
            optionState.selectSingle(group.getOptionGroupId(), choiceId);
            updateTotalPrice();
        });
        return radioGroup;
    }

    private void addCheckboxes(MenuItemDetailV3Response.MenuOptionGroup group) {
        for (MenuItemDetailV3Response.MenuOptionChoice choice : group.getChoices()) {
            CheckBox checkBox = new CheckBox(requireContext());
            checkBox.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            checkBox.setText(formatChoiceLabel(choice));
            checkBox.setTextSize(14);
            checkBox.setEnabled(choice.isAvailable());
            checkBox.setChecked(optionState.isSelected(group.getOptionGroupId(), choice.getOptionChoiceId()));
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                boolean changed = optionState.toggleMultiple(group.getOptionGroupId(), choice.getOptionChoiceId());
                if (!changed && isChecked) {
                    buttonView.setChecked(false);
                    Toast.makeText(
                            requireContext(),
                            "Chi duoc chon toi da " + group.getMaxSelect() + " " + group.getName(),
                            Toast.LENGTH_SHORT
                    ).show();
                    return;
                }
                updateTotalPrice();
            });
            binding.foodDetailOptionsContainer.addView(checkBox);
        }
    }

    private String formatChoiceLabel(MenuItemDetailV3Response.MenuOptionChoice choice) {
        return choice.getName() + " +" + MoneyFormatter.format(choice.getPriceDelta());
    }

    private void updateTotalPrice() {
        FoodItem item = viewModel.getFoodItem().getValue();
        if (item == null) return;

        double total = optionState == null ? item.getPrice() * quantity : optionState.getTotalPrice();
        binding.tvTotalPrice.setText(MoneyFormatter.format(total));
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
