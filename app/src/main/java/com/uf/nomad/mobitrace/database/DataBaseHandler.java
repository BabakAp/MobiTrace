package com.uf.nomad.mobitrace.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.net.wifi.ScanResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Roozbeh on 3/30/2015.
 */
public class DataBaseHandler {

    private SQLiteDatabase database;
    private DataBaseHelper dbHelper;

    public DataBaseHandler(Context context) {
        dbHelper = new DataBaseHelper(context);
    }

    /*
    It instantiates the database with a writable instance unless a condition prevents it from
    being writable. In that case Exception is thrown.
     */
    public void openWritable() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    /*
    Readable object is essentially the same as writable (i.e. it is writable) unless a condition
    prevents it from being writable. In that case a read only db object is returned.
     */
    public void openReadable() throws SQLException {
        database = dbHelper.getReadableDatabase();
    }

    public void close() {
        dbHelper.close();
    }


    public boolean insertCompleteTrace() {
        //TODO: add corresponding records to all three tables
        return true;
    }

    public boolean insertLocationRecord(Location loc, float[] orientations, String timestamp)//MISSING ORIENTATION
    {
        ContentValues values = new ContentValues();
        values.put(DataBaseHelper.COL_TS, timestamp);
        values.put(DataBaseHelper.COL_LOC_X, loc.getLongitude());
        values.put(DataBaseHelper.COL_LOC_Y, loc.getLatitude());
        values.put(DataBaseHelper.COL_ACCU, loc.getAccuracy());
        values.put(DataBaseHelper.COL_SPD, loc.getSpeed());
        values.put(DataBaseHelper.COL_BEAR, loc.getBearing());
        values.put(DataBaseHelper.COL_ORI_X, orientations[0]);
        values.put(DataBaseHelper.COL_ORI_Y, orientations[1]);
        values.put(DataBaseHelper.COL_ORI_Z, orientations[2]);
        values.put(DataBaseHelper.COL_SENT, false);

        //orientation fields are nullified
        long insertId = database.insertWithOnConflict(DataBaseHelper.TABLE_LOCATIONS, null,
                values, SQLiteDatabase.CONFLICT_REPLACE);

        return (insertId != -1);
    }

    public boolean insertActivityRecord(int[] Confidences, String timestamp, int is_manual) {
        ContentValues values = new ContentValues();
        values.put(DataBaseHelper.COL_TS, timestamp);
        values.put(DataBaseHelper.COL_VEHICLE, Confidences[0]);
        values.put(DataBaseHelper.COL_CYCLE, Confidences[1]);
        values.put(DataBaseHelper.COL_FOOT, Confidences[2]);
        values.put(DataBaseHelper.COL_RUNNING, Confidences[3]);
        values.put(DataBaseHelper.COL_STILL, Confidences[4]);
        values.put(DataBaseHelper.COL_TILT, Confidences[5]);
        values.put(DataBaseHelper.COL_UNKNOWN, Confidences[6]);
        values.put(DataBaseHelper.COL_WALK, Confidences[7]);
        values.put(DataBaseHelper.COL_BUS, Confidences[8]);
        values.put(DataBaseHelper.COL_MAN, is_manual);
        values.put(DataBaseHelper.COL_SENT, false);

        long insertId = database.insertWithOnConflict(DataBaseHelper.TABLE_ACTIVITIES, null,
                values, SQLiteDatabase.CONFLICT_REPLACE);

        return (insertId != -1);
    }

    public boolean insertWiFiRecord(ScanResult wifi, String timestamp) {
        ContentValues values = new ContentValues();
        values.put(DataBaseHelper.COL_TS, timestamp);
        values.put(DataBaseHelper.COL_MAC, wifi.BSSID);
        values.put(DataBaseHelper.COL_SSID, wifi.SSID);
        values.put(DataBaseHelper.COL_STR, wifi.level);
        values.put(DataBaseHelper.COL_FREQ, wifi.frequency);
        values.put(DataBaseHelper.COL_SENT, false);

        long insertId = database.insertWithOnConflict(DataBaseHelper.TABLE_WIFI, null,
                values, SQLiteDatabase.CONFLICT_REPLACE);

        return (insertId != -1);
    }


