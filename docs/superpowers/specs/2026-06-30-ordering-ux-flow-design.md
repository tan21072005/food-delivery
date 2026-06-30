# Ordering UX Flow Design

## Scope

This design fixes the ordering flow so it behaves like a real food delivery app:

- each restaurant has its own draft cart;
- checkout can choose, add, and edit delivery addresses through the full address screen;
- order tabs continue to read real draft carts and orders from Supabase RPCs.

## Restaurant Cart Isolation

Restaurant detail must only show the draft cart that belongs to the current `restaurant_id`.

When the customer opens restaurant A, the sticky cart checks for a draft cart where `draft.restaurant_id == restaurantId`. If no matching cart exists, the sticky cart is hidden and the cart for that restaurant is treated as empty. Draft carts from restaurant B must never appear on restaurant A.

Home can still show an active/global draft-cart entry because the home cart button is a shortcut to the Orders tab and draft carts list.

## Checkout Address Flow

Checkout uses the existing full address management screen instead of a lightweight picker.

When the customer taps the checkout address row or the disabled `Chon dia chi` button, Checkout opens `AddressListFragment` with:

- `source = checkout`;
- current `cart_id`;
- current `restaurant_id`.

In checkout mode, AddressList keeps its normal full functionality: search, select saved address, edit saved address, add home/work/other address, and delete/set default where already supported by the existing screen.

After the customer selects an address, the repository stores the selected address id and the app returns to Checkout with the same `cart_id` and `restaurant_id`. Checkout reloads the current address, parses its numeric id, enables `Dat mon`, and uses that id in `checkout_cart_v3`.

If the customer has no saved address, AddressList shows the existing empty state and add-address actions. After saving a new address, it becomes selected and the customer returns to Checkout.

## Order Tabs And Data Source

The Orders tab remains DB-backed:

- `draft` loads `getDraftCartsV3()`;
- `processing` loads `get_my_orders_v3(null)` and filters active statuses client-side;
- `completed` loads `get_my_orders_v3("completed")`;
- `cancelled` loads `get_my_orders_v3("cancelled")`.

After successful checkout, the app should return to Orders and reload the list.

## Non-Goals

These are known realism gaps but are outside this small fix:

- real voucher selection and promotion application;
- scheduled delivery;
- gifting flow;
- realistic recommended add-ons in checkout;
- real reorder behavior from historical order items;
- dynamic delivery/service fee calculation if backend does not already return it.

## Testing

Add or update regression tests for:

- RestaurantDetail does not display a draft cart from another restaurant;
- RestaurantDetail plus opens food detail instead of quick-adding;
- Home plus remains hidden and the home cart opens draft Orders;
- Checkout address action opens the address flow with checkout source and preserves cart context;
- completed/cancelled tabs continue to use `get_my_orders_v3` with the expected status.
