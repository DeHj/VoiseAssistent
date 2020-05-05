package com.example.voiceassistent.Forecast;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

class Forecast implements Serializable { // API Key = 71bc12e78fd263856e8fcd1728471803
    @SerializedName("current")
    @Expose
    Weather current;

    public class Weather implements Serializable {
        @SerializedName("temperature")
        @Expose
        Integer temperature;

        @SerializedName("weather_descriptions")
        @Expose
        List<String> weather_descriptions;
    }
}