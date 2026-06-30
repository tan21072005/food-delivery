package com.example.fooddelivery.ui.splash;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.example.fooddelivery.MainActivity;
import com.example.fooddelivery.data.local.prefs.SessionManager;
import com.example.fooddelivery.ui.auth.AuthActivity;

public final class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        SessionManager sessionManager = new SessionManager(getApplicationContext());
        LaunchDestination destination = new SessionDestinationResolver().resolve(
                sessionManager.getToken(),
                System.currentTimeMillis() / 1_000L
        );
        Class<?> target = destination == LaunchDestination.MAIN
                ? MainActivity.class
                : AuthActivity.class;

        startActivity(new Intent(this, target));
        finish();
    }
}
