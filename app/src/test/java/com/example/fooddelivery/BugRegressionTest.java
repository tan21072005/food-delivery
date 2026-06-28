package com.example.fooddelivery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.example.fooddelivery.data.remote.apis.ApiService;
import com.example.fooddelivery.data.model.DeliveryAddress;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.parsers.DocumentBuilderFactory;

import retrofit2.http.Headers;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PATCH;

public class BugRegressionTest {

    @Test
    public void addToCartUsesRestUpsertSoMissingRpcDoesNot404() throws Exception {
        Method method = Arrays.stream(ApiService.class.getMethods())
                .filter(candidate -> candidate.getName().equals("addToCart"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing addToCart endpoint"));

        POST post = method.getAnnotation(POST.class);
        Headers headers = method.getAnnotation(Headers.class);

        assertNotNull(post);
        assertEquals("rest/v1/carts?on_conflict=user_id,menu_id", post.value());
        assertEquals("CartRequest", method.getParameterTypes()[0].getSimpleName());
        assertNotNull(headers);
        assertTrue(Arrays.asList(headers.value()).contains("Prefer: resolution=merge-duplicates,return=minimal"));
    }

    @Test
    public void addToCartSuccessNavigatesToCheckoutFromInteractiveSurfaces() throws Exception {
        assertSourceContains("src/main/java/com/example/fooddelivery/ui/home/HomeFragment.java",
                "getCartAddedEvent().observe",
                "new Intent(requireContext(), Checkout.class)");
        assertSourceContains("src/main/java/com/example/fooddelivery/ui/detail/FoodDetailFragment.java",
                "getCartAddedEvent().observe",
                "new Intent(requireContext(), Checkout.class)");
        assertSourceContains("src/main/java/com/example/fooddelivery/ui/search/SearchFragment.java",
                "getCartAddedEvent().observe",
                "new Intent(requireContext(), Checkout.class)");
    }

    @Test
    public void homeMenuActionAndMenuFragmentAreInteractive() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        Document document = factory.newDocumentBuilder().parse(Files.newInputStream(projectPath("src/main/res/navigation/nav_home.xml")));

        NodeList actions = document.getElementsByTagName("action");
        boolean foundMenuAction = false;
        for (int i = 0; i < actions.getLength(); i++) {
            org.w3c.dom.Element action = (org.w3c.dom.Element) actions.item(i);
            if ("@+id/action_home_to_menu".equals(action.getAttributeNS("http://schemas.android.com/apk/res/android", "id"))) {
                assertEquals("@id/menuFragment", action.getAttributeNS("http://schemas.android.com/apk/res-auto", "destination"));
                foundMenuAction = true;
            }
        }
        assertTrue("Missing action_home_to_menu", foundMenuAction);

        String menuLayout = readFile(projectPath("src/main/res/layout/menu_fragment.xml"));
        assertTrue(menuLayout.contains("RecyclerView"));
        assertSourceContains("src/main/java/com/example/fooddelivery/ui/menu/MenuFragment.java",
                "MenuAdapter",
                "action_menu_to_restaurantDetail",
                "ToppingBottomSheet",
                "LocalCart.getInstance().addItem");
    }

    @Test
    public void profileMenuTextAndIconsUseStableVisibleThemeColors() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        Document document = factory.newDocumentBuilder().parse(Files.newInputStream(profileLayoutPath()));

        NodeList menuTextViews = document.getElementsByTagName("TextView");
        int checkedTextViews = 0;
        for (int i = 0; i < menuTextViews.getLength(); i++) {
            org.w3c.dom.Element element = (org.w3c.dom.Element) menuTextViews.item(i);
            String text = element.getAttributeNS("http://schemas.android.com/apk/res/android", "text");
            if (text != null && !text.isEmpty()) {
                checkedTextViews++;
            }
        }

        NodeList imageViews = document.getElementsByTagName("ImageView");
        int checkedIconViews = 0;
        for (int i = 0; i < imageViews.getLength(); i++) {
            org.w3c.dom.Element element = (org.w3c.dom.Element) imageViews.item(i);
            String src = element.getAttributeNS("http://schemas.android.com/apk/res/android", "src");
            if (src != null && src.startsWith("@drawable/ic_")) {
                checkedIconViews++;
            }
        }

        assertTrue("Profile layout should contain text rows", checkedTextViews > 0);
        assertTrue("Profile layout should contain icon rows", checkedIconViews > 0);
        assertThemeItem("src/main/res/values/themes.xml", "android:textColorPrimary", "@color/text_primary");
        assertThemeItem("src/main/res/values/themes.xml", "colorControlNormal", "@color/text_primary");
        assertThemeItem("src/main/res/values-night/themes.xml", "android:textColorPrimary", "@color/text_primary");
        assertThemeItem("src/main/res/values-night/themes.xml", "colorControlNormal", "@color/text_primary");
    }

