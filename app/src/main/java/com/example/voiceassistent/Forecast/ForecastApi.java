package com.example.voiceassistent.Forecast;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ForecastApi {
    @GET("/current?access_key=71bc12e78fd263856e8fcd1728471803")
    Call<Forecast> getCurrentWeather(@Query("query") String city);
}
