package com.example.fooddelivery;

public class ProfileMenuItem {

    public enum Type {
        HEADER,
        SECTION,
        MENU_ITEM
    }

    private final Type type;
    private String sectionTitle;
    private int    iconRes;
    private String label;
    private boolean showArrow;
    private boolean showDivider;
    private String  itemId;

    // Constructor HEADER
    public ProfileMenuItem() {
        this.type = Type.HEADER;
    }

    // Constructor SECTION
    public ProfileMenuItem(String sectionTitle) {
        this.type = Type.SECTION;
        this.sectionTitle = sectionTitle;
    }

    // Constructor MENU_ITEM
    public ProfileMenuItem(String itemId, int iconRes, String label,
                           boolean showArrow, boolean showDivider) {
        this.type        = Type.MENU_ITEM;
        this.itemId      = itemId;
        this.iconRes     = iconRes;
        this.label       = label;
        this.showArrow   = showArrow;
        this.showDivider = showDivider;
    }

    public Type    getType()         { return type; }
    public String  getSectionTitle() { return sectionTitle; }
    public int     getIconRes()      { return iconRes; }
    public String  getLabel()        { return label; }
    public boolean isShowArrow()     { return showArrow; }
    public boolean isShowDivider()   { return showDivider; }
    public String  getItemId()       { return itemId; }
}