    @Test
    public void mainNavigationGraphInflatesAuthBeforeGraphsThatReferenceIt() throws Exception {
        String navMain = readFile(projectPath("src/main/res/navigation/nav_main.xml"));
        int authInclude = navMain.indexOf("@navigation/nav_auth");
        int favoritesInclude = navMain.indexOf("@navigation/nav_favorites");
        int profileInclude = navMain.indexOf("@navigation/nav_profile");

        assertTrue("nav_auth must be included before nav_favorites because favorites has actions to @id/nav_auth",
                authInclude >= 0 && favoritesInclude >= 0 && authInclude < favoritesInclude);
        assertTrue("nav_auth must be included before nav_profile because profile has actions to @id/nav_auth",
                authInclude >= 0 && profileInclude >= 0 && authInclude < profileInclude);

        for (String graph : Arrays.asList("nav_home.xml", "nav_ordes.xml", "nav_favorites.xml", "nav_profile.xml", "nav_auth.xml")) {
            String source = readFile(projectPath("src/main/res/navigation/" + graph));
            assertFalse("Navigation action destinations should reference existing ids with @id, not create new ids with @+id in " + graph,
                    source.contains("app:destination=\"@+id/"));
        }
    }

    @Test
    public void restaurantRatingClickNavigatesToUsableReviewsScreen() throws Exception {
        assertSourceContains("src/main/java/com/example/fooddelivery/ui/detail/RestaurantDetailFragment.java",
                "R.id.layoutRatingReview",
                "R.id.action_restaurantDetail_to_reviews");

        String navHome = readFile(projectPath("src/main/res/navigation/nav_home.xml"));
        assertTrue(navHome.contains("android:id=\"@+id/action_restaurantDetail_to_reviews\""));
        assertTrue(navHome.contains("app:destination=\"@id/reviewsFragment\""));
    }

    @Test
    public void detailScreensExposeBackHandlersAndScreenLayoutsReserveTopInset() throws Exception {
        assertSourceContains("src/main/java/com/example/fooddelivery/ui/order/OrderDetailFragment.java",
                "R.id.ivBack",
                "popBackStack()");
        assertSourceContains("src/main/java/com/example/fooddelivery/ui/order/OrderReviewFragment.java",
                "R.id.ivBack",
                "popBackStack()");
        assertSourceContains("src/main/java/com/example/fooddelivery/ui/cart/Checkout.java",
                "R.id.ivBack",
                "finish()");

        for (String layout : Arrays.asList(
                "src/main/res/layout/home_fragment.xml",
                "src/main/res/layout/search_fragment.xml",
                "src/main/res/layout/menu_fragment.xml",
                "src/main/res/layout/profile_fragment.xml",
                "src/main/res/layout/favorites_fragment.xml",
                "src/main/res/layout/food_fragment_detail.xml",
                "src/main/res/layout/fragment_restaurant_detail.xml",
                "src/main/res/layout/fragment_restaurant_info.xml",
                "src/main/res/layout/fragment_promotions.xml",
                "src/main/res/layout/fragment_reviews.xml",
                "src/main/res/layout/fragment_address_list.xml",
                "src/main/res/layout/order_fragment_management.xml",
                "src/main/res/layout/order_fragment_list.xml",
                "src/main/res/layout/order_fragment_detail.xml",
                "src/main/res/layout/order_fragment_review.xml",
                "src/main/res/layout/cart_activity_checkout.xml")) {
            String source = readFile(projectPath(layout));
            assertTrue("Layout should reserve top inset with fitsSystemWindows: " + layout,
                    source.contains("android:fitsSystemWindows=\"true\""));
        }
    }

    @Test
    public void checkoutSuccessReturnsToOrdersPendingTab() throws Exception {
        assertSourceContains("src/main/java/com/example/fooddelivery/ui/cart/Checkout.java",
                "putExtra(\"open_tab\", \"orders\")",
                "putExtra(\"orders_tab\", \"pending\")");
        assertSourceContains("src/main/java/com/example/fooddelivery/ui/order/OrderManagementFragment.java",
                "EXTRA_ORDERS_TAB",
                "getStringExtra(EXTRA_ORDERS_TAB)",
                "viewPager.setCurrentItem(initialTab, false)");
    }

    @Test
    public void deliveryAddressDisplayTextUsesRecipientAndFullAddressSnapshot() {
        DeliveryAddress address = new DeliveryAddress();
        address.setLabel("Nhà");
        address.setRecipientName("Nguyễn Văn A");
        address.setRecipientPhone("0901234567");
        address.setFullAddress("Phòng 605, Linh Đàm");

        assertEquals("Nguyễn Văn A - 0901234567\nPhòng 605, Linh Đàm", address.toCheckoutDisplayText());
    }

