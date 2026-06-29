package com.example.fooddelivery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CartUiFlowRegressionTest {

    @Test
    public void toppingSheetDoesNotShowHardcodedWrongFoodOptions() throws Exception {
        String layout = readFile(projectPath("src/main/res/layout/topping_bottom_sheet.xml"));

        assertFalse(layout.contains("khoai tây"));
        assertFalse(layout.contains("Khoai tây"));
        assertFalse(layout.contains("Sốt tương"));
        assertTrue(layout.contains("NestedScrollView"));
        assertTrue(layout.contains("@+id/btnAddToppingCart"));
    }

    @Test
    public void toppingSheetOpensExpandedWithSafeFooter() throws Exception {
        String source = readFile(projectPath("src/main/java/com/example/fooddelivery/ui/home/ToppingBottomSheet.java"));
        String layout = readFile(projectPath("src/main/res/layout/topping_bottom_sheet.xml"));

        assertTrue(source.contains("BottomSheetBehavior.STATE_EXPANDED"));
        assertTrue(source.contains("setSkipCollapsed(true)"));
        assertTrue(source.contains("WindowInsetsCompat.Type.systemBars()"));
        assertTrue(layout.contains("@+id/toppingFooter"));
        assertTrue(layout.contains("android:layout_height=\"0dp\""));
        assertTrue(layout.contains("android:layout_weight=\"1\""));
    }

    @Test
    public void cartSheetClearDismissesAndNotifiesHostInsteadOfShowingCheckoutZero() throws Exception {
        String source = readFile(projectPath("src/main/java/com/example/fooddelivery/ui/cart/CartBottomSheet.java"));

        assertTrue(source.contains("OnCartChangedListener"));
        assertTrue(source.contains("notifyCartChanged()"));
        assertTrue(source.contains("clearRestaurant(restaurantId)"));
        assertTrue(source.contains("dismiss()"));
        assertTrue(source.contains("btnViewOrder.setEnabled(count > 0)"));
        assertFalse(source.contains("0Đ"));
    }

    @Test
    public void moneyFormattingUsesVietnameseDotSeparatorAndLowercaseCurrency() throws Exception {
        Class<?> formatter = Class.forName("com.example.fooddelivery.utils.MoneyFormatter");

        assertEquals("80.000đ", formatter.getMethod("format", long.class).invoke(null, 80_000L));
        assertEquals("0đ", formatter.getMethod("format", long.class).invoke(null, 0L));
    }

    @Test
    public void stickyCartSurfacesUseSharedMoneyFormatter() throws Exception {
        assertSourceContains("src/main/java/com/example/fooddelivery/ui/detail/RestaurantDetailFragment.java",
                "MoneyFormatter.format",
                "new CartBottomSheet(() -> updateStickyCart(view))");
        assertSourceDoesNotContain("src/main/java/com/example/fooddelivery/ui/detail/RestaurantDetailFragment.java",
                "DecimalFormat(\"#,###\")",
                "+ \"d\"");

        assertSourceContains("src/main/java/com/example/fooddelivery/ui/menu/MenuFragment.java",
                "MoneyFormatter.format",
                "new CartBottomSheet(() -> updateStickyCart(view))");
        assertSourceDoesNotContain("src/main/java/com/example/fooddelivery/ui/menu/MenuFragment.java",
                "DecimalFormat(\"#,###\")");
    }

    private void assertSourceContains(String path, String... snippets) throws Exception {
        String source = readFile(projectPath(path));
        for (String snippet : snippets) {
            assertTrue("Missing snippet in " + path + ": " + snippet, source.contains(snippet));
        }
    }

    private void assertSourceDoesNotContain(String path, String... snippets) throws Exception {
        String source = readFile(projectPath(path));
        for (String snippet : snippets) {
            assertFalse("Unexpected snippet in " + path + ": " + snippet, source.contains(snippet));
        }
    }

    private Path projectPath(String path) {
        Path moduleRelative = Paths.get(path);
        if (Files.exists(moduleRelative)) {
            return moduleRelative;
        }
        return Paths.get("app").resolve(path);
    }

    private String readFile(Path path) throws Exception {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
