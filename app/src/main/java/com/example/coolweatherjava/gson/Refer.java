package com.example.coolweatherjava.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Refer {
    @SerializedName("sources")
    public List<String> sources;
    @SerializedName("license")
    public List<String> licenseList;
}
