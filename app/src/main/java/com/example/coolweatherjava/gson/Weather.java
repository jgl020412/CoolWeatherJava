package com.example.coolweatherjava.gson;

import com.google.gson.annotations.SerializedName;

public class Weather {
    @SerializedName("code")
    public String code;
    @SerializedName("updateTime")
    public String updateTime;
    @SerializedName("fxLink")
    public String fxLink;
    @SerializedName("now")
    public Now now;
    @SerializedName("refer")
    public Refer refer;
}
