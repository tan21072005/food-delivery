# Ordering UX Flow Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make restaurant carts isolated per restaurant and make Checkout use the full AddressList flow before placing an RPC order.

**Architecture:** Keep the existing RPC cart and address repositories. Add a restaurant-specific draft selector for RestaurantDetail/Menu contexts while leaving Home's global draft shortcut intact. Route Checkout to MainActivity/AddressList with `source=checkout`, preserve `cart_id` and `restaurant_id`, and return to Checkout after a saved address is selected or created.

**Tech Stack:** Android Java, Jetpack Navigation fragments, AppCompat Activity, Retrofit/Supabase RPCs, JUnit/Robolectric unit tests.

## Global Constraints

- Restaurant detail must only show the draft cart for the current `restaurant_id`.
- Home may show an active/global draft cart because it opens the Orders draft tab.
- Checkout address selection must use the full `AddressListFragment`, not a lightweight picker.
- Checkout must preserve `cart_id` and `restaurant_id` while selecting an address.
- Completed and cancelled order tabs must continue to use `get_my_orders_v3` with the expected status.

---

## File Structure

- Modify `app/src/main/java/com/example/fooddelivery/ui/cart/RpcCartUiState.java`
  - Add a helper that returns only a draft matching a restaurant id.
- Modify `app/src/main/java/com/example/fooddelivery/ui/detail/RestaurantDetailFragment.java`
  - Use the matching-restaurant helper and clear sticky cart state when no matching draft exists.
- Modify `app/src/main/java/com/example/fooddelivery/ui/menu/MenuFragment.java`
  - Keep menu sticky behavior consistent when a restaurant id is known from an add/cart result.
- Modify `app/src/main/java/com/example/fooddelivery/ui/cart/Checkout.java`
  - Open the address flow with checkout source and reload selected addresses on resume.
- Modify `app/src/main/java/com/example/fooddelivery/MainActivity.java`
  - Recognize checkout address extras and navigate Home nav host to AddressList.
- Modify `app/src/main/java/com/example/fooddelivery/ui/profile/AddressListFragment.java`
  - In `source=checkout`, selecting an address returns to Checkout with preserved cart context.
- Modify `app/src/main/java/com/example/fooddelivery/ui/profile/DeliveryAddressFormFragment.java`
  - In `source=checkout`, after saving a new/edited address, return to Checkout with preserved cart context.
- Test `app/src/test/java/com/example/fooddelivery/CartUiFlowRegressionTest.java`
  - Add unit coverage for restaurant cart isolation and checkout address intent extras.
- Test `app/src/test/java/com/example/fooddelivery/BugRegressionTest.java`
  - Keep existing home/restaurant plus regression coverage.

---

### Task 1: Restaurant Draft Cart Isolation

**Files:**
- Modify: `app/src/main/java/com/example/fooddelivery/ui/cart/RpcCartUiState.java`
- Modify: `app/src/main/java/com/example/fooddelivery/ui/detail/RestaurantDetailFragment.java`
- Modify: `app/src/main/java/com/example/fooddelivery/ui/menu/MenuFragment.java`
- Test: `app/src/test/java/com/example/fooddelivery/CartUiFlowRegressionTest.java`

**Interfaces:**
- Consumes: `DraftCartV3Response#getRestaurantId()`, `DraftCartV3Response#getCartId()`, `DraftCartV3Response#getItemCount()`
- Produces:
  - `public static DraftCartV3Response selectDraftForRestaurant(List<DraftCartV3Response> drafts, long restaurantId)`
  - `private void clearActiveCartState()` in restaurant/menu fragments

- [ ] **Step 1: Write the failing helper test**

Add this test to `CartUiFlowRegressionTest.java`:

```java
@Test
public void selectDraftForRestaurantDoesNotFallbackToOtherRestaurant() throws Exception {
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
public void selectDraftForRestaurantReturnsOnlyMatchingRestaurant() throws Exception {
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
```

- [ ] **Step 2: Run the focused unit test and verify failure**

Run:

