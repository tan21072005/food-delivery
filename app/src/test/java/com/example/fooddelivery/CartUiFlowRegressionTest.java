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
                "refreshDraftCartState(view, stickyRestaurantId, activeCartId, false)");
        assertSourceDoesNotContain("src/main/java/com/example/fooddelivery/ui/detail/RestaurantDetailFragment.java",
                "DecimalFormat(\"#,###\")",
                "+ \"d\"");

        assertSourceContains("src/main/java/com/example/fooddelivery/ui/menu/MenuFragment.java",
                "MoneyFormatter.format",
                "refreshDraftCartState(view, stickyRestaurantId, activeCartId, false)");
        assertSourceDoesNotContain("src/main/java/com/example/fooddelivery/ui/menu/MenuFragment.java",
                "DecimalFormat(\"#,###\")");
    }

    @Test
    public void rpcCartSheetMutatesDraftCartItemsThroughServerRpc() throws Exception {
        assertSourceContains("src/main/java/com/example/fooddelivery/data/remote/apis/ApiService.java",
                "update_cart_item_quantity_v3",
                "remove_cart_item_v3",
                "clear_cart_v3");
        assertSourceContains("src/main/java/com/example/fooddelivery/data/repository/OrderRepository.java",
                "updateCartItemQuantityV3(long cartItemId, int quantity)",
                "removeCartItemV3(long cartItemId)",
                "clearCartV3(long cartId)");
        assertSourceContains("src/main/java/com/example/fooddelivery/ui/cart/CartBottomSheet.java",
                "entry.cartItemId",
                "updateRpcCartItemQuantity(entry, entry.quantity + 1)",
                "updateRpcCartItemQuantity(entry, entry.quantity - 1)",
                "removeCartItemV3(entry.cartItemId)",
                "clearCartV3(cartId)",
                "loadCartSummaryV3()");
        assertSourceDoesNotContain("src/main/java/com/example/fooddelivery/ui/cart/CartBottomSheet.java",
                "tvClearAll.setVisibility(isRpcCart() ? View.GONE : View.VISIBLE)");
        assertSourceContains("src/main/java/com/example/fooddelivery/data/local/LocalCart.java",
                "public final long cartItemId",
                "public CartEntry(FoodItem item, int quantity, long cartItemId)");
        assertSourceContains("docs/supabase_v3_food_delivery_schema.sql",
                "create or replace function public.update_cart_item_quantity_v3",
                "create or replace function public.remove_cart_item_v3",
                "create or replace function public.clear_cart_v3",
                "auth.uid()",
                "update public.carts",
                "updated_at = now()");
    }

    @Test
    public void listPlusAndImageOpenFoodDetailInsteadOfToppingSheet() throws Exception {
        assertSourceContains("src/main/java/com/example/fooddelivery/ui/home/HomeFragment.java",
                "navigateToFoodDetail",
                "action_home_to_foodDetail",
                "args.putLong(\"food_id\", item.getId())",
                "layoutStickyCart",
                "getDraftCartsV3()",
                "new CartBottomSheet(() ->",
                "refreshDraftCartState(view, stickyRestaurantId, activeCartId)");
        assertSourceContains("src/main/java/com/example/fooddelivery/ui/home/adapters/TopSellingAdapter.java",
                "h.itemView.setOnClickListener",
                "h.imgFood.setOnClickListener",
                "h.btnAdd.setVisibility(showAddButton ? View.VISIBLE : View.GONE)");
        assertSourceContains("src/main/java/com/example/fooddelivery/ui/home/adapters/FoodVerticalAdapter.java",
                "h.itemView.setOnClickListener",
                "h.imgFood.setOnClickListener",
                "h.btnAdd.setVisibility(showAddButton ? View.VISIBLE : View.GONE)");
        assertSourceContains("src/main/java/com/example/fooddelivery/ui/home/HomeFragment.java",
                "null,",
                "false");

        assertSourceContains("src/main/java/com/example/fooddelivery/ui/menu/MenuFragment.java",
                "openFoodDetail(view, item)",
                "onAddToCartClick(FoodItem item)");
        assertSourceDoesNotContain("src/main/java/com/example/fooddelivery/ui/menu/MenuFragment.java",
                "new ToppingBottomSheet",
                "toppingSheet.show");

        assertSourceContains("src/main/java/com/example/fooddelivery/ui/detail/RestaurantDetailFragment.java",
                "openFoodDetail(view, item)",
                "onAddToCartClick(FoodItem item)");
        assertSourceDoesNotContain("src/main/java/com/example/fooddelivery/ui/detail/RestaurantDetailFragment.java",
                "new ToppingBottomSheet",
                "toppingSheet.show");

        assertSourceContains("src/main/java/com/example/fooddelivery/ui/menu/adapters/MenuAdapter.java",
                "holder.imgFood.setOnClickListener",
                "listener.onFoodClick(item)");
        assertSourceContains("src/main/java/com/example/fooddelivery/ui/detail/adapters/StorefrontAdapter.java",
                "holder.imgFood.setOnClickListener",
                "listener.onFoodClick(item)");
    }

    @Test
    public void foodDetailAddsRpcCartWithQuantityNoteAndEmptyOptionIds() throws Exception {
        assertSourceContains("src/main/res/layout/food_fragment_detail.xml",
                "android:id=\"@+id/edNote\"");
        assertSourceContains("src/main/java/com/example/fooddelivery/ui/detail/FoodDetailFragment.java",
                "String note = binding.edNote.getText().toString()",
                "orderRepository.addToCartV3(item.getId(), quantity, safeNote, Collections.emptyList())",
                "setFragmentResult(\"cart_changed\"");
        assertSourceDoesNotContain("src/main/java/com/example/fooddelivery/ui/detail/FoodDetailFragment.java",
                "addToCartV3(item.getId(), quantity, null");
    }

    @Test
    public void cartSheetOpensExpandedWithFixedFooterAndLoadingGuard() throws Exception {
        assertSourceContains("src/main/java/com/example/fooddelivery/ui/cart/CartBottomSheet.java",
                "BottomSheetBehavior.STATE_EXPANDED",
                "setSkipCollapsed(true)",
                "setPeekHeight",
                "isMutating",
                "setCartActionsEnabled(false)",
                "setCartActionsEnabled(true)");
        assertSourceContains("src/main/res/layout/cart_bottom_sheet.xml",
                "android:layout_height=\"match_parent\"",
                "android:id=\"@+id/cartSheetFooter\"",
                "android:layout_height=\"0dp\"",
                "android:layout_weight=\"1\"");
    }

    @Test
    public void stickyCartCallbacksRefreshRpcStateAfterSheetMutations() throws Exception {
        assertSourceContains("src/main/java/com/example/fooddelivery/ui/menu/MenuFragment.java",
                "new CartBottomSheet(() ->",
                "refreshDraftCartState(view, stickyRestaurantId, activeCartId, false)");
        assertSourceContains("src/main/java/com/example/fooddelivery/ui/detail/RestaurantDetailFragment.java",
                "new CartBottomSheet(() ->",
                "refreshDraftCartState(view, stickyRestaurantId, activeCartId, false)");
        assertSourceContains("src/main/java/com/example/fooddelivery/ui/home/HomeFragment.java",
                "new CartBottomSheet(() ->",
                "refreshDraftCartState(view, stickyRestaurantId, activeCartId)");
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
        Path repoRelative = Paths.get("..").resolve(path).normalize();
        if (Files.exists(repoRelative)) {
            return repoRelative;
        }
        return Paths.get("app").resolve(path);
    }

    private String readFile(Path path) throws Exception {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
