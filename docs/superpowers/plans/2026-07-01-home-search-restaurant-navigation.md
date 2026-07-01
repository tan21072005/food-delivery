# Home Search Restaurant Navigation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Home search opens a focused search-only fragment, filters menu items by name, shows `khong co ket qua` when nothing matches, and opens the Restaurant page for tapped menu results.

**Architecture:** Keep the existing `SearchFragment` entry point and existing `MenuViewModel` network loading. Add a pure `MenuItemSearchFilter` so name-only filtering is testable without Android framework dependencies. Use the existing `FoodVerticalAdapter` with add buttons hidden and route item clicks to `restaurant_id`.

**Tech Stack:** Android Java, AndroidX Fragment/ViewModel/LiveData/Navigation, RecyclerView, JUnit 4.

## Global Constraints

- Do not change Cart, Checkout, Restaurant detail internals, or Supabase schema.
- Search results must use menu item names only, not descriptions.
- Empty results text must be exactly `khong co ket qua`.
- Tapping a menu result from Search must navigate to Restaurant detail, not Food detail.
- Keep Home search as the only entry behavior changed in Home.

---

### Task 1: Search Filtering Contract

**Files:**
- Create: `app/src/main/java/com/example/fooddelivery/ui/search/MenuItemSearchFilter.java`
- Test: `app/src/test/java/com/example/fooddelivery/ui/search/MenuItemSearchFilterTest.java`

**Interfaces:**
- Consumes: `FoodItem.getName()` and `FoodItem.getRestaurantId()`.
- Produces: `MenuItemSearchFilter.filterByName(List<FoodItem> source, String query): List<FoodItem>`.

- [ ] **Step 1: Write the failing test**

```java
@Test public void filtersMenuItemsByNameOnlyIgnoringDescription() {
    FoodItem pho = item(1, 10, "Pho bo", "popular soup");
    FoodItem hidden = item(2, 11, "Com tam", "Pho appears only here");

    List<FoodItem> results = MenuItemSearchFilter.filterByName(Arrays.asList(pho, hidden), "pho");

    assertEquals(1, results.size());
    assertEquals(10, results.get(0).getRestaurantId());
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "com.example.fooddelivery.ui.search.MenuItemSearchFilterTest"`

Expected: FAIL because `MenuItemSearchFilter` does not exist.

- [ ] **Step 3: Write minimal implementation**

```java
public final class MenuItemSearchFilter {
    public static List<FoodItem> filterByName(List<FoodItem> source, String query) {
        if (source == null || query == null || query.trim().isEmpty()) return new ArrayList<>();
        String normalizedQuery = query.trim().toLowerCase(Locale.ROOT);
        List<FoodItem> results = new ArrayList<>();
        for (FoodItem item : source) {
            String name = item.getName() == null ? "" : item.getName().toLowerCase(Locale.ROOT);
            if (name.contains(normalizedQuery)) results.add(item);
        }
        return results;
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "com.example.fooddelivery.ui.search.MenuItemSearchFilterTest"`

Expected: PASS.

### Task 2: Search Fragment UI And Navigation

**Files:**
- Modify: `app/src/main/java/com/example/fooddelivery/ui/search/SearchFragment.java`
- Modify: `app/src/main/res/layout/search_fragment.xml`
- Modify: `app/src/main/res/navigation/nav_home.xml`
- Test: `app/src/test/java/com/example/fooddelivery/BugRegressionTest.java`

**Interfaces:**
- Consumes: `MenuItemSearchFilter.filterByName(...)`.
- Produces: Search result click navigation to `R.id.action_search_to_restaurantDetail` with `restaurant_id`.

- [ ] **Step 1: Write failing source/navigation regression tests**

Check that Search uses `action_search_to_restaurantDetail`, passes `item.getRestaurantId()`, shows `khong co ket qua`, requests keyboard display, and no longer defines the old Search-to-Food detail action.

- [ ] **Step 2: Run tests to verify they fail**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "com.example.fooddelivery.BugRegressionTest.searchScreenRoutesMenuResultsToRestaurantDetail"`

Expected: FAIL because Search still routes to Food detail and layout still contains filter controls.

- [ ] **Step 3: Implement SearchFragment and XML changes**

Use a local `allMenuItems` cache populated once from `MenuViewModel.loadFoods("")`, call the pure filter on text changes, show keyboard after `requestFocus()`, hide add-to-cart buttons, and navigate result clicks with `restaurant_id`.

- [ ] **Step 4: Run focused tests**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "com.example.fooddelivery.ui.search.MenuItemSearchFilterTest" --tests "com.example.fooddelivery.BugRegressionTest.searchScreenRoutesMenuResultsToRestaurantDetail"`

Expected: PASS.

### Task 3: Build Verification

**Files:**
- Modify only files from previous tasks.

**Interfaces:**
- Consumes: completed Task 1 and Task 2.
- Produces: compile-checked app.

- [ ] **Step 1: Run compile check**

Run: `.\gradlew.bat :app:compileDebugJavaWithJavac`

Expected: BUILD SUCCESSFUL.

- [ ] **Step 2: Run debug assemble**

Run: `.\gradlew.bat :app:assembleDebug`

Expected: BUILD SUCCESSFUL.
