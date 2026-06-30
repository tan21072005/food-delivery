package com.example.fooddelivery.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.Collections;
import java.util.List;

public class MenuItemDetailV3Response {
    @SerializedName("id")
    private long id;

    @SerializedName("restaurant_id")
    private long restaurantId;

    @SerializedName("category_id")
    private long categoryId;

    @SerializedName("item_name")
    private String itemName;

    @SerializedName("description")
    private String description;

    @SerializedName("image_url")
    private String imageUrl;

    @SerializedName("price")
    private double price;

    @SerializedName("rating")
    private double rating;

    @SerializedName("status")
    private String status;

    @SerializedName("sold_count")
    private int soldCount;

    @SerializedName("restaurant")
    private Restaurant restaurant;

    @SerializedName("option_groups")
    private List<MenuOptionGroup> optionGroups;

    public long getId() {
        return id;
    }

    public long getRestaurantId() {
        return restaurantId;
    }

    public long getCategoryId() {
        return categoryId;
    }

    public String getItemName() {
        return itemName;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public double getPrice() {
        return price;
    }

    public double getRating() {
        return rating;
    }

    public String getStatus() {
        return status;
    }

    public int getSoldCount() {
        return soldCount;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public List<MenuOptionGroup> getOptionGroups() {
        if (optionGroups == null) {
            return Collections.emptyList();
        }
        return optionGroups;
    }

    public static class Restaurant {
        @SerializedName("id")
        private long id;

        @SerializedName("name")
        private String name;

        @SerializedName("logo_url")
        private String logoUrl;

        @SerializedName("is_open")
        private boolean open;

        public long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getLogoUrl() {
            return logoUrl;
        }

        public boolean isOpen() {
            return open;
        }
    }

    public static class MenuOptionGroup {
        public static final String SELECTION_TYPE_SINGLE = "single";
        public static final String SELECTION_TYPE_MULTIPLE = "multiple";

        @SerializedName("option_group_id")
        private long optionGroupId;

        @SerializedName("name")
        private String name;

        @SerializedName("selection_type")
        private String selectionType;

        @SerializedName("min_select")
        private int minSelect;

        @SerializedName("max_select")
        private int maxSelect;

        @SerializedName("is_required")
        private boolean required;

        @SerializedName("choices")
        private List<MenuOptionChoice> choices;

        public long getOptionGroupId() {
            return optionGroupId;
        }

        public String getName() {
            return name;
        }

        public String getSelectionType() {
            return selectionType;
        }

        public int getMinSelect() {
            return minSelect;
        }

        public int getMaxSelect() {
            return maxSelect;
        }

        public boolean isRequired() {
            return required;
        }

        public List<MenuOptionChoice> getChoices() {
            if (choices == null) {
                return Collections.emptyList();
            }
            return choices;
        }

        public boolean isSingleSelection() {
            return SELECTION_TYPE_SINGLE.equals(selectionType);
        }

        public boolean isMultipleSelection() {
            return SELECTION_TYPE_MULTIPLE.equals(selectionType);
        }
    }

    public static class MenuOptionChoice {
        @SerializedName("option_choice_id")
        private long optionChoiceId;

        @SerializedName("name")
        private String name;

        @SerializedName("price_delta")
        private double priceDelta;

        @SerializedName("is_available")
        private boolean available;

        public long getOptionChoiceId() {
            return optionChoiceId;
        }

        public String getName() {
            return name;
        }

        public double getPriceDelta() {
            return priceDelta;
        }

        public boolean isAvailable() {
            return available;
        }
    }
}
