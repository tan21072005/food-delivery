package com.example.fooddelivery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.example.fooddelivery.data.remote.apis.ApiService;

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
import retrofit2.http.POST;

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
    public void orderingSurfacesAddToPerRestaurantDraftCartWithoutClearingOtherCarts() throws Exception {
        assertSourceContains("src/main/java/com/example/fooddelivery/ui/detail/FoodDetailFragment.java",
                "orderRepository.addToCartV3");
        assertSourceDoesNotContain("src/main/java/com/example/fooddelivery/ui/detail/FoodDetailFragment.java",
                "hasDifferentRestaurant",
                "Xoa va them mon moi",
                "cart.clear()");
        assertSourceDoesNotContain("src/main/java/com/example/fooddelivery/ui/detail/FoodDetailFragment.java",
                "new Intent(requireContext(), Checkout.class)");

        assertSourceContains("src/main/java/com/example/fooddelivery/ui/detail/RestaurantDetailFragment.java",
                "action_restaurantDetail_to_foodDetail",
                "CartBottomSheet",
                "layoutStickyCart");
        assertSourceDoesNotContain("src/main/java/com/example/fooddelivery/ui/detail/RestaurantDetailFragment.java",
                "new ToppingBottomSheet",
                "toppingSheet.show");
        assertSourceDoesNotContain("src/main/java/com/example/fooddelivery/ui/detail/RestaurantDetailFragment.java",
                "Xoa va them mon moi",
                "cart.clear()");
        assertSourceContains("src/main/java/com/example/fooddelivery/ui/menu/MenuFragment.java",
                "action_menu_to_foodDetail",
                "CartBottomSheet",
                "layoutStickyCart");
        assertSourceDoesNotContain("src/main/java/com/example/fooddelivery/ui/menu/MenuFragment.java",
                "new ToppingBottomSheet",
                "toppingSheet.show");
        assertSourceDoesNotContain("src/main/java/com/example/fooddelivery/ui/menu/MenuFragment.java",
                "Xoa va them mon moi",
                "cart.clear()");
    }

    @Test
    public void foodDetailLoadsSelectedFoodInsteadOfMockingRestaurantOne() throws Exception {
        assertSourceContains("src/main/java/com/example/fooddelivery/data/repository/FoodRepository.java",
                "getFoodById",
                "\"eq.\" + foodId");
        assertSourceContains("src/main/java/com/example/fooddelivery/data/remote/apis/ApiService.java",
                "getMenuItemById",
                "@Query(\"id\")");
        assertSourceContains("src/main/java/com/example/fooddelivery/ui/detail/FoodDetailViewModel.java",
                "foodRepository.getFoodById(foodId)");
        assertSourceDoesNotContain("src/main/java/com/example/fooddelivery/ui/detail/FoodDetailViewModel.java",
                "new FoodItem(foodId",
                "setRestaurantId(1)");
    }

    @Test
    public void orderSurfacesUseDeliveryAddressCopyInsteadOfTableServiceCopy() throws Exception {
        assertSourceDoesNotContain("src/main/java/com/example/fooddelivery/data/local/LocalOrderStore.java",
                "Bàn",
                "tầng");
        assertSourceDoesNotContain("src/main/res/values/strings.xml",
                "Bàn",
                "tầng");
        assertSourceContains("src/main/res/values/strings.xml",
                "label_delivery_address",
                "Địa chỉ giao hàng");
    }

    @Test
    public void homeCardsOpenRestaurantWithoutPlusAndCartOpensDraftOrders() throws Exception {
        String homeFragment = readFile(projectPath("src/main/java/com/example/fooddelivery/ui/home/HomeFragment.java"));
        String homeViewModel = readFile(projectPath("src/main/java/com/example/fooddelivery/ui/home/HomeViewModel.java"));
        String homeLayout = readFile(projectPath("src/main/res/layout/home_fragment.xml"));

        assertFalse("HomeFragment must not open a topping/cart shortcut flow",
                homeFragment.contains("ToppingBottomSheet"));
        assertFalse("HomeFragment must not mutate the local Cart",
                homeFragment.contains("LocalCart.getInstance().add"));
        assertFalse("HomeFragment must not observe add-to-cart events and jump to Checkout",
                homeFragment.contains("getCartAddedEvent().observe"));
        assertFalse("HomeViewModel must not expose Home add-to-cart behavior",
                homeViewModel.contains("addToCart("));
        assertTrue("Home should show sticky cart after restaurant/cart changes",
                homeLayout.contains("layoutStickyCart"));
        assertSourceContains("src/main/java/com/example/fooddelivery/ui/home/HomeFragment.java",
                "navigateToRestaurantDetail",
                "action_home_to_restaurantDetail",
                "args.putLong(\"restaurant_id\", item.getRestaurantId())",
                "setFragmentResultListener(\"cart_changed\"",
                "getDraftCartsV3()",
                "this::navigateToRestaurantDetail",
                "null,",
                "false",
                "putExtra(\"orders_tab\", \"draft\")",
                "setSelectedItemId(R.id.nav_ordes)");
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
                "action_menu_to_foodDetail",
                "openFoodDetail");
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

        String reviewsLayout = readFile(projectPath("src/main/res/layout/fragment_reviews.xml"));
        assertTrue("ReviewsFragment registers btnResetFilter, so the layout must define it",
                reviewsLayout.contains("android:id=\"@+id/btnResetFilter\""));
        assertTrue(reviewsLayout.contains("android:id=\"@+id/chipPhoto\""));
        assertTrue(reviewsLayout.contains("android:id=\"@+id/chipStar\""));
        assertTrue(reviewsLayout.contains("android:id=\"@+id/tvReset\""));
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
        return new String(Files.readAllBytes(path), java.nio.charset.StandardCharsets.UTF_8);
    }
}
