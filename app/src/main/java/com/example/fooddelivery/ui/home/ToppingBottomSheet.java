package com.example.fooddelivery.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.fooddelivery.R;
import com.example.fooddelivery.data.model.FoodItem;
import com.example.fooddelivery.data.model.MenuItemDetailV3Response;
import com.example.fooddelivery.data.repository.OrderRepository;
import com.example.fooddelivery.ui.home.options.MenuOptionSelectionState;
import com.example.fooddelivery.utils.MoneyFormatter;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ToppingBottomSheet extends BottomSheetDialogFragment {

    public static final String TAG = "ToppingBottomSheet";

    private FoodItem foodItem;
    private OnAddToCartListener listener;
    private int quantity = 1;
    private String initialNote;
    private OrderRepository orderRepository;
    private MenuOptionSelectionState optionState;
    private LinearLayout optionsContainer;
    private TextView tvNoOptionsMessage;
    private TextView tvTotal;

    public interface OnAddToCartListener {
        void onAddToCart(FoodItem item, String note, List<Long> optionChoiceIds, ToppingBottomSheet sheet);
    }

    public ToppingBottomSheet(FoodItem foodItem, OnAddToCartListener listener) {
        this.foodItem = foodItem;
        this.listener = listener;
    }

    public ToppingBottomSheet(FoodItem foodItem, int quantity, String initialNote, OnAddToCartListener listener) {
        this.foodItem = foodItem;
        this.quantity = Math.max(1, quantity);
        this.initialNote = initialNote;
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.topping_bottom_sheet, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
        if (dialog == null) return;

        View sheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (sheet == null) return;

        sheet.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
        BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(sheet);
        behavior.setSkipCollapsed(true);
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        behavior.setPeekHeight(getResources().getDisplayMetrics().heightPixels);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvTitle = view.findViewById(R.id.tvToppingTitle);
        tvTotal = view.findViewById(R.id.tvToppingTotal);
        TextView tvClose = view.findViewById(R.id.tvCloseTopping);
        EditText etNote = view.findViewById(R.id.etToppingNote);
        Button btnAdd = view.findViewById(R.id.btnAddToppingCart);
        optionsContainer = view.findViewById(R.id.optionsContainer);
        tvNoOptionsMessage = view.findViewById(R.id.tvNoOptionsMessage);
        orderRepository = new OrderRepository(requireContext());
        optionState = new MenuOptionSelectionState(
                foodItem == null ? 0 : foodItem.getPrice(),
                quantity,
                Collections.emptyList()
        );
        if (initialNote != null && !initialNote.trim().isEmpty()) {
            etNote.setText(initialNote);
        }
        

        if (foodItem != null) {
            tvTitle.setText(foodItem.getName());
            updateTotal();
            loadOptions();
        }

        View footer = view.findViewById(R.id.toppingFooter);
        int originalFooterBottomPadding = footer.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(footer, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                    v.getPaddingLeft(),
                    v.getPaddingTop(),
                    v.getPaddingRight(),
                    originalFooterBottomPadding + systemBars.bottom
            );
            return insets;
        });

        tvClose.setOnClickListener(v -> dismiss());

        btnAdd.setOnClickListener(v -> {
            if (listener != null && foodItem != null) {
                MenuOptionSelectionState.ValidationResult validation = optionState.validate();
                if (!validation.isValid()) {
                    Toast.makeText(requireContext(), validation.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }
                String note = etNote == null ? null : etNote.getText().toString();
                listener.onAddToCart(foodItem, note, optionState.getSelectedOptionChoiceIds(), this);
            }
        });
    }

    private void loadOptions() {
        orderRepository.getMenuItemDetailV3(foodItem.getId()).enqueue(new Callback<MenuItemDetailV3Response>() {
            @Override
            public void onResponse(@NonNull Call<MenuItemDetailV3Response> call,
                                   @NonNull Response<MenuItemDetailV3Response> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    List<MenuItemDetailV3Response.MenuOptionGroup> groups = response.body().getOptionGroups();
                    optionState = new MenuOptionSelectionState(foodItem.getPrice(), quantity, groups);
                    renderOptions(groups);
                    updateTotal();
                    return;
                }
                showOptionsLoadError();
            }

            @Override
            public void onFailure(@NonNull Call<MenuItemDetailV3Response> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                showOptionsLoadError();
            }
        });
    }

    private void showOptionsLoadError() {
        optionState = new MenuOptionSelectionState(
                foodItem == null ? 0 : foodItem.getPrice(),
                quantity,
                Collections.emptyList()
        );
        showNoOptionsMessage();
        updateTotal();
        Toast.makeText(requireContext(), "Khong the tai tuy chon mon", Toast.LENGTH_SHORT).show();
    }

    private void renderOptions(List<MenuItemDetailV3Response.MenuOptionGroup> groups) {
        if (optionsContainer == null || tvNoOptionsMessage == null) {
            return;
        }
        optionsContainer.removeAllViews();
        if (groups == null || groups.isEmpty()) {
            showNoOptionsMessage();
            return;
        }

        tvNoOptionsMessage.setVisibility(View.GONE);
        optionsContainer.setVisibility(View.VISIBLE);
        for (MenuItemDetailV3Response.MenuOptionGroup group : groups) {
            optionsContainer.addView(createGroupTitle(group));
            if (group.isSingleSelection()) {
                optionsContainer.addView(createRadioGroup(group));
            } else if (group.isMultipleSelection()) {
                addCheckboxes(group);
            }
        }
    }

    private void showNoOptionsMessage() {
        if (optionsContainer != null) {
            optionsContainer.removeAllViews();
            optionsContainer.setVisibility(View.GONE);
        }
        if (tvNoOptionsMessage != null) {
            tvNoOptionsMessage.setVisibility(View.VISIBLE);
        }
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
            updateTotal();
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
                updateTotal();
            });
            optionsContainer.addView(checkBox);
        }
    }

    private String formatChoiceLabel(MenuItemDetailV3Response.MenuOptionChoice choice) {
        return choice.getName() + " +" + MoneyFormatter.format(choice.getPriceDelta());
    }

    private void updateTotal() {
        if (tvTotal != null && optionState != null) {
            tvTotal.setText(MoneyFormatter.format(optionState.getTotalPrice()));
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
