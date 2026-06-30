package com.example.fooddelivery.data.remote.apis;

import com.example.fooddelivery.data.model.FoodCategory;
import com.example.fooddelivery.data.model.FoodItem;
import com.example.fooddelivery.data.model.AddToCartV3Request;
import com.example.fooddelivery.data.model.CartSummaryV3Request;
import com.example.fooddelivery.data.model.CheckoutCartV3Request;
import com.example.fooddelivery.data.model.ClearCartV3Request;
import com.example.fooddelivery.data.model.CartRequest;
import com.example.fooddelivery.data.model.GetMenuItemDetailV3Request;
import com.example.fooddelivery.data.model.GetOrderDetailV3Request;
import com.example.fooddelivery.data.model.GetMyOrdersV3Request;
import com.example.fooddelivery.data.model.MyOrderV3Response;
import com.example.fooddelivery.data.model.RemoveCartItemV3Request;
import com.example.fooddelivery.data.model.UpdateCartItemQuantityV3Request;
import com.example.fooddelivery.data.model.User;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;

// ApiService.java
public interface ApiService {

    // GET danh sách users
    @GET("rest/v1/users")
    Call<List<User>> getUsers(
            @Query("select") String select  // vd: "id,username,email"
    );

    // GET user theo id
    @GET("rest/v1/users")
    Call<List<User>> getUserById(
            @Query("id") String idFilter    // vd: "eq.5"
    );

    // GET user theo auth_uid
    @GET("rest/v1/users")
    Call<List<User>> getUserByAuthUid(
            @Query("auth_uid") String authUidFilter    // vd: "eq.uuid"
    );

    // GET user theo email
    @GET("rest/v1/users")
    Call<List<User>> getUserByEmail(
            @Query("email") String emailFilter
    );

    // POST tạo user mới
    @POST("rest/v1/users")
    Call<Void> createUser(@Body User user);

    // PATCH cập nhật
    @PATCH("rest/v1/users")
    Call<Void> updateUser(
            @Query("id") String idFilter,
            @Body User user
    );

    // DELETE
    @DELETE("rest/v1/users")
    Call<Void> deleteUser(@Query("id") String idFilter);

    @GET("rest/v1/delivery_addresses")
    Call<List<com.example.fooddelivery.data.model.DeliveryAddress>> getDeliveryAddresses(
            @Query("select") String select,
            @Query("customer_id") String customerIdFilter,
            @Query("deleted_at") String deletedAtFilter,
            @Query("order") String order
    );

    @GET("rest/v1/delivery_addresses")
    Call<List<com.example.fooddelivery.data.model.DeliveryAddress>> getDeliveryAddressById(
            @Query("id") String idFilter,
            @Query("select") String select
    );

    @Headers("Prefer: return=representation")
    @POST("rest/v1/delivery_addresses")
    Call<List<com.example.fooddelivery.data.model.DeliveryAddress>> createDeliveryAddress(
            @Body java.util.Map<String, Object> body
    );

    @Headers("Prefer: return=representation")
    @PATCH("rest/v1/delivery_addresses")
    Call<List<com.example.fooddelivery.data.model.DeliveryAddress>> updateDeliveryAddress(
            @Query("id") String idFilter,
            @Body java.util.Map<String, Object> body
    );

    @Headers("Prefer: return=minimal")
    @PATCH("rest/v1/delivery_addresses")
    Call<Void> updateDeliveryAddressDefaults(
            @Query("customer_id") String customerIdFilter,
            @Query("is_default") String defaultFilter,
            @Body java.util.Map<String, Object> body
    );

    @DELETE("rest/v1/delivery_addresses")
    Call<Void> deleteDeliveryAddress(@Query("id") String idFilter);

    // Lấy danh sách category (bảng categories)
    @GET("rest/v1/cuisines")
    Call<List<FoodCategory>> getCategories(
            @Query("select") String select
    );

    // Lấy danh sách món ăn (bảng menus)
    @GET("rest/v1/menus_compat")
    Call<List<FoodItem>> getMenus(
            @Query("select") String select
    );