```powershell
$env:ANDROID_HOME='C:\Users\Admin\AppData\Local\Android\Sdk'; .\gradlew.bat :app:testDebugUnitTest --tests "com.example.fooddelivery.CartUiFlowRegressionTest"
```

Expected: FAIL because `RpcCartUiState.selectDraftForRestaurant` does not exist.

- [ ] **Step 3: Implement the helper**

Add to `RpcCartUiState.java`:

```java
public static DraftCartV3Response selectDraftForRestaurant(List<DraftCartV3Response> drafts,
                                                           long restaurantId) {
    if (restaurantId <= 0 || drafts == null || drafts.isEmpty()) return null;

    for (DraftCartV3Response draft : drafts) {
        if (draft == null || draft.getCartId() <= 0) continue;
        if (draft.getRestaurantId() == restaurantId) {
            return draft;
        }
    }
    return null;
}
```

- [ ] **Step 4: Use the helper in RestaurantDetail**

In `RestaurantDetailFragment.refreshDraftCartState`, replace the call to `selectActiveDraft(response.body(), preferredRestaurantId)` with:

```java
DraftCartV3Response draft = RpcCartUiState.selectDraftForRestaurant(
        response.body(),
        restaurantId
);
```

Add this method to the fragment:

```java
private void clearActiveCartState() {
    activeCartId = -1L;
    activeCartRestaurantId = -1L;
    activeCartItemCount = 0;
    activeCartTotal = 0;
}
```

Before the final `updateStickyCart(view);` path when no draft and no fallback cart exists, call:

```java
clearActiveCartState();
updateStickyCart(view);
```

- [ ] **Step 5: Keep MenuFragment from leaking carts when restaurant context is known**

In `MenuFragment.refreshDraftCartState`, when `preferredRestaurantId > 0`, use:

```java
DraftCartV3Response draft = preferredRestaurantId > 0
        ? RpcCartUiState.selectDraftForRestaurant(response.body(), preferredRestaurantId)
        : RpcCartUiState.selectActiveDraft(response.body(), preferredRestaurantId);
```

Add the same `clearActiveCartState()` helper and use it before `updateStickyCart(view)` when no draft and no fallback exists.

- [ ] **Step 6: Run tests and compile**

Run:

```powershell
$env:ANDROID_HOME='C:\Users\Admin\AppData\Local\Android\Sdk'; .\gradlew.bat :app:testDebugUnitTest --tests "com.example.fooddelivery.CartUiFlowRegressionTest"
$env:ANDROID_HOME='C:\Users\Admin\AppData\Local\Android\Sdk'; .\gradlew.bat :app:compileDebugJavaWithJavac
```

Expected: both commands PASS.

---

### Task 2: Checkout Full AddressList Flow

**Files:**
- Modify: `app/src/main/java/com/example/fooddelivery/ui/cart/Checkout.java`
- Modify: `app/src/main/java/com/example/fooddelivery/MainActivity.java`
- Modify: `app/src/main/java/com/example/fooddelivery/ui/profile/AddressListFragment.java`
- Modify: `app/src/main/java/com/example/fooddelivery/ui/profile/DeliveryAddressFormFragment.java`
- Test: `app/src/test/java/com/example/fooddelivery/CartUiFlowRegressionTest.java`

**Interfaces:**
- Consumes: `DeliveryAddressRepository.select(String id)`, `Checkout` extras `cart_id` and `restaurant_id`
- Produces:
  - Checkout launch extras: `open_address_source=checkout`, `cart_id`, `restaurant_id`
  - AddressList argument keys: `source`, `cart_id`, `restaurant_id`

- [ ] **Step 1: Add checkout address navigation tests**

Add a package-visible helper to `Checkout` and test its extras:

```java
static Intent buildCheckoutAddressIntent(android.content.Context context, long cartId, long restaurantId) {
    Intent intent = new Intent(context, MainActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    intent.putExtra("open_address_source", "checkout");
    intent.putExtra("cart_id", cartId);
    intent.putExtra("restaurant_id", restaurantId);
    return intent;
}
```

Then test:

