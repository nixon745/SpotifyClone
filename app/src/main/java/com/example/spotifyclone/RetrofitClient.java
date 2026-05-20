package com.example.spotifyclone;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory; // שים לב לייבוא המדויק הזה

public class RetrofitClient {
    private static Retrofit retrofit = null;
    private static final String BASE_URL = "https://itunes.apple.com/";

    public static ITunesApiService getApiService() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(ITunesApiService.class);
    }
}