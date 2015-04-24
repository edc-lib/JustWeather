package com.edward.justweather.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.edward.justweather.R;
import com.edward.justweather.util.HttpCallbackListener;
import com.edward.justweather.util.HttpUtil;
import com.edward.justweather.util.Utility;

public class WeatherActivity extends ActionBarActivity implements View.OnClickListener {

    private LinearLayout weatherInfoLayout;
    private TextView cityNameText;
    private TextView publishText;
    private TextView weatherDesp;
    private TextView temp1Text;
    private TextView temp2Text;
    private TextView currentDateText;

    private Button switchCity;
    private Button refreshWeather;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

//        初始化控件
        weatherInfoLayout = (LinearLayout)findViewById(R.id.weather_info_layout);
        cityNameText = (TextView)findViewById(R.id.city_name);
        publishText = (TextView)findViewById(R.id.publish_text);
        weatherDesp = (TextView)findViewById(R.id.weather_desp);
        temp1Text = (TextView)findViewById(R.id.temp1);
        temp2Text = (TextView)findViewById(R.id.temp2);
        currentDateText = (TextView)findViewById(R.id.current_data);
        switchCity = (Button)findViewById(R.id.switch_city);
        refreshWeather = (Button)findViewById(R.id.refresh_weather);

        String countyCode = getIntent().getStringExtra("county_code");
        if (!TextUtils.isEmpty(countyCode)){
//            用县级代码查询天气
            publishText.setText("同步中……");
            weatherInfoLayout.setVisibility(View.INVISIBLE);
            cityNameText.setVisibility(View.INVISIBLE);
            queryWeatherCode(countyCode);
        }else {
            showWeather();
        }
    }

    private void showWeather() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        cityNameText.setText(preferences.getString("city_name",""));
        temp1Text.setText(preferences.getString("temp1",""));
        temp2Text.setText(preferences.getString("temp2",""));
        weatherDesp.setText(preferences.getString("weather_desp",""));
        publishText.setText("今天" + preferences.getString("publish_time","") + "发布");
        currentDateText.setText(preferences.getString("current_data",""));
        weatherInfoLayout.setVisibility(View.VISIBLE);
        cityNameText.setVisibility(View.VISIBLE);

    }

    private void queryWeatherCode(String countyCode) {
        String address = "" + countyCode + ".xml";
        queryFromServer(address,"countyCode");

    }

    private void queryWeatherInfo(String weatherCode){
        String address = "" + weatherCode + ".html";
        queryFromServer(address,"weatherCode");
    }

    private void queryFromServer(final String address, final String type) {
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                if ("county_code".equals(type)){
                    if (!TextUtils.isEmpty(response)){
                        String[] array = response.split("\\|");
                        if (array != null && array.length == 2){
                            String weatherCode = array[1];
                            queryWeatherInfo(weatherCode);
                        }
                    }
                }else if ("weatherCode".equals(type)){
                    Utility.handleWeatherResponse(WeatherActivity.this,response);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showWeather();
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        publishText.setText("同步失败");
                    }
                });
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.switch_city:
                Intent intent = new Intent(this,ChooseAreaActivity.class);
                intent.putExtra("from_weather_activity", true);
                startActivity(intent);
                finish();
                break;
            case R.id.refresh_weather:
                publishText.setText("同步中……");
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                String weatherCode = preferences.getString("weather_code","");
                if (!TextUtils.isEmpty(weatherCode)){
                    queryWeatherInfo(weatherCode);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_weather, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
