package com.example.voiceassistent.Forecast;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

class ForecastService {
    static ForecastApi getApi() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://api.weatherstack.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit.create(ForecastApi.class);
    }
}