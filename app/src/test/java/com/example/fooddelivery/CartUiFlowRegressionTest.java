package com.example.fooddelivery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.example.fooddelivery.data.local.LocalCart;
import com.example.fooddelivery.data.model.CartSummaryV3Response;
import com.example.fooddelivery.data.model.DraftCartV3Response;
import com.example.fooddelivery.ui.cart.RpcCartUiState;
import com.google.gson.Gson;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class CartUiFlowRegressionTest {

    @Test
    public void selectDraftForRestaurantDoesNotFallbackToOtherRestaurant() {
        DraftCartV3Response other = new DraftCartV3Response();
        other.setCartId(11L);
        other.setRestaurantId(200L);
        other.setItemCount(2);

        DraftCartV3Response result = RpcCartUiState.selectDraftForRestaurant(
                java.util.Collections.singletonList(other),
                100L
        );

        assertNull(result);
    }

    @Test
    public void selectDraftForRestaurantReturnsOnlyMatchingRestaurant() {
        DraftCartV3Response other = new DraftCartV3Response();
        other.setCartId(11L);
        other.setRestaurantId(200L);
        other.setItemCount(2);

        DraftCartV3Response matching = new DraftCartV3Response();
        matching.setCartId(12L);
        matching.setRestaurantId(100L);
        matching.setItemCount(1);

        DraftCartV3Response result = RpcCartUiState.selectDraftForRestaurant(
                java.util.Arrays.asList(other, matching),
                100L
        );

        assertSame(matching, result);
    }

    @Test
    public void checkoutAddressIntentPreservesCartContext() throws Exception {
        assertSourceContains("src/main/java/com/example/fooddelivery/ui/cart/Checkout.java",
                "public static Intent buildCheckoutAddressIntent",
                "intent.putExtra(\"open_address_source\", \"checkout\")",
                "intent.putExtra(\"cart_id\", cartId)",
                "intent.putExtra(\"restaurant_id\", restaurantId)");
    }

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
    public void homeHidesPlusCartOpensDraftAndRestaurantPlusOpensFoodDetail() throws Exception {
        assertSourceContains("src/main/java/com/example/fooddelivery/ui/home/HomeFragment.java",
                "navigateToRestaurantDetail",
                "action_home_to_restaurantDetail",
                "args.putLong(\"restaurant_id\", item.getRestaurantId())",
                "layoutStickyCart",
                "getDraftCartsV3()",
                "putExtra(\"orders_tab\", \"draft\")",
                "setSelectedItemId(R.id.nav_ordes)");
        assertSourceContains("src/main/java/com/example/fooddelivery/ui/home/adapters/TopSellingAdapter.java",
                "h.itemView.setOnClickListener",
                "h.imgFood.setOnClickListener",
                "h.btnAdd.setVisibility(showAddButton ? View.VISIBLE : View.GONE)");
        assertSourceContains("src/main/java/com/example/fooddelivery/ui/home/adapters/FoodVerticalAdapter.java",
                "h.itemView.setOnClickListener",
                "h.imgFood.setOnClickListener",
                "h.btnAdd.setVisibility(showAddButton ? View.VISIBLE : View.GONE)");
        assertSourceContains("src/main/java/com/example/fooddelivery/ui/home/HomeFragment.java",
                "this::navigateToRestaurantDetail",
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
                "quickAddToCart(view, item)",
                "orderRepository.addToCartV3(item.getId(), 1, null, Collections.emptyList())");
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
    public void foodDetailRendersSupabaseOptionsInlineBeforeAddingRpcCart() throws Exception {
        assertSourceContains("src/main/res/layout/food_fragment_detail.xml",
                "android:id=\"@+id/edNote\"",
                "android:id=\"@+id/foodDetailOptionsContainer\"",
                "android:id=\"@+id/tvFoodDetailNoOptionsMessage\"");
        assertSourceContains("src/main/java/com/example/fooddelivery/ui/detail/FoodDetailFragment.java",
                "orderRepository.getMenuItemDetailV3(item.getId())",
                "renderOptions(groups)",
                "selectedOptionIds",
                "orderRepository.addToCartV3(item.getId(), quantity, safeNote, selectedOptionIds)",
                "setFragmentResult(\"cart_changed\"",
                "popBackStack(R.id.homeFragment, false)");
        assertSourceDoesNotContain("src/main/java/com/example/fooddelivery/ui/detail/FoodDetailFragment.java",
                "new ToppingBottomSheet",
                "toppingSheet.show",
                "addToCartV3(item.getId(), quantity, null",
                "addToCartV3(item.getId(), quantity, safeNote, Collections.emptyList())");
    }

    @Test
    public void foodDetailLayoutDoesNotRenderHardcodedOptionGroups() throws Exception {
        assertSourceDoesNotContain("src/main/res/layout/food_fragment_detail.xml",
                "Option Group 1",
                "Option Group 2",
                "Chọn size",
                "Size vừa",
                "Size lớn",
                "Thêm Sốt",
                "Tương Chua Ngọt",
                "Tương Cà",
                "<RadioGroup",
                "<CheckBox");
    }

    @Test
    public void supabaseCartAndOrderTotalsIncludeSelectedOptionPriceDeltas() throws Exception {
        assertSourceContains("src/main/java/com/example/fooddelivery/data/repository/OrderRepository.java",
                "addToCartV3(long menuItemId, int quantity, String note, List<Long> optionChoiceIds)",
                "safeOptionChoiceIds");
        assertSourceContains("docs/supabase_v3_food_delivery_schema.sql",
                "create table if not exists public.carts",
                "create table if not exists public.orders",
                "create table if not exists public.cart_item_options",
                "create table if not exists public.order_lines",
                "price_delta_snapshot numeric(12, 2)",
                "coalesce(sum((mi.base_price + coalesce(opt.option_total, 0)) * ci.quantity), 0)",
                "mi.base_price + coalesce(opt.option_total, 0)",
                "values (v_order_id, p_payment_method, v_total, 'pending')");
        assertSourceContains("docs/live_cart_order_option_totals_patch.sql",
                "line unit price = menu_items.base_price + sum(cart_item_options.price_delta_snapshot)",
                "create or replace function public.get_draft_carts_v3()",
                "create or replace function public.get_cart_summary_v3(p_cart_id bigint)",
                "create or replace function public.checkout_cart_v3(",
                "coalesce(ci.last_known_unit_price, mi.base_price) + coalesce(opt.option_total, 0)",
                "insert into public.payments (order_id, provider, amount, status)");
    }

    @Test
    public void rpcCartSummaryMapsToppingInclusiveUnitPriceForLineItems() {
        String json = "{"
                + "\"cart_id\":44,"
                + "\"items\":[{"
                + "\"cart_item_id\":301,"
                + "\"menu_item_id\":12,"
                + "\"item_name\":\"Pho dac biet\","
                + "\"image_url\":\"https://example.com/pho.jpg\","
                + "\"quantity\":2,"
                + "\"base_price\":50000,"
                + "\"unit_price\":65000,"
                + "\"note\":\"it hanh\","
                + "\"options\":[{\"name\":\"Them bo\",\"price_delta\":15000}]"
                + "}],"
                + "\"subtotal\":130000,"
                + "\"delivery_fee\":0,"
                + "\"discount_amount\":0,"
                + "\"total_amount\":130000"
                + "}";
        CartSummaryV3Response summary = new Gson().fromJson(json, CartSummaryV3Response.class);

        List<LocalCart.CartEntry> entries = RpcCartUiState.mapSummaryItems(summary.getItems());

        assertEquals(1, entries.size());
        assertEquals(65000, entries.get(0).item.getPrice(), 0.001);
        assertEquals(130000, entries.get(0).subtotal(), 0.001);
    }

    @Test
    public void checkoutUsesRpcCartIdAndShowsReferenceFlowSections() throws Exception {
        assertSourceContains("src/main/java/com/example/fooddelivery/ui/cart/Checkout.java",
                "cartId = getIntent().getLongExtra(\"cart_id\", -1L)",
                "isRpcCheckout()",
                "buildCheckoutAddressIntent",
                "open_address_source",
                "sectionAddress.setOnClickListener(v -> openAddressFlow())",
                "if (!hasDeliveryAddress)",
                "orderRepository.checkoutCartV3(cartId, deliveryAddressId, \"COD\", note)",
                "setLoadingOverlayVisible(isSubmitting)",
                "openCartEditor()",
                "loadCartSummaryV3()",
                "tvAddMore.setText(\"Thêm món\")",
                "btnOrder.setText(\"Đặt món\")");
        assertSourceContains("src/main/res/layout/cart_activity_checkout.xml",
                "android:id=\"@+id/loadingOverlay\"",
                "android:id=\"@+id/rowDoorDelivery\"",
                "android:id=\"@+id/sectionGift\"",
                "android:id=\"@+id/sectionRecommendations\"",
                "android:id=\"@+id/tvAddMore\"");
        assertSourceContains("src/main/java/com/example/fooddelivery/ui/cart/adapters/CartBottomSheetAdapter.java",
                "void onEdit(LocalCart.CartEntry entry)",
                "holder.tvEdit.setOnClickListener");
        assertSourceContains("src/main/res/layout/cart_bottom_sheet_item.xml",
                "android:id=\"@+id/tvEditItem\"",
                "android:text=\"@string/");
    }

    @Test
    public void checkoutAddressFlowUsesFullAddressListAndReturnsToCheckout() throws Exception {
        assertSourceContains("src/main/java/com/example/fooddelivery/MainActivity.java",
                "\"checkout\".equals(intent.getStringExtra(\"open_address_source\"))",
                "args.putString(\"source\", \"checkout\")",
                "args.putLong(\"cart_id\", intent.getLongExtra(\"cart_id\", -1L))",
                "navController.navigate(R.id.addressListFragment, args)");
        assertSourceContains("src/main/java/com/example/fooddelivery/ui/profile/AddressListFragment.java",
                "\"checkout\".equals(source)",
                "repository.select(item.getId())",
                "returnToCheckout()",
                "intent.putExtra(\"cart_id\", checkoutCartId)",
                "intent.putExtra(\"restaurant_id\", checkoutRestaurantId)");
        assertSourceContains("src/main/java/com/example/fooddelivery/ui/profile/DeliveryAddressFormFragment.java",
                "checkoutCartId = getArguments().getLong(\"cart_id\", -1L)",
                "checkoutRestaurantId = getArguments().getLong(\"restaurant_id\", -1L)",
                "\"checkout\".equals(source)",
                "returnToCheckout()");
        assertSourceContains("src/main/res/navigation/nav_home.xml",
                "android:name=\"cart_id\"",
                "android:name=\"restaurant_id\"");
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
                "refreshDraftCartState(binding.getRoot(), activeCartRestaurantId, activeCartId)",
                "putExtra(\"orders_tab\", \"draft\")",
                "setSelectedItemId(R.id.nav_ordes)");
    }

    @Test
    public void orderTabsUseRpcStatusesForCompletedAndCancelled() throws Exception {
        assertSourceContains("src/main/java/com/example/fooddelivery/ui/order/OrderListFragment.java",
                "orderRepository.getDraftCartsV3()",
                "String rpcStatus = \"processing\".equals(tabStatus) ? null : tabStatus",
                "orderRepository.getMyOrdersV3(rpcStatus)");
    }

    @Test
    public void orderManagementDefaultsToDraftTabWhenNoTabRequested() throws Exception {
        assertSourceContains("src/main/java/com/example/fooddelivery/ui/order/OrderManagementFragment.java",
                "int initialTab = 0",
                "\"processing\".equals(requestedTab)",
                "initialTab = 1");
        assertSourceDoesNotContain("src/main/java/com/example/fooddelivery/ui/order/OrderManagementFragment.java",
                "int initialTab = 1;");
    }

    @Test
    public void checkoutRpcCartQuantityButtonsMutateServerCart() throws Exception {
        assertSourceContains("src/main/java/com/example/fooddelivery/ui/cart/Checkout.java",
                "updateRpcCartItemQuantity(entry, entry.quantity + 1)",
                "updateRpcCartItemQuantity(entry, entry.quantity - 1)",
                "removeRpcCartItem(entry)",
                "orderRepository.updateCartItemQuantityV3(entry.cartItemId, quantity)",
                "orderRepository.removeCartItemV3(entry.cartItemId)",
                "loadCartSummaryV3()");
        assertSourceDoesNotContain("src/main/java/com/example/fooddelivery/ui/cart/Checkout.java",
                "if (isRpcCheckout()) return;");
    }

    @Test
    public void draftOrdersSupportLongPressClearCartConfirmation() throws Exception {
        assertSourceContains("src/main/java/com/example/fooddelivery/ui/order/adapters/OrderAdapter.java",
                "void onOrderLongClick(Order order)",
                "setOnLongClickListener",
                "itemView.setAlpha(0.65f)",
                "itemView.setAlpha(1f)");
        assertSourceContains("src/main/java/com/example/fooddelivery/ui/order/OrderListFragment.java",
                "onOrderLongClick(Order order)",
                "showDeleteDraftCartDialog(order)",
                "order.getRpcCartId() > 0",
                "orderRepository.clearCartV3(cartId)",
                "loadOrders()");
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
