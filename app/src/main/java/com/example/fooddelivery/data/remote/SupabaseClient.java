package com.example.fooddelivery.data.remote;

import android.content.Context;

import com.example.fooddelivery.data.local.prefs.SessionManager;
import com.example.fooddelivery.utils.Constants;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

// SupabaseClient.java
public class SupabaseClient {

   private static Retrofit retrofit;

   public static Retrofit getInstance(Context context){
        if(retrofit == null){
            SessionManager sessionManager = new SessionManager(context);
            
            OkHttpClient client = new OkHttpClient.Builder().addInterceptor(chain -> {
                String authToken = sessionManager.getBearerToken();
                // If the user is logged in, use their JWT token. Otherwise, fallback to the API Key for anonymous access.
                String authorizationHeader = (authToken != null) ? authToken : "Bearer " + Constants.SUPABASE_ANON_KEY;

                Request.Builder requestBuilder = chain.request().newBuilder()
                        .addHeader("apikey", Constants.SUPABASE_ANON_KEY)
                        .addHeader("Authorization", authorizationHeader);

                // Không override Content-Type nếu request đã có (ví dụ: Multipart upload)
                if (chain.request().header("Content-Type") == null && !chain.request().url().encodedPath().contains("/storage/v1/object/")) {
                    requestBuilder.addHeader("Content-Type", "application/json");
                }
                
                return chain.proceed(requestBuilder.build());
            }).build();
            
            retrofit = new Retrofit.Builder()
                    .baseUrl(Constants.SUPABASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
   }
}