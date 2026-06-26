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

                Request request = chain.request().newBuilder()
                        .addHeader("apikey" , Constants.SUPABASE_ANON_KEY)
                        .addHeader("Authorization", authorizationHeader)
                        .addHeader("Content-Type", "application/json")
                        .build();
                return chain.proceed(request);
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