    @Test
    public void apiServiceExposesDeliveryAddressRestAndDefaultRpcContracts() throws Exception {
        assertEndpoint("getDeliveryAddresses", GET.class, "rest/v1/user_addresses");
        assertEndpoint("createDeliveryAddress", POST.class, "rest/v1/user_addresses");
        assertEndpoint("updateDeliveryAddress", PATCH.class, "rest/v1/user_addresses");
        assertEndpoint("softDeleteDeliveryAddress", PATCH.class, "rest/v1/user_addresses");
        assertEndpoint("setDefaultDeliveryAddress", POST.class, "rest/v1/rpc/set_default_delivery_address");
    }

    @Test
    public void checkoutActivityUsesSupabaseViewModelAndSelectedDeliveryAddress() throws Exception {
        assertSourceContains("src/main/java/com/example/fooddelivery/ui/cart/Checkout.java",
                "CheckoutViewModel",
                "loadDefaultDeliveryAddress",
                "checkout(selectedDeliveryAddress",
                "CartAdapter");
        assertSourceContains("src/main/java/com/example/fooddelivery/ui/cart/Checkout.java",
                "LocalCart is a legacy fallback",
                "LocalOrderStore");
    }

    @Test
    public void sqlContractUsesCanonicalOrderStatusesAndDeliveryAddressSnapshots() throws Exception {
        String sql = readFile(projectPath("../docs/sql.sql"));
        assertTrue(sql.contains("CREATE TYPE order_status     AS ENUM ('pending', 'confirmed', 'preparing', 'ready_for_pickup', 'delivering', 'completed', 'cancelled')"));
        assertFalse(sql.contains("'ready', 'on_the_way', 'delivered'"));
        assertTrue(sql.contains("recipient_name"));
        assertTrue(sql.contains("recipient_phone"));
        assertTrue(sql.contains("delivery_address_id"));
        assertTrue(sql.contains("recipient_name_snapshot"));
        assertTrue(sql.contains("item_name_snapshot"));
        assertTrue(sql.contains("item_image_snapshot"));
    }

    @Test
    public void checkoutRpcAcceptsDeliveryAddressIdAndSnapshotsOrderLines() throws Exception {
        String rpc = readFile(projectPath("../docs/rpc_cart_order.sql"));
        assertTrue(rpc.contains("checkout_cart(p_delivery_address_id BIGINT, p_note TEXT)"));
        assertTrue(rpc.contains("ua.user_id = v_user_id"));
        assertTrue(rpc.contains("recipient_name_snapshot"));
        assertTrue(rpc.contains("item_name_snapshot"));
        assertTrue(rpc.contains("item_image_snapshot"));
        assertFalse(rpc.contains("checkout_cart(p_delivery_address TEXT"));
    }

    private Path profileLayoutPath() {
        return projectPath("src/main/res/layout/profile_fragment.xml");
    }

    private void assertThemeItem(String path, String itemName, String expectedValue) throws Exception {
        Path themePath = projectPath(path);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        Document document = factory.newDocumentBuilder().parse(Files.newInputStream(themePath));
        NodeList items = document.getElementsByTagName("item");
        for (int i = 0; i < items.getLength(); i++) {
            org.w3c.dom.Element item = (org.w3c.dom.Element) items.item(i);
            if (itemName.equals(item.getAttribute("name"))) {
                assertEquals(expectedValue, item.getTextContent().trim());
                return;
            }
        }
        throw new AssertionError("Missing theme item: " + itemName + " in " + path);
    }

    private void assertSourceContains(String path, String... snippets) throws Exception {
        String source = readFile(projectPath(path));
        for (String snippet : snippets) {
            assertTrue("Missing snippet in " + path + ": " + snippet, source.contains(snippet));
        }
    }

    private void assertEndpoint(String methodName, Class<?> annotationClass, String expectedValue) throws Exception {
        Method method = Arrays.stream(ApiService.class.getMethods())
                .filter(candidate -> candidate.getName().equals(methodName))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing endpoint: " + methodName));

        Object annotation = method.getAnnotation((Class) annotationClass);
        assertNotNull("Missing annotation on endpoint: " + methodName, annotation);

        Method value = annotationClass.getMethod("value");
        assertEquals(expectedValue, value.invoke(annotation));
    }

    private Path projectPath(String path) {
        Path moduleRelative = Paths.get(path);
        if (Files.exists(moduleRelative)) {
            return moduleRelative;
        }
        return Paths.get("app").resolve(path);
    }

    private String readFile(Path path) throws Exception {
        return new String(Files.readAllBytes(path), java.nio.charset.StandardCharsets.UTF_8);
    }
}
