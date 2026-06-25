package com.example.fooddelivery.data.model;



public class FoodItem {
    private int id;
    private String name;
    private String description;
    private int soldCount;
    private double price;
    private int imageResId;   // for local drawable
    private String imageUrl;  // for remote image via Glide

//    public FoodItem() {}
//
//    public FoodItem(int id, String name, String description,
//                    int soldCount, double price, int imageResId) {
//        this.id = id;
//        this.name = name;
//        this.description = description;
//        this.soldCount = soldCount;
//        this.price = price;
//        this.imageResId = imageResId;
//    }

    public FoodItem(int id, String name, String description,
                    int soldCount, double price, String imageUrl) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.soldCount = soldCount;
        this.price = price;
        this.imageUrl = imageUrl;
    }

    // --- Getters & Setters ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getSoldCount() { return soldCount; }
    public void setSoldCount(int soldCount) { this.soldCount = soldCount; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getImageResId() { return imageResId; }
    public void setImageResId(int imageResId) { this.imageResId = imageResId; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    /** Returns formatted price string, e.g. "35.000đ" */
    public String getFormattedPrice() {
        long rounded = Math.round(price);
        // Format with dot thousands separator
        String raw = String.valueOf(rounded);
        StringBuilder sb = new StringBuilder();
        int offset = raw.length() % 3;
        for (int i = 0; i < raw.length(); i++) {
            if (i != 0 && (i - offset) % 3 == 0) sb.append('.');
            sb.append(raw.charAt(i));
        }
        return sb + "đ";
    }

    /** Returns sold count label, e.g. "14 đã bán" */
    public String getSoldCountLabel() {
        return soldCount + " đã bán";
    }
}