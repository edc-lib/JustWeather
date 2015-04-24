package com.edward.justweather.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.edward.justweather.R;
import com.edward.justweather.db.JustWeatherDB;
import com.edward.justweather.model.City;
import com.edward.justweather.model.County;
import com.edward.justweather.model.Province;
import com.edward.justweather.util.HttpCallbackListener;
import com.edward.justweather.util.HttpUtil;
import com.edward.justweather.util.Utility;

import java.util.ArrayList;
import java.util.List;

public class ChooseAreaActivity extends ActionBarActivity {

    private boolean isFromWeatherActivity;

    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    private ProgressDialog progressDialog;
    private TextView titleText;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private JustWeatherDB justWeatherDB;
    private List<String> dataList = new ArrayList<>();

    /*省列表*/
    private List<Province> provinceList;
    /*市列表*/
    private List<City> cityList;
    /*县列表*/
    private List<County> countyList;
    /*选中的省份*/
    private Province selectProvince;
    /*选中的城市*/
    private City selectCity;
    /*当前选中的级别*/
    private int currentLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isFromWeatherActivity = getIntent().getBooleanExtra("is_from_weather_activity",false);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (preferences.getBoolean("select_city",false) && !isFromWeatherActivity){
            Intent intent = new Intent(this,WeatherActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        setContentView(R.layout.choose_area);


        listView = (ListView) findViewById(R.id.list_view);
        titleText = (TextView) findViewById(R.id.title_text);

        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);

        justWeatherDB = JustWeatherDB.getInstance(this);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel == LEVEL_PROVINCE){
                    selectProvince = provinceList.get(position);
                    queryCities();
                } else if (currentLevel == LEVEL_CITY){
                    selectCity = cityList.get(position);
                    queryCounties();
                } else if (currentLevel == LEVEL_COUNTY){
                    String countyCode = countyList.get(position).getCountyCode();
                    Intent intent = new Intent(ChooseAreaActivity.this,WeatherActivity.class);
                    intent.putExtra("cpunty_code",countyCode);
                    startActivity(intent);
                    finish();
                }
            }
        });

        queryProvinces();
    }

    private void queryProvinces() {
        provinceList = justWeatherDB.loadProvinces();
        if (provinceList.size() > 0){
            dataList.clear();
            for (Province province : provinceList){
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText("中国");
            currentLevel = LEVEL_PROVINCE;
        } else {
            queryFromServer(null, "province");
        }
    }

    private void queryCities() {
        cityList = justWeatherDB.loadCities(selectProvince.getId());
        if (cityList.size() > 0){
            dataList.clear();
            for (City city:cityList){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText(selectProvince.getProvinceName());
            currentLevel = LEVEL_CITY;
        } else {
            queryFromServer(selectProvince.getProvinceCode(),"city");
        }
    }

    private void queryCounties() {
        countyList = justWeatherDB.loadCounties(selectCity.getId());
        if (countyList.size() > 0 ){
            dataList.clear();
            for (County county : countyList){
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText(selectCity.getCityName());
            currentLevel = LEVEL_COUNTY;
        }else {
            queryFromServer(selectCity.getCityCode(),"county");
        }
    }

    private void queryFromServer(final String code, final String type) {
        String address;
        if (!TextUtils.isEmpty(code)){
            address = "";
        } else {
            address = "";
        }
        showProgressDialog();

        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                boolean result = false;
                if ("province".equals(type)) {
                    result = Utility.handleProvincesResponse(justWeatherDB, response);
                } else if ("city".equals(type)) {
                    result = Utility.handleCitiesResponse(justWeatherDB, response, selectProvince.getId());
                } else if ("county".equals(type)) {
                    result = Utility.handleCountyResponse(justWeatherDB, response, selectCity.getId());
                }

                if (result) {
                    //通过runOnUiThread()方法回到主线程处理逻辑
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)) {
                                queryProvinces();
                            } else if ("city".equals(type)) {
                                queryCities();
                            } else if ("county".equals(type)) {
                                queryCounties();
                            }
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(ChooseAreaActivity.this, "加载失败", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

    }

    private void closeProgressDialog() {
        if (progressDialog == null){
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("正在加载……");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    @Override
    public void onBackPressed() {
        if (currentLevel == LEVEL_COUNTY){
            queryCities();
        } else if (currentLevel == LEVEL_CITY){
            queryProvinces();
        } else {
            if (isFromWeatherActivity){
                Intent intent = new Intent(this,WeatherActivity.class);
                startActivity(intent);
            }
            finish();
        }
    }

    private void showProgressDialog() {
        if (progressDialog != null){
            progressDialog.dismiss();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_choose_area, menu);
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
