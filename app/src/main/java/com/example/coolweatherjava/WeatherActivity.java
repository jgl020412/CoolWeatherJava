package com.example.coolweatherjava;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.coolweatherjava.gson.Daily;
import com.example.coolweatherjava.gson.Forecast;
import com.example.coolweatherjava.gson.Now;
import com.example.coolweatherjava.gson.Weather;
import com.example.coolweatherjava.util.HttpUtil;
import com.example.coolweatherjava.util.Utility;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private static final String TAG = "WeatherActivity";
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView weatherObsTime;
    private ImageView weatherIcon;
    private TextView weatherInfoText;
    private TextView degreeText;
    private LinearLayout forecastLayout;
    private TextView windDir;
    private TextView windDirAngle;
    private TextView windScale;
    private TextView windSpeed;
    private TextView cloud;
    private TextView visibility;
    private TextView precipitation;
    private TextView humidity;
    private LinearLayout weather;
    public SwipeRefreshLayout swipeRefreshLayout;
    private String key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: 成功跳转到天气页面");
        setContentView(R.layout.activity_weather);
        // 初始化各个控件
        titleCity = (TextView) findViewById(R.id.title_city);
        titleUpdateTime = (TextView) findViewById(R.id.title_update_time);
        weatherObsTime = (TextView) findViewById(R.id.weather_obsTime);
        weatherIcon = (ImageView) findViewById(R.id.weather_icon);
        weatherInfoText = (TextView) findViewById(R.id.weather_info_text);
        degreeText = (TextView) findViewById(R.id.degree_text);
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        windDir = (TextView) findViewById(R.id.wind_dir);
        windDirAngle = (TextView) findViewById(R.id.wind_dir_angle);
        windScale = (TextView) findViewById(R.id.wind_scale);
        windSpeed = (TextView) findViewById(R.id.wind_speed);
        cloud = (TextView) findViewById(R.id.cloud);
        visibility = (TextView) findViewById(R.id.visibility);
        precipitation = (TextView) findViewById(R.id.precipitation);
        humidity = (TextView) findViewById(R.id.humidity);
        weather = (LinearLayout) findViewById(R.id.weather);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));
        Log.d(TAG, "onCreate: 各个组件初始化成功");
        key = Utility.getConfig(this, "weather.key");

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String locationString = prefs.getString("location", null);
        if (locationString == null) {
            locationString = getIntent().getStringExtra("location");
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("location", locationString);
            editor.apply();
        }
        titleCity.setText(locationString);

        final String weatherId;
        if (prefs.getString("weather_id", null) == null) {
            weatherId = getIntent().getStringExtra("weather_id");
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("weather_id", weatherId);
            editor.apply();
        } else {
            weatherId = prefs.getString("weatherId", null);
        }

        String weatherString = prefs.getString("weather", null);
        if (weatherString != null) {
            // 有缓存直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            showNowWeatherInfo(weather);
        } else {
            // 无缓存时去服务器查询天气
            requestWeather(weatherId);
        }

        String forecastString = prefs.getString("forecast", null);
        if (forecastString != null) {
            Forecast forecast = Utility.handleForecastResponse(forecastString);
            showForecastInfo(forecast);
        } else {
            requestForecast(weatherId);
        }

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(weatherId);
                requestForecast(weatherId);
            }
        });
    }

    /**
     * 根据天气id请求城市实时天气信息
     */
    public void requestWeather(final String weatherId) {
        String weatherUrl = "https://devapi.qweather.com/v7/weather/now?location="
                + weatherId + "&key=" + key;
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Toast.makeText(WeatherActivity.this,
                        "获取天气信息失败",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "200".equals(weather.code)) {
                            SharedPreferences.Editor editor = PreferenceManager
                                    .getDefaultSharedPreferences(WeatherActivity.this)
                                    .edit();
                            editor.putString("weather", responseText);
                            editor.apply();
                            showNowWeatherInfo(weather);
                        } else {
                            Toast.makeText(WeatherActivity.this,
                                    "获取天气信息失败",
                                    Toast.LENGTH_SHORT).show();
                        }
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        });
    }

    /**
     * 根据天气id请求城市7天的预报情况
     */
    public void requestForecast(final String weatherId) {
        String forecastUrl = "https://devapi.qweather.com/v7/weather/7d?location="
                + weatherId + "&key=" + key;
        HttpUtil.sendOkHttpRequest(forecastUrl, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Toast.makeText(WeatherActivity.this,
                        "预报天气获取失败",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseText = response.body().string();
                Forecast forecast = Utility.handleForecastResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SharedPreferences.Editor editor = PreferenceManager
                                .getDefaultSharedPreferences(WeatherActivity.this)
                                .edit();
                        editor.putString("forecast", responseText);
                        editor.apply();
                        showForecastInfo(forecast);
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        });
    }

    /**
     * 处理并展示Weather实体类中的数据
     */
    private void showNowWeatherInfo(Weather weather) {
        String updateTime = weather.updateTime;
        Now now = weather.now;
        String obsTime = now.observeTime;
        String iconCode = now.iconCode;
        String weatherInfo = now.text;
        String temperature = now.temperature;
        String windDir = now.windDirection;
        String windDirAngle = now.windAngle;
        String windScale = now.windScale;
        String windSpeed = now.windSpeed;
        String humidity = now.humidity;
        String precipitation = now.precipitation;
        String visibility = now.visibility;
        String cloud = now.cloud;
        titleUpdateTime.setText(Utility.changeDate(updateTime, "MM月dd日 HH:mm"));
        weatherObsTime.setText(Utility.changeDate(obsTime, "yyyy年MM月dd日"));
        String fileName = "ic_" + iconCode;
        int resourceId = Utility.getResourceIdByName(WeatherActivity.this, fileName, "drawable");
        weatherIcon.setImageResource(resourceId);
        Log.d(TAG, "showNowWeatherInfo: " + resourceId + "---" + fileName);
        weatherInfoText.setText(weatherInfo);
        degreeText.setText(temperature + "\u2103");
        this.windDir.setText("风向：" + windDir);
        this.windScale.setText("风等级：" + windScale);
        this.windSpeed.setText("风速：" + windSpeed + "km/h");
        this.windDirAngle.setText("风角度：" + windDirAngle + "\u00B0");
        this.humidity.setText("湿度：" + humidity + "%");
        this.precipitation.setText("降水量：" + precipitation + "mm");
        this.visibility.setText("能见度：" + visibility + "km");
        this.cloud.setText("云量：" + cloud + "%");
        loadBingPic();
    }

    /**
     * 处理并展示forecast实体类的数据
     */
    private void showForecastInfo(Forecast forecast) {
        List<Daily> dailies = forecast.dailies;
        for (Daily daily : dailies) {
            View view = LayoutInflater
                    .from(this)
                    .inflate(R.layout.forecast_item, forecastLayout, false);
            TextView forecastDate = (TextView) view.findViewById(R.id.forecast_date_text);
            TextView forecastInfo = (TextView) view.findViewById(R.id.forecast_info_text);
            TextView forecastDegree = (TextView) view.findViewById(R.id.forecast_degree_text);
            forecastDate.setText(daily.forecastDate);
            forecastInfo.setText(daily.weather);
            forecastDegree.setText(daily.minTemperature + "\u2103~" + daily.maxTemperature + "\u2103");
            forecastLayout.addView(view);
        }
    }

    /**
     * 加载必应每日一图
     */
    private void loadBingPic() {
        String requestBingPic = "https://www.bing.com/HPImageArchive.aspx?format=js&idx=0&n=1";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Toast.makeText(WeatherActivity.this, "加载图片失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String bingPic = response.body().string();
                String imageUrl = Utility.handleBingPicResponseForImageURL(bingPic);
                SharedPreferences.Editor editor = PreferenceManager
                        .getDefaultSharedPreferences(WeatherActivity.this)
                        .edit();
                editor.putString("bing_pic", imageUrl);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "run: 设置必应图片" + imageUrl);
                        Glide.with(getApplicationContext())
                                .load(imageUrl)
                                .into(new SimpleTarget<Drawable>() {
                                    @Override
                                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                        weather.setBackground(resource);
                                    }
                                });
                    }
                });
            }
        });
    }
}