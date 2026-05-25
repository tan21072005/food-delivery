package com.example.app.model;

public class SettingItem {

    private int icon;
    private String title;
    private Class<?> activityClass;

    public SettingItem(int icon, String title, Class<?> activityClass) {
        this.icon = icon;
        this.title = title;
        this.activityClass = activityClass;
    }

    public int getIcon() {
        return icon;
    }

    public String getTitle() {
        return title;
    }

    public Class<?> getActivityClass() {
        return activityClass;
    }
}