    public void updateAllNotSendToSend() {
        database.execSQL("update " + DataBaseHelper.TABLE_LOCATIONS + " set is_sent = 1 where is_sent = 0 ");
        database.execSQL("update " + DataBaseHelper.TABLE_ACTIVITIES + " set is_sent = 1 where is_sent = 0 ");
        database.execSQL("update " + DataBaseHelper.TABLE_WIFI + " set is_sent = 1 where is_sent = 0 ");
    }

    /*
    REMEMBER TO CLOSE THE CURSOR AFTER USE!
     */
    public Cursor getAllLocations() {
        return database.rawQuery("select * from " + DataBaseHelper.TABLE_LOCATIONS, null);
        // Make sure to close the cursor
        // cursor.close();

    }

    protected Cursor getAllLocationsNotSent() {
        return database.rawQuery("select * from " + DataBaseHelper.TABLE_LOCATIONS + " where " + DataBaseHelper.COL_SENT + " = 0", null);
    }

    protected Cursor getAllWiFiNotSent() {
        return database.rawQuery("select * from " + DataBaseHelper.TABLE_WIFI + " where " + DataBaseHelper.COL_SENT + " = 0", null);
    }

    protected Cursor getAllActivityNotSent() {
        return database.rawQuery("select * from " + DataBaseHelper.TABLE_ACTIVITIES + " where " + DataBaseHelper.COL_SENT + " = 0", null);
    }

    /**
     * @return a list of activities not yet sent to server
     */
    public List<ActivityTuple> getActivityList() {
        Cursor c = getAllActivityNotSent();
        List<ActivityTuple> actList = new ArrayList<>();
        c.moveToFirst();
        while (!c.isAfterLast()) {
            ActivityTuple act = cursorToActivity(c);
//            act.device_id = dev_id;
            actList.add(act);
            c.moveToNext();
        }
        return actList;
    }

    /**
     * @return a list of wifi scans not yet sent to server
     */
    public List<WiFiTuple> getWiFiList() {
        Cursor c = getAllWiFiNotSent();
        List<WiFiTuple> wifiList = new ArrayList<>();
        c.moveToFirst();
        while (!c.isAfterLast()) {
            WiFiTuple wifi = cursorToWiFi(c);
//            wifi.device_id = dev_id;
            wifiList.add(wifi);
            c.moveToNext();
        }
        return wifiList;
    }

    /**
     * @return a list of location updates not yet sent to server
     */
    public List<LocationTuple> getLocationList() {
        Cursor c = getAllLocationsNotSent();
        List<LocationTuple> locationsList = new ArrayList<>();
        c.moveToFirst();
        while (!c.isAfterLast()) {
            LocationTuple location = cursorToLocation(c);
//            location.device_id = dev_id;
            locationsList.add(location);
            c.moveToNext();
        }
        return locationsList;
    }

    private ActivityTuple cursorToActivity(Cursor c) {
        ActivityTuple t = new ActivityTuple();
        //device_id is not assigned
        t.act_id = c.getInt(0);
        t.date_time = c.getString(1);
        t.in_vehicle = c.getInt(2);
        t.on_bicycle = c.getInt(3);
        t.on_foot = c.getInt(4);
        t.running = c.getInt(5);
        t.still = c.getInt(6);
        t.tilting = c.getInt(7);
        t.unknown = c.getInt(8);
        t.walking = c.getInt(9);
        t.in_bus = c.getInt(10);
        t.is_manual = c.getInt(11);
        t.is_sent = c.getInt(12);
        return t;

    }

    private WiFiTuple cursorToWiFi(Cursor c) {
        WiFiTuple t = new WiFiTuple();
        //device_id is not assigned
        t.wifi_id = c.getInt(0);
        t.date_time = c.getString(1);
        t.mac_addr = c.getString(2);
        t.ssid = c.getString(3);
        t.strength = c.getInt(4);
        t.frequency = c.getInt(5);
        t.is_sent = c.getInt(6);
        return t;
    }

    private LocationTuple cursorToLocation(Cursor c) {
        LocationTuple t = new LocationTuple();
        //device_id is not assigned
        t.location_id = c.getInt(0);
        t.date_time = c.getString(1);
        t.location_x = c.getDouble(2);
        t.location_y = c.getDouble(3);
        t.loc_accuracy = c.getFloat(4);
        t.speed = c.getFloat(5);
        t.bearing = c.getFloat(6);
        t.orient_x = c.getString(7);
        t.orient_y = c.getString(8);
        t.orient_z = c.getString(9);
        t.is_sent = c.getInt(10);
        return t;
    }
}