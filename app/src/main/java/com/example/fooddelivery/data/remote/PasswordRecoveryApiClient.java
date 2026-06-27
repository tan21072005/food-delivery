package com.example.fooddelivery.data.remote;

import com.example.fooddelivery.data.remote.apis.PasswordRecoveryApiService;
import com.example.fooddelivery.utils.Constants;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class PasswordRecoveryApiClient {
    private PasswordRecoveryApiClient() {
    }

    public static PasswordRecoveryApiService create() {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request request = chain.request().newBuilder()
                            .header("apikey", Constants.SUPABASE_ANON_KEY)
                            .header("Content-Type", "application/json")
                            .build();
                    return chain.proceed(request);
                })
                .build();

        return new Retrofit.Builder()
                .baseUrl(Constants.SUPABASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(PasswordRecoveryApiService.class);
    }
}
