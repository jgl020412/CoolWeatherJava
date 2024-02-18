package com.example.coolweatherjava.gson;

import com.google.gson.annotations.SerializedName;

public class Now {
    @SerializedName("obsTime")
    public String observeTime;
    @SerializedName("temp")
    public String temperature;
    @SerializedName("feelsLike")
    public String feelsLike;
    @SerializedName("icon")
    public String iconCode;
    @SerializedName("text")
    public String text;
    @SerializedName("wind360")
    public String windAngle;
    @SerializedName("windDir")
    public String windDirection;
    @SerializedName("windScale")
    public String windScale;
    @SerializedName("windSpeed")
    public String windSpeed;
    @SerializedName("humidity")
    public String humidity;
    @SerializedName("precip")
    public String precipitation;
    @SerializedName("pressure")
    public String pressure;
    @SerializedName("vis")
    public String visibility;
    @SerializedName("cloud")
    public String cloud;
    @SerializedName("dew")
    public String dewPointTemperature;
}
