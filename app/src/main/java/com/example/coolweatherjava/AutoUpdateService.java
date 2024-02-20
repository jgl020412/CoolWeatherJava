package com.example.coolweatherjava;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Presentation;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.coolweatherjava.gson.Forecast;
import com.example.coolweatherjava.gson.Weather;
import com.example.coolweatherjava.util.HttpUtil;
import com.example.coolweatherjava.util.Utility;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateService extends Service {

    private static final String TAG = "AutoUpdateService";

    private SharedPreferences prefs;
    private String weatherId;
    private String key;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        weatherId = prefs.getString("weather_id", null);
        InputStream inputStream = getResources().openRawResource(R.raw.config);
        Properties properties = new Properties();
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        key = properties.getProperty("weather.key");
        Log.d(TAG, "onStartCommand: " + weatherId);
        updateWeather();
        updateForecast();
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int anHour = 8 * 60 * 60 * 100;
//        int anHour = 6 * 1000;
        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
        Intent i = new Intent(this, AutoUpdateService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, i, PendingIntent.FLAG_IMMUTABLE);
        manager.cancel(pi);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 更新天气信息
     */
    private void updateWeather() {
        String weatherString = prefs.getString("weather", null);
        if (weatherString != null) {
            String weatherURL = "https://devapi.qweather.com/v7/weather/now?location=" + weatherId + "&key=" + key;
            HttpUtil.sendOkHttpRequest(weatherURL, new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String responseText = response.body().string();
                    Weather weather = Utility.handleWeatherResponse(responseText);
                    if (weather != null && "200".equals(weather.code)) {
                        SharedPreferences.Editor editor = prefs.edit();
                        Log.d(TAG, "onResponse: " + responseText);
                        editor.putString("weather", responseText);
                        editor.apply();
                    }
                }
            });
        }
    }

    /**
     * 更新预报天气
     */
    private void updateForecast() {
        String forecastString = prefs.getString("forecast", null);
        if (forecastString != null) {
            String forecastURL = "https://devapi.qweather.com/v7/weather/7d?location=" + weatherId + "&key=" + key;
            HttpUtil.sendOkHttpRequest(forecastURL, new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String responseText = response.body().string();
                    Forecast forecast = Utility.handleForecastResponse(responseText);
                    if (forecast != null && "200".equals(forecast.code)) {
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("forecast", responseText);
                        editor.apply();
                    }
                }
            });
        }
    }

}