package com.edward.justweather.db;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class JustWeatherOpenHelper extends SQLiteOpenHelper {
    /*
    *  Province表建表语句
    * */
    public static final String CREATE_PROVINCE = "creat table Province ("
            + "id integer primary key autoincrement,"
            + "province_name text,"
            + "province_code text)";
    /*
    * City 建表语句
    * */
    public static final String CREATE_CITY = "create table City ("
            + "id integer primary key autoincrement,"
            + "city_name text,"
            + "city_code text,"
            + "province_id integer)";
    /*
    County表建表语句
    */
    public static final String CREATE_COUNTY = "create table County ("
            + "id integer primary key autoincrement,"
            + "county_name text,"
            + "county_code text,"
            + "city_id integer)";

    public JustWeatherOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_PROVINCE);//创见province表
        db.execSQL(CREATE_CITY);//创见city表
        db.execSQL(CREATE_COUNTY);//创见county表

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
