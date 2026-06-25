package com.example.fooddelivery.data.remote.apis;

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
}