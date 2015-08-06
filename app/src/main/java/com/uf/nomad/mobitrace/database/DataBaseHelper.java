package com.uf.nomad.mobitrace.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Roozbeh on 3/30/2015.
 */
public class DataBaseHelper extends SQLiteOpenHelper
{

    private static final String DATABASE_NAME = "mobitrace.db";
    private static final int DATABASE_VERSION = 2;

    //ACTIVITIES TABLE
    public static final String TABLE_ACTIVITIES = "activities";
    public static final String ACT_ID = "act_id";
    public static final String COL_VEHICLE = "in_vehicle";
    public static final String COL_CYCLE = "on_bicycle";
    public static final String COL_FOOT = "on_foot"; //either walk or run
    public static final String COL_RUNNING = "running";
    public static final String COL_STILL = "still";
    public static final String COL_TILT = "tilting";
    public static final String COL_UNKNOWN = "unknown";
    public static final String COL_WALK = "walking";

    //shared columns
    public static final String COL_SENT = "is_sent"; //THIS COLUMN IS USED ON ALL TABLES
    public static final String COL_TS ="date_time";

    //TRACES TABLE
    public static final String TABLE_LOCATIONS = "locations";
    public static final String LOCATION_ID = "location_id";
    public static final String COL_LOC_X = "location_x";
    public static final String COL_LOC_Y = "location_y";
    public static final String COL_ACCU = "loc_accuracy";
    public static final String COL_SPD = "speed";
    public static final String COL_BEAR = "bearing";
    public static final String COL_ORI_X = "orient_x";
    public static final String COL_ORI_Y = "orient_y";
    public static final String COL_ORI_Z = "orient_z";

    //WIFI AP TABLE
    public static final String TABLE_WIFI = "wifi";
    public static final String WIFI_ID = "wifi_id";
    public static final String COL_MAC = "mac_addr"; //mac address of the access point
    public static final String COL_SSID = "ssid";
    public static final String COL_STR = "strength";
    public static final String COL_FREQ = "frequency";
    //IS SENT and TS COLUMN SHARED


    private static final String DATABASE_CREATE_ACTIVITIES =
            "create table " + TABLE_ACTIVITIES + " ( " +
                    ACT_ID + " integer primary key, " +
                    COL_TS + " text unique not null, " +
                    COL_VEHICLE + " integer, " +
                    COL_CYCLE + " integer, " +
                    COL_FOOT + " integer, " +
                    COL_RUNNING + " integer, " +
                    COL_STILL + " integer, " +
                    COL_TILT + " integer, " +
                    COL_UNKNOWN + " integer, " +
                    COL_WALK + " integer, " +
                    COL_SENT + " boolean " +
                    "); ";

    private static final String DATABASE_CREATE_LOCATIONS =
            "create table " + TABLE_LOCATIONS + " ( " +
                    LOCATION_ID + " integer primary key, " +
                    COL_TS + " text unique not null, " +
                    COL_LOC_X + " double, " +
                    COL_LOC_Y + " double, " +
                    COL_ACCU + " float, " +
                    COL_SPD + " float, " +
                    COL_BEAR + " float, " +
                    COL_ORI_X + " text, " +
                    COL_ORI_Y + " text, " +
                    COL_ORI_Z + " text, " +
                    COL_SENT + " boolean " +
                    "); ";


    private static final String DATABASE_CREATE_WIFI =
            "create table " + TABLE_WIFI + " ( " +
                    WIFI_ID + " integer primary key, " +
                    COL_TS + " text unique not null, " +
                    COL_MAC + " text not null, " +
                    COL_SSID + " text, " +
                    COL_STR + " integer, " +
                    COL_FREQ + " integer, " +
                    COL_SENT + "boolean " +
                    "); ";

    public DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE_LOCATIONS);
        db.execSQL(DATABASE_CREATE_WIFI);
        db.execSQL("PRAGMA foreign_keys = TRUE;");

        Log.w(DataBaseHelper.class.getName(),
                "Schema Created Successfully!");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(DataBaseHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WIFI);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACTIVITIES);
        onCreate(db);
    }
}
