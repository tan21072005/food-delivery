package com.example.fooddelivery.data.remote.apis;

import com.example.fooddelivery.data.model.FoodCategory;
import com.example.fooddelivery.data.model.FoodItem;
import com.example.fooddelivery.data.model.CartRequest;
import com.example.fooddelivery.data.model.CartQuantityRequest;
import com.example.fooddelivery.data.model.DeliveryAddress;
import com.example.fooddelivery.data.model.DeliveryAddressRequest;
import com.example.fooddelivery.data.model.SetDefaultDeliveryAddressRequest;
import com.example.fooddelivery.data.model.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
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

    // Lấy danh sách category (bảng categories)
    @GET("rest/v1/categories")
    Call<List<FoodCategory>> getCategories(
            @Query("select") String select
    );

    // Lấy danh sách món ăn (bảng menus)
    @GET("rest/v1/menus")
    Call<List<FoodItem>> getMenus(
            @Query("select") String select
    );

    // Lấy danh sách món ăn theo category
    @GET("rest/v1/menus")
    Call<List<FoodItem>> getMenusByCategory(
            @Query("category_id") String categoryIdFilter,
            @Query("select") String select
    );

    // [RPC] Lấy dữ liệu tổng hợp cho màn hình Home
    @POST("rest/v1/rpc/get_home_data")
    Call<com.example.fooddelivery.data.model.HomeDataResponse> getHomeData();

    // --------------------------------------------------------
    // CART & ORDER ENDPOINTS
    // --------------------------------------------------------

    @POST("rest/v1/rpc/get_cart_summary")
    Call<com.example.fooddelivery.data.model.CartSummaryResponse> getCartSummary();

    @POST("rest/v1/rpc/checkout_cart")
    Call<List<Long>> checkoutCart(@Body com.example.fooddelivery.data.model.CheckoutRequest request);

    @retrofit2.http.Headers("Prefer: resolution=merge-duplicates,return=minimal")
    @POST("rest/v1/carts?on_conflict=user_id,menu_id")
    Call<Void> addToCart(@Body CartRequest request);

    @PATCH("rest/v1/carts")
    Call<Void> updateCartQuantity(@Query("id") String eqId, @Body CartQuantityRequest request);

    @DELETE("rest/v1/carts")
    Call<Void> removeFromCart(@Query("id") String eqId);

    @GET("rest/v1/user_addresses")
    Call<List<DeliveryAddress>> getDeliveryAddresses(
            @Query("select") String select,
            @Query("deleted_at") String deletedAtFilter,
            @Query("order") String order
    );

    @POST("rest/v1/user_addresses")
    Call<Void> createDeliveryAddress(@Body DeliveryAddressRequest request);

    @PATCH("rest/v1/user_addresses")
    Call<Void> updateDeliveryAddress(
            @Query("id") String idFilter,
            @Body DeliveryAddressRequest request
    );

    @PATCH("rest/v1/user_addresses")
    Call<Void> softDeleteDeliveryAddress(
            @Query("id") String idFilter,
            @Body DeliveryAddressRequest request
    );

    @POST("rest/v1/rpc/set_default_delivery_address")
    Call<Void> setDefaultDeliveryAddress(@Body SetDefaultDeliveryAddressRequest request);

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
