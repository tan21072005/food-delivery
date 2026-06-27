package com.example.fooddelivery.di;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import com.example.fooddelivery.data.local.prefs.SessionManager;
import com.example.fooddelivery.utils.Constants;
import com.example.fooddelivery.data.remote.apis.AuthApiService;
import com.example.fooddelivery.data.remote.apis.ApiService;

public class AppModule {

    public ExecutorService provideExecutorService() {
        return Executors.newFixedThreadPool(4); // Or newCachedThreadPool() depending on needs
    }

    public Retrofit provideRetrofit(SessionManager sessionManager) {
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(chain -> {
            String authToken = sessionManager.getBearerToken();
            String authorizationHeader = (authToken != null) ? authToken : "Bearer " + Constants.SUPABASE_ANON_KEY;

            Request.Builder requestBuilder = chain.request().newBuilder()
                    .addHeader("apikey", Constants.SUPABASE_ANON_KEY)
                    .addHeader("Authorization", authorizationHeader);

            if (chain.request().header("Content-Type") == null && !chain.request().url().encodedPath().contains("/storage/v1/object/")) {
                requestBuilder.addHeader("Content-Type", "application/json");
            }
            
            return chain.proceed(requestBuilder.build());
        }).build();
        
        return new Retrofit.Builder()
                .baseUrl(Constants.SUPABASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public AuthApiService provideAuthApiService(Retrofit retrofit) {
        return retrofit.create(AuthApiService.class);
    }

    public ApiService provideApiService(Retrofit retrofit) {
        return retrofit.create(ApiService.class);
    }
}
