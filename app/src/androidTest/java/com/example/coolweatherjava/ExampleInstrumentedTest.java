package com.example.coolweatherjava;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import com.example.coolweatherjava.gson.Daily;
import com.example.coolweatherjava.gson.Forecast;
import com.example.coolweatherjava.util.HttpUtil;
import com.example.coolweatherjava.util.Utility;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    private static final String TAG = "ExampleInstrumentedTest";

    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.example.coolweatherjava", appContext.getPackageName());
    }

    @Test
    public void getJSONObject() {

        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        InputStream inputStream = context.getResources().openRawResource(R.raw.config);
        Properties properties = new Properties();
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String key = properties.getProperty("weather.key");

        Log.d(TAG, "getJSONObject: " + key);
        
        HttpUtil.sendOkHttpRequest("https://devapi.qweather.com/v7/weather/7d?location=101050106&key=" + key, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "onFailure: " + key);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                Forecast forecast = Utility.handleForecastResponse(response.body().string());
                Log.d(TAG, "onResponse: " + forecast.code);
                Log.d(TAG, "onResponse: " + forecast.fxLink);
                Log.d(TAG, "onResponse: " + forecast.updateTime);
                Log.d(TAG, "onResponse: " + forecast.refer);
                List<Daily> dailies = forecast.dailies;
                for (Daily d : dailies) {
                    Log.d(TAG, "onResponse: " + d.forecastDate);
                    Log.d(TAG, "onResponse: " + d.maxTemperature);
                    Log.d(TAG, "onResponse: " + d.minTemperature);
                    Log.d(TAG, "onResponse: " + d.weather);
                }
            }
        });

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}