    // Lấy danh sách món ăn theo category
    @GET("rest/v1/menus_compat")
    Call<List<FoodItem>> getMenusByCategory(
            @Query("category_id") String categoryIdFilter,
            @Query("select") String select
    );

    @GET("rest/v1/menus_compat")
    Call<List<FoodItem>> getMenusByRestaurant(
            @Query("restaurant_id") String restaurantIdFilter,
            @Query("select") String select
    );

    @GET("rest/v1/menus_compat")
    Call<List<FoodItem>> getMenuItemById(
            @Query("id") String foodIdFilter,
            @Query("select") String select
    );

    // [RPC] Lấy dữ liệu tổng hợp cho màn hình Home
    @POST("rest/v1/rpc/get_home_data_v3")
    Call<com.example.fooddelivery.data.model.HomeDataResponse> getHomeData();

    // --------------------------------------------------------
    // CART & ORDER ENDPOINTS
    // --------------------------------------------------------

    @POST("rest/v1/rpc/add_to_cart_v3")
    Call<Long> addToCartV3(@Body AddToCartV3Request request);

    @POST("rest/v1/rpc/get_draft_carts_v3")
    Call<List<com.example.fooddelivery.data.model.DraftCartV3Response>> getDraftCartsV3(@Body Map<String, Object> emptyBody);

    @POST("rest/v1/rpc/get_cart_summary_v3")
    Call<com.example.fooddelivery.data.model.CartSummaryV3Response> getCartSummaryV3(@Body CartSummaryV3Request request);

    @POST("rest/v1/rpc/checkout_cart_v3")
    Call<Long> checkoutCartV3(@Body CheckoutCartV3Request request);

    @POST("rest/v1/rpc/update_cart_item_quantity_v3")
    Call<Long> updateCartItemQuantityV3(@Body UpdateCartItemQuantityV3Request request);

    @POST("rest/v1/rpc/remove_cart_item_v3")
    Call<Long> removeCartItemV3(@Body RemoveCartItemV3Request request);

    @POST("rest/v1/rpc/clear_cart_v3")
    Call<Long> clearCartV3(@Body ClearCartV3Request request);

    @POST("rest/v1/rpc/get_my_orders_v3")
    Call<List<MyOrderV3Response>> getMyOrdersV3(@Body GetMyOrdersV3Request request);

    @POST("rest/v1/rpc/get_order_detail_v3")
    Call<JsonObject> getOrderDetailV3(@Body GetOrderDetailV3Request request);

    @POST("rest/v1/rpc/get_menu_item_detail_v3")
    Call<JsonObject> getMenuItemDetailV3(@Body GetMenuItemDetailV3Request request);

    @POST("rest/v1/rpc/get_cart_summary_v3")
    Call<com.example.fooddelivery.data.model.CartSummaryResponse> getCartSummary();

    @POST("rest/v1/rpc/checkout_cart_v3")
    Call<List<Long>> checkoutCart(@Body com.example.fooddelivery.data.model.CheckoutRequest request);

    @retrofit2.http.Headers("Prefer: resolution=merge-duplicates,return=minimal")
    @POST("rest/v1/carts?on_conflict=user_id,menu_id")
    Call<Void> addToCart(@Body CartRequest request);

    @PATCH("rest/v1/carts")
    Call<Void> updateCartQuantity(@Query("id") String eqId, @Body com.example.fooddelivery.data.model.CartRequest request);

    @DELETE("rest/v1/carts")
    Call<Void> removeFromCart(@Query("id") String eqId);

    // --------------------------------------------------------
    // SUPABASE STORAGE ENDPOINTS
    // --------------------------------------------------------

    // Upload file lên Supabase Storage
    // Cách gọi: bucket = "avatars", fileName = "user123.jpg"
    @POST("storage/v1/object/{bucket}/{fileName}")
    Call<Void> uploadFile(
            @retrofit2.http.Path("bucket") String bucket,
            @retrofit2.http.Path("fileName") String fileName,
            @Body okhttp3.RequestBody file
    );
}
