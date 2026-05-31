package com.example.fooddelivery;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

// SupabaseClient.java
public class SupabaseClient {
   private static final String BASE_URL = "https://eiioaiyxlsfpoptmsbsm.supabase.co/";
   private static final String API_KEY= "sb_publishable_Kq2KFW_2KRjJjOv406Y-iQ_UZDv-QIk";

   private static Retrofit retrofit;

   public static Retrofit getInstance(){
        if(retrofit == null){
            OkHttpClient client = new OkHttpClient.Builder().addInterceptor(chain -> {
                Request request = chain.request().newBuilder()
                        .addHeader("apikey" , API_KEY)
                        .addHeader("Authorization", "Bearer" + API_KEY)
                        .addHeader("Content-Type", "application/json")
                        .build();
                return chain.proceed(request);
            }).build();
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
   }
}