package com.example.fooddelivery;

public class FoodCategory {
    private String id;
    private String name;
    private String slug;
    private String iconUrl;

    public FoodCategory() {}

    public FoodCategory(String id, String name, String slug, String iconUrl) {
        this.id      = id;
        this.name    = name;
        this.slug    = slug;
        this.iconUrl = iconUrl;
    }

    public String getId()      { return id; }
    public String getName()    { return name; }
    public String getSlug()    { return slug; }
    public String getIconUrl() { return iconUrl; }

    public void setId(String id)           { this.id = id; }
    public void setName(String name)       { this.name = name; }
    public void setSlug(String slug)       { this.slug = slug; }
    public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }
}
