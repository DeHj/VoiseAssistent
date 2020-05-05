package com.example.voiceassistent.Forecast;

import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.util.function.Consumer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForecastToString {
    public static void getForecast(String city, final Consumer<String> callback) {
        ForecastApi api = ForecastService.getApi();
        Call<Forecast> call = api.getCurrentWeather(city);

        call.enqueue(new Callback<Forecast>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onResponse(@NonNull Call<Forecast> call, @NonNull  Response<Forecast> response) {
                Forecast result = response.body();
                if (result.current != null) {
                    callback.accept("сейчас где-то " + result.current.temperature +
                            " градусов и " + result.current.weather_descriptions.get(0));
                }
                else {
                    callback.accept("Не могу узнать погоду");
                }
            }

            @Override
            public void onFailure(@NonNull  Call<Forecast> call, @NonNull Throwable t) {
                Log.w("WEATHER", t.getMessage());
            }
        });
    }

}
