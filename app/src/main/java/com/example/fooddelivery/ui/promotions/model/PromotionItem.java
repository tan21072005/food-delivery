package com.example.fooddelivery.ui.promotions.model;

public class PromotionItem {

    private final String emoji;
    private final String name;
    private final String value;
    private final String description;
    private final String quantity;
    private final String price;

    public PromotionItem(String emoji, String name, String value, String description, String quantity, String price) {
        this.emoji = emoji;
        this.name = name;
        this.value = value;
        this.description = description;
        this.quantity = quantity;
        this.price = price;
    }

    public String getEmoji() {
        return emoji;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public String getQuantity() {
        return quantity;
    }

    public String getPrice() {
        return price;
    }
}
