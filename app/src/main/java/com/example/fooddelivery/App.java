package com.example.fooddelivery;


import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.example.fooddelivery.data.local.prefs.LocaleStore;

public class App extends Application {
    @Override
    public void onCreate(){
        super.onCreate();
        if (AppCompatDelegate.getApplicationLocales().isEmpty()) {
            LocaleStore.apply("vi");
        }
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
    }
}
