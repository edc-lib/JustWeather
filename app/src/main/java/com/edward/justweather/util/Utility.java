package com.edward.justweather.util;

import android.text.TextUtils;

import com.edward.justweather.db.JustWeatherDB;
import com.edward.justweather.model.City;
import com.edward.justweather.model.County;
import com.edward.justweather.model.Province;

/**
 * Created by Edward on 2015-04-21.
 */
public class Utility {

    /*解析和处理服务器返回的省级数据*/
    public synchronized static boolean handleProvincesResponse(JustWeatherDB justWeatherDB,String response){
        if (!TextUtils.isEmpty(response)) {
            String[] allProvinces = response.split(",");
            if (allProvinces != null && allProvinces.length > 0){
                for (String p : allProvinces){
                    String[] array = p.split("\\|");
                    Province province = new Province();
                    province.setProvinceCode(array[0]);
                    province.setProvinceName(array[1]);
                    //将解析出来的数据存储到province表
                    justWeatherDB.saveProvince(province);
                }
                return true;
            }
        }
        return false;
    }

    /*解析和处理服务器返回的市级数据*/
    public static boolean handleCitiesResponse(JustWeatherDB justWeatherDB,String response,int provinceId){
        if (!TextUtils.isEmpty(response)){
            String[] allCities = response.split(",");
            if (allCities != null && allCities.length > 0){
                for (String c : allCities){
                    String[] array = c.split("\\|");
                    City city = new City();
                    city.setCityCode(array[0]);
                    city.setCityName(array[1]);
                    city.setProvinceId(provinceId);
                    //将解析出来的数据存储到city表
                    justWeatherDB.saveCity(city);
                }
                return true;
            }
        }
        return false;
    }

    /*解析和处理服务器返回的县级数据*/
    public static boolean handleCountyResponse(JustWeatherDB justWeatherDB,String response,int cityId){
        if (!TextUtils.isEmpty(response)){
            String[] allCounties = response.split(",");
            if (allCounties != null && allCounties.length > 0){
                for (String c:allCounties){
                    String[] array = c.split("\\|");
                    County county = new County();
                    county.setCountyCode(array[0]);
                    county.setCountyName(array[1]);
                    county.setCityId(cityId);
                    //将解析出来的数据存储到county表
                    justWeatherDB.saveCounty(county);
                }
                return true;
            }
        }
        return false;
    }
}
