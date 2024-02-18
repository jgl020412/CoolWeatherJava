package com.example.coolweatherjava.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Forecast {
    @SerializedName("code")
    public String code;
    @SerializedName("updateTime")
    public String updateTime;
    @SerializedName("fxLink")
    public String fxLink;
    @SerializedName("daily")
    public List<Daily> dailies;
    @SerializedName("refer")
    public Refer refer;

}
