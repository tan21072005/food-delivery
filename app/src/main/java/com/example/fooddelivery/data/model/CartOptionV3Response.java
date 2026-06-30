package com.example.fooddelivery.data.model;

import com.google.gson.annotations.SerializedName;

public class CartOptionV3Response {
    @SerializedName("option_choice_id")
    private long optionChoiceId;

    @SerializedName("name")
    private String name;

    @SerializedName("price_delta")
    private double priceDelta;

    public long getOptionChoiceId() {
        return optionChoiceId;
    }

    public void setOptionChoiceId(long optionChoiceId) {
        this.optionChoiceId = optionChoiceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPriceDelta() {
        return priceDelta;
    }

    public void setPriceDelta(double priceDelta) {
        this.priceDelta = priceDelta;
    }
}