```java
@Test
public void checkoutAddressIntentPreservesCartContext() {
    android.content.Context context = ApplicationProvider.getApplicationContext();

    Intent intent = Checkout.buildCheckoutAddressIntent(context, 99L, 7L);

    assertEquals("checkout", intent.getStringExtra("open_address_source"));
    assertEquals(99L, intent.getLongExtra("cart_id", -1L));
    assertEquals(7L, intent.getLongExtra("restaurant_id", -1L));
}
```

- [ ] **Step 2: Run focused test and verify failure**

Run:

```powershell
$env:ANDROID_HOME='C:\Users\Admin\AppData\Local\Android\Sdk'; .\gradlew.bat :app:testDebugUnitTest --tests "com.example.fooddelivery.CartUiFlowRegressionTest"
```

Expected: FAIL until `buildCheckoutAddressIntent` exists.

- [ ] **Step 3: Implement Checkout address launch**

In `Checkout.java`, import or use `MainActivity`, then add:

```java
static Intent buildCheckoutAddressIntent(android.content.Context context, long cartId, long restaurantId) {
    Intent intent = new Intent(context, MainActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    intent.putExtra("open_address_source", "checkout");
    intent.putExtra("cart_id", cartId);
    intent.putExtra("restaurant_id", restaurantId);
    return intent;
}

private void openAddressFlow() {
    startActivity(buildCheckoutAddressIntent(this, cartId, restaurantId));
}
```

Change the `tvChangeAddress` click listener to:

```java
tvChangeAddress.setOnClickListener(v -> openAddressFlow());
```

Also make the address section clickable:

```java
View sectionAddress = findViewById(R.id.sectionAddress);
sectionAddress.setOnClickListener(v -> openAddressFlow());
```

Change `btnOrder.setOnClickListener` to open addresses when blocked by missing address:

```java
btnOrder.setOnClickListener(v -> {
    if (!hasDeliveryAddress) {
        openAddressFlow();
        return;
    }
    placeOrder();
});
```

Override `onResume`:

```java
@Override
protected void onResume() {
    super.onResume();
    if (deliveryAddressRepository != null && tvAddressTitle != null) {
        loadCurrentDeliveryAddress();
    }
}
```

- [ ] **Step 4: Route MainActivity to AddressList in checkout mode**

In `MainActivity.handleIntent(Intent intent)`, after the existing `open_tab=orders` handling, read:

```java
String addressSource = getIntent().getStringExtra("open_address_source");
if ("checkout".equals(addressSource)) {
    Bundle args = new Bundle();
    args.putString("source", "checkout");
    args.putLong("cart_id", intent.getLongExtra("cart_id", -1L));
    args.putLong("restaurant_id", intent.getLongExtra("restaurant_id", -1L));
    binding.bottomNav.setSelectedItemId(R.id.nav_home);
    navController.navigate(R.id.addressListFragment, args);
}
```

The project uses `main_activity.xml`, `R.id.navHostFragment`, and the existing `navController` field, so no additional nav-host lookup is needed.

- [ ] **Step 5: Return from AddressList to Checkout**

In `AddressListFragment`, store:

```java
private long checkoutCartId = -1L;
private long checkoutRestaurantId = -1L;
```

Read them in `onViewCreated`:

```java
checkoutCartId = getArguments() == null ? -1L : getArguments().getLong("cart_id", -1L);
checkoutRestaurantId = getArguments() == null ? -1L : getArguments().getLong("restaurant_id", -1L);
```

Change address click:

```java
adapter.setListener(item -> {
    if ("home".equals(source)) {
        repository.select(item.getId());
        Navigation.findNavController(requireView()).popBackStack(R.id.homeFragment, false);
    } else if ("checkout".equals(source)) {
        repository.select(item.getId());
        returnToCheckout();
    } else {
        openForm(item.getId(), null);
    }
});
```

Add:

```java
private void returnToCheckout() {
    Intent intent = new Intent(requireContext(), Checkout.class);
    intent.putExtra("cart_id", checkoutCartId);
    intent.putExtra("restaurant_id", checkoutRestaurantId);
    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    startActivity(intent);
}
```

