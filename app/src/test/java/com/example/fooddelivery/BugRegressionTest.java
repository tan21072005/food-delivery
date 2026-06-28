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
                "action_menu_to_foodDetail",
                "viewModel.addToCart");
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
