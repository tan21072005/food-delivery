package com.example.fooddelivery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.fooddelivery.data.local.LocalCart;
import com.example.fooddelivery.data.model.FoodItem;
import com.example.fooddelivery.ui.cart.CartBottomSheet;
import com.example.fooddelivery.ui.home.ToppingBottomSheet;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicBoolean;

@RunWith(AndroidJUnit4.class)
public class CartBottomSheetsInstrumentedTest {

    @Test
    public void toppingSheetShowsSelectedFoodWithoutWrongHardcodedOptionsAndVisibleCta() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                FoodItem item = food(101, 1, "Tra sua oolong nuong", 45_000);
                ToppingBottomSheet sheet = new ToppingBottomSheet(item, selected -> {});
                sheet.show(activity.getSupportFragmentManager(), ToppingBottomSheet.TAG);
                activity.getSupportFragmentManager().executePendingTransactions();
            });

            onView(withText("Tra sua oolong nuong")).check(matches(isDisplayed()));
            onView(withId(R.id.btnAddToppingCart)).check(matches(isDisplayed()));
            onView(withText(containsString("Khoai tây"))).check(doesNotExist());
            onView(withText(containsString("Sốt tương"))).check(doesNotExist());
        }
    }

    @Test
    public void clearingCartSheetClearsOnlySelectedRestaurantAndNotifiesHost() {
        LocalCart.getInstance().clear();
        LocalCart.getInstance().add(food(101, 1, "Bun bo gio heo", 62_000), 1);
        LocalCart.getInstance().add(food(201, 2, "Pizza", 90_000), 1);
        LocalCart.getInstance().setActiveRestaurantId(1);

        AtomicBoolean cartChanged = new AtomicBoolean(false);

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                CartBottomSheet sheet = new CartBottomSheet(() -> cartChanged.set(true));
                sheet.show(activity.getSupportFragmentManager(), CartBottomSheet.TAG);
                activity.getSupportFragmentManager().executePendingTransactions();
            });

            onView(withText("Bun bo gio heo")).check(matches(isDisplayed()));
            onView(withText("Pizza")).check(doesNotExist());
            onView(withId(R.id.tvClearAll)).perform(click());
        }

        assertTrue(cartChanged.get());
        assertTrue(LocalCart.getInstance().isEmpty(1));
        assertFalse(LocalCart.getInstance().isEmpty(2));
    }

    private FoodItem food(long id, long restaurantId, String name, double price) {
        FoodItem item = new FoodItem(id, name, "", 0, price, "");
        item.setRestaurantId(restaurantId);
        return item;
    }
}