Import `android.content.Intent` and `com.example.fooddelivery.ui.cart.Checkout`.

- [ ] **Step 6: Preserve checkout context when adding/editing an address**

In `AddressListFragment.openForm`, include:

```java
args.putLong("cart_id", checkoutCartId);
args.putLong("restaurant_id", checkoutRestaurantId);
```

In `DeliveryAddressFormFragment`, add fields:

```java
private String source = "profile";
private long checkoutCartId = -1L;
private long checkoutRestaurantId = -1L;
```

Read them from args in `onViewCreated`:

```java
source = getArguments() == null ? "profile" : getArguments().getString("source", "profile");
checkoutCartId = getArguments() == null ? -1L : getArguments().getLong("cart_id", -1L);
checkoutRestaurantId = getArguments() == null ? -1L : getArguments().getLong("restaurant_id", -1L);
```

After successful save, call:

```java
if ("checkout".equals(source)) {
    Intent intent = new Intent(requireContext(), Checkout.class);
    intent.putExtra("cart_id", checkoutCartId);
    intent.putExtra("restaurant_id", checkoutRestaurantId);
    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    startActivity(intent);
    return;
}
Navigation.findNavController(requireView()).navigateUp();
```

Import `android.content.Intent` and `com.example.fooddelivery.ui.cart.Checkout`.

- [ ] **Step 7: Run tests and compile**

Run:

```powershell
$env:ANDROID_HOME='C:\Users\Admin\AppData\Local\Android\Sdk'; .\gradlew.bat :app:testDebugUnitTest --tests "com.example.fooddelivery.CartUiFlowRegressionTest"
$env:ANDROID_HOME='C:\Users\Admin\AppData\Local\Android\Sdk'; .\gradlew.bat :app:compileDebugJavaWithJavac
```

Expected: both commands PASS.

---

### Task 3: Order Tab Verification And Final Regression

**Files:**
- Inspect: `app/src/main/java/com/example/fooddelivery/ui/order/OrderListFragment.java`
- Test: `app/src/test/java/com/example/fooddelivery/BugRegressionTest.java`
- Test: `app/src/test/java/com/example/fooddelivery/CartUiFlowRegressionTest.java`

**Interfaces:**
- Consumes: `OrderRepository.getDraftCartsV3()`, `OrderRepository.getMyOrdersV3(String status)`
- Produces: verified behavior that `completed` and `cancelled` tabs remain DB-backed.

- [ ] **Step 1: Add or keep order tab regression assertions**

Add a source-level regression test because `OrderListFragment` does not currently expose an injectable repository:

```java
@Test
public void orderTabsUseRpcStatusesForCompletedAndCancelled() throws Exception {
    String source = new String(
            java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(
                    "app/src/main/java/com/example/fooddelivery/ui/order/OrderListFragment.java")),
            java.nio.charset.StandardCharsets.UTF_8
    );

    assertTrue(source.contains("orderRepository.getDraftCartsV3()"));
    assertTrue(source.contains("orderRepository.getMyOrdersV3(rpcStatus)"));
    assertTrue(source.contains("String rpcStatus = \"processing\".equals(tabStatus) ? null : tabStatus;"));
}
```

- [ ] **Step 2: Run all unit tests**

Run:

```powershell
$env:ANDROID_HOME='C:\Users\Admin\AppData\Local\Android\Sdk'; .\gradlew.bat :app:testDebugUnitTest
```

Expected: PASS.

- [ ] **Step 3: Compile debug Java**

Run:

```powershell
$env:ANDROID_HOME='C:\Users\Admin\AppData\Local\Android\Sdk'; .\gradlew.bat :app:compileDebugJavaWithJavac
```

Expected: PASS.

- [ ] **Step 4: Report realism gaps**

Final report must mention these still-open realistic-app gaps:

- voucher selection is not real yet;
- schedule delivery is not real yet;
- gift flow is not real yet;
- checkout recommendations are placeholders;
- reorder still needs real order-item to cart behavior;
- fee display is only as realistic as the backend summary values.
