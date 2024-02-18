package com.example.coolweatherjava.gson;

import com.google.gson.annotations.SerializedName;

public class Daily {
    @SerializedName("fxDate")
    public String forecastDate;
    @SerializedName("tempMax")
    public String maxTemperature;
    @SerializedName("tempMin")
    public String minTemperature;
    @SerializedName("textDay")
    public String weather;
}
