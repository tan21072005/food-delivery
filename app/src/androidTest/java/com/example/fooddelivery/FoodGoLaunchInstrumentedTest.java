package com.example.fooddelivery;

import static org.junit.Assert.assertEquals;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.fooddelivery.ui.auth.AuthActivity;
import com.example.fooddelivery.ui.cart.Checkout;
import com.example.fooddelivery.ui.menu.MenuActivity;
import com.example.fooddelivery.ui.splash.SplashActivity;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class FoodGoLaunchInstrumentedTest {
    private final Context context = ApplicationProvider.getApplicationContext();

    @Test
    public void allDeclaredActivitiesArePortrait() throws PackageManager.NameNotFoundException {
        assertPortrait(SplashActivity.class);
        assertPortrait(AuthActivity.class);
        assertPortrait(MainActivity.class);
        assertPortrait(MenuActivity.class);
        assertPortrait(Checkout.class);
    }

    @Test
    public void applicationLabelIsFoodGo() throws PackageManager.NameNotFoundException {
        PackageManager packageManager = context.getPackageManager();
        ApplicationInfo app = packageManager.getApplicationInfo(context.getPackageName(), 0);
        assertEquals("FoodGo", packageManager.getApplicationLabel(app).toString());
    }

    private void assertPortrait(Class<?> activityClass)
            throws PackageManager.NameNotFoundException {
        ActivityInfo info = context.getPackageManager().getActivityInfo(
                new ComponentName(context, activityClass),
                0
        );
        assertEquals(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, info.screenOrientation);
    }
}
