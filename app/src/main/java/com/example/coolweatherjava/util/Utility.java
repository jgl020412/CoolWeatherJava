package com.example.coolweatherjava.util;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;

import com.example.coolweatherjava.R;
import com.example.coolweatherjava.db.City;
import com.example.coolweatherjava.db.County;
import com.example.coolweatherjava.db.Province;
import com.example.coolweatherjava.gson.Forecast;
import com.example.coolweatherjava.gson.Weather;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class Utility {

    /**
     * 解析和处理服务器返回的省级数据
     * @param response JSON数据
     * @return 是否成功
     */
    public static boolean handleProvinceResponse(String response) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allProvinces = new JSONArray(response);
                for (int i = 0; i < allProvinces.length(); i++) {
                    JSONObject provinceObject = allProvinces.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceName(provinceObject.getString("name"));
                    province.setProvinceCode(provinceObject.getInt("id"));
                    province.save();
                }
                return true;
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }

    /**
     * 解析和处理服务器返回的市级数据
     * @param response JSON数据
     * @param provinceId 省级id
     * @return 是否成功
     */
    public static boolean handleCityResponse(String response, int provinceId) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allCities = new JSONArray(response);
                for (int i = 0; i < allCities.length(); i++) {
                    JSONObject cityObject = allCities.getJSONObject(i);
                    City city = new City();
                    city.setCityName(cityObject.getString("name"));
                    city.setCityCode(cityObject.getInt("id"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }

    public static boolean handleCountyResponse(String response, int cityId) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allCounties = new JSONArray(response);
                for (int i = 0; i < allCounties.length(); i++) {
                    JSONObject countyObject = allCounties.getJSONObject(i);
                    County county = new County();
                    county.setCountyName(countyObject.getString("name"));
                    String weatherId = countyObject.getString("weather_id");
                    county.setWeatherId(weatherId.substring(2));
                    county.setCityId(cityId);
                    county.save();
                }
                return true;
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }

    public static Weather handleWeatherResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            return new Gson().fromJson(jsonObject.toString(), Weather.class);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public static Forecast handleForecastResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            return new Gson().fromJson(jsonObject.toString(), Forecast.class);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getConfig(Context context, String str) {
        InputStream inputStream = context.getResources().openRawResource(R.raw.config);
        Properties properties = new Properties();
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String key = properties.getProperty(str);
        return key;
    }

    public static String changeDate(String str, String format) {
        OffsetDateTime offsetDateTime = OffsetDateTime.parse(str);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        String formattedDateTime = formatter.format(offsetDateTime);
        return formattedDateTime;
    }

    /**
     * 根据资源名称和类型获取资源id
     */
    public static int getResourceIdByName(Context context, String resourceName, String resourceType) {
        Resources resources = context.getResources();
        String packageName = context.getPackageName();
        int resourceId = resources.getIdentifier(resourceName, resourceType, packageName);
        return resourceId;
    }

}
