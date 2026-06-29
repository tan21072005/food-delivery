package com.example.fooddelivery.data.model;

import com.google.gson.annotations.SerializedName;

public class FoodCategory {
    @SerializedName("id")
    private long id;

    // Mapping to both 'name' (menu_categories) and 'cat_name' (categories) could be tricky. 
    // Usually we pick one based on which table is queried. Assuming 'categories' for home page:
    @SerializedName(value = "cat_name", alternate = {"name"})
    private String name;

    private String slug;

    @SerializedName("icon_url")
    private String iconUrl;

    public FoodCategory() {}

    public FoodCategory(long id, String name, String slug, String iconUrl) {
        this.id      = id;
        this.name    = name;
        this.slug    = slug;
        this.iconUrl = iconUrl;
    }

    public long getId()      { return id; }
    public String getName()    { return name; }
    public String getSlug()    { return slug; }
    public String getIconUrl() { return iconUrl; }

    public void setId(long id)             { this.id = id; }
    public void setName(String name)       { this.name = name; }
    public void setSlug(String slug)       { this.slug = slug; }
    public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }
}
