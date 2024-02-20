package com.example.coolweatherjava.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class BingPic {
    @SerializedName("images")
    public List<Image> imageList;
}
