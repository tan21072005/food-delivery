package com.example.fooddelivery.ui.home.options;

import com.example.fooddelivery.data.model.MenuItemDetailV3Response;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MenuOptionSelectionState {
    private final double basePrice;
    private int quantity;
    private final List<MenuItemDetailV3Response.MenuOptionGroup> groups;
    private final Map<Long, MenuItemDetailV3Response.MenuOptionGroup> groupsById = new HashMap<>();
    private final Map<Long, MenuItemDetailV3Response.MenuOptionChoice> choicesById = new HashMap<>();
    private final Map<Long, Long> groupIdByChoiceId = new HashMap<>();
    private final Map<Long, LinkedHashSet<Long>> selectedChoiceIdsByGroupId = new HashMap<>();

    public MenuOptionSelectionState(
            double basePrice,
            int quantity,
            List<MenuItemDetailV3Response.MenuOptionGroup> groups
    ) {
        this.basePrice = basePrice;
        this.quantity = Math.max(1, quantity);
        this.groups = groups == null ? Collections.emptyList() : groups;
        indexGroups();
        selectDefaults();
    }

    public boolean selectSingle(long groupId, long choiceId) {
        MenuItemDetailV3Response.MenuOptionGroup group = groupsById.get(groupId);
        if (group == null || !group.isSingleSelection() || !isChoiceInGroup(groupId, choiceId)) {
            return false;
        }

        LinkedHashSet<Long> selected = selectedChoiceIdsByGroupId.get(groupId);
        selected.clear();
        selected.add(choiceId);
        return true;
    }

    public boolean toggleMultiple(long groupId, long choiceId) {
        MenuItemDetailV3Response.MenuOptionGroup group = groupsById.get(groupId);
        if (group == null || !group.isMultipleSelection() || !isChoiceInGroup(groupId, choiceId)) {
            return false;
        }

        LinkedHashSet<Long> selected = selectedChoiceIdsByGroupId.get(groupId);
        if (selected.contains(choiceId)) {
            selected.remove(choiceId);
            return true;
        }

        int maxSelect = group.getMaxSelect();
        if (maxSelect > 0 && selected.size() >= maxSelect) {
            return false;
        }

        selected.add(choiceId);
        return true;
    }

    public boolean isSelected(long groupId, long choiceId) {
        Set<Long> selected = selectedChoiceIdsByGroupId.get(groupId);
        return selected != null && selected.contains(choiceId);
    }

    public ValidationResult validate() {
        for (MenuItemDetailV3Response.MenuOptionGroup group : groups) {
            int selectedCount = selectedChoiceIdsByGroupId.get(group.getOptionGroupId()).size();
            int minRequired = group.isRequired() ? Math.max(1, group.getMinSelect()) : group.getMinSelect();
            if (minRequired > 0 && selectedCount < minRequired) {
                return ValidationResult.invalid("Vui long chon " + safeGroupName(group));
            }

            int maxSelect = group.getMaxSelect();
            if (maxSelect > 0 && selectedCount > maxSelect) {
                return ValidationResult.invalid("Chi duoc chon toi da " + maxSelect + " " + safeGroupName(group));
            }
        }
        return ValidationResult.valid();
    }

    public double getTotalPrice() {
        double optionTotal = 0;
        for (Long choiceId : getSelectedOptionChoiceIds()) {
            MenuItemDetailV3Response.MenuOptionChoice choice = choicesById.get(choiceId);
            if (choice != null) {
                optionTotal += choice.getPriceDelta();
            }
        }
        return (basePrice + optionTotal) * quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = Math.max(1, quantity);
    }

    public List<Long> getSelectedOptionChoiceIds() {
        List<Long> selectedIds = new ArrayList<>();
        for (MenuItemDetailV3Response.MenuOptionGroup group : groups) {
            Set<Long> selectedInGroup = selectedChoiceIdsByGroupId.get(group.getOptionGroupId());
            if (selectedInGroup == null || selectedInGroup.isEmpty()) {
                continue;
            }
            for (MenuItemDetailV3Response.MenuOptionChoice choice : group.getChoices()) {
                long choiceId = choice.getOptionChoiceId();
                if (selectedInGroup.contains(choiceId)) {
                    selectedIds.add(choiceId);
                }
            }
        }
        return selectedIds;
    }

    private void indexGroups() {
        for (MenuItemDetailV3Response.MenuOptionGroup group : groups) {
            long groupId = group.getOptionGroupId();
            groupsById.put(groupId, group);
            selectedChoiceIdsByGroupId.put(groupId, new LinkedHashSet<>());
            for (MenuItemDetailV3Response.MenuOptionChoice choice : group.getChoices()) {
                if (choice.isAvailable()) {
                    choicesById.put(choice.getOptionChoiceId(), choice);
                    groupIdByChoiceId.put(choice.getOptionChoiceId(), groupId);
                }
            }
        }
    }

    private void selectDefaults() {
        for (MenuItemDetailV3Response.MenuOptionGroup group : groups) {
            if (!group.isSingleSelection()) {
                continue;
            }
            if (!group.isRequired() && group.getMinSelect() <= 0) {
                continue;
            }
            for (MenuItemDetailV3Response.MenuOptionChoice choice : group.getChoices()) {
                if (choice.isAvailable()) {
                    selectSingle(group.getOptionGroupId(), choice.getOptionChoiceId());
                    break;
                }
            }
        }
    }

    private boolean isChoiceInGroup(long groupId, long choiceId) {
        Long actualGroupId = groupIdByChoiceId.get(choiceId);
        return actualGroupId != null && actualGroupId == groupId;
    }

    private String safeGroupName(MenuItemDetailV3Response.MenuOptionGroup group) {
        String name = group.getName();
        if (name == null || name.trim().isEmpty()) {
            return "tuy chon bat buoc";
        }
        return name;
    }

    public static class ValidationResult {
        private final boolean valid;
        private final String message;

        private ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public static ValidationResult valid() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult invalid(String message) {
            return new ValidationResult(false, message);
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }
    }
}
