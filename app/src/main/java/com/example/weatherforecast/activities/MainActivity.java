package com.example.weatherforecast.activities;

import static com.google.android.gms.common.GooglePlayServicesUtil.isGooglePlayServicesAvailable;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.weatherforecast.databinding.ActivityMainBinding;
import com.example.weatherforecast.network.Network;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.OkHttp;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    String currentLocation;

    ImageView mWeatherImageView;

    final public String TAG = "IVANNIA DEBUGGING";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mWeatherImageView = binding.currentImage;

        Intent intent = getIntent();

        currentLocation = intent.getStringExtra("LATITUDE") + "," +
                intent.getStringExtra("LONGITUD");
        Log.v(TAG, currentLocation);

        final OkHttpClient client = new OkHttpClient();

        final Request request = new Request.Builder()
                .url(Network.openWeatherAPI + "forecast.json?key=" + Network.openWeatherAPIKey
                        + "&q=" + currentLocation + "&days=1&aqi=no&alerts=no")
                .build();

        @SuppressLint("StaticFieldLeak")
        AsyncTask<Void, Void, String> asyncTask = new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    Response response = client.newCall(request).execute();
                    if (!response.isSuccessful()){
                        return null;
                    }
                    return response.body().string();
                } catch (Exception e){
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String s){
                super.onPostExecute(s);
                if(s != null){
                    JSONObject jsonResponse = null;
                    try {
                        jsonResponse = new JSONObject(s);
                        JSONObject locationObject = jsonResponse.getJSONObject("location");
                        binding.locationText2.setText(locationObject.getString("name"));
                        JSONObject currentObject = jsonResponse.getJSONObject("current");
                        binding.currentText.setText(String.valueOf((int) Math.round(currentObject.getDouble("temp_c"))) + "\u2103");
                        String imageUrl = currentObject.getJSONObject("condition").getString("icon");
                        imageUrl = "https:" + imageUrl.replace("64", "128");
                        JSONObject tempObject = jsonResponse.getJSONObject("forecast").getJSONArray("forecastday").getJSONObject(0).getJSONObject("day");
                        binding.minimunText.setText("min: " + String.valueOf((int) Math.round(tempObject.getDouble("mintemp_c"))) + "\u2103");
                        binding.maximumText.setText("max: " + String.valueOf((int) Math.round(tempObject.getDouble("maxtemp_c"))) + "\u2103");
                        Glide.with(MainActivity.this).load(imageUrl).into(mWeatherImageView);
                    } catch (JSONException e){
                        throw new RuntimeException(e);
                    }
                }
            }
        };

        asyncTask.execute();
    }
}