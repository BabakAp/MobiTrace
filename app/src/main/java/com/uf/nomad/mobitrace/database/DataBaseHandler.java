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
        values.put(DataBaseHelper.COL_TS,timestamp);
        values.put(DataBaseHelper.COL_LOC_X,loc.getLongitude());
        values.put(DataBaseHelper.COL_LOC_Y,loc.getLatitude());
        values.put(DataBaseHelper.COL_ACCU,loc.getAccuracy());
        values.put(DataBaseHelper.COL_SPD,loc.getSpeed());
        values.put(DataBaseHelper.COL_BEAR,loc.getBearing());
        values.put(DataBaseHelper.COL_ORI_X,orientations[0]);
        values.put(DataBaseHelper.COL_ORI_Y,orientations[1]);
        values.put(DataBaseHelper.COL_ORI_Z,orientations[2]);
        values.put(DataBaseHelper.COL_SENT,false);

        //orientation fields are nullified
        long insertId = database.insertWithOnConflict(DataBaseHelper.TABLE_LOCATIONS, null,
                values,SQLiteDatabase.CONFLICT_REPLACE);

        return (insertId != -1);
    }

    public boolean insertActivityRecord(int[] Confidences,String timestamp)
    {
        ContentValues values = new ContentValues();
        values.put(DataBaseHelper.COL_TS,timestamp);
        values.put(DataBaseHelper.COL_VEHICLE,Confidences[0]);
        values.put(DataBaseHelper.COL_CYCLE,Confidences[1]);
        values.put(DataBaseHelper.COL_FOOT,Confidences[2]);
        values.put(DataBaseHelper.COL_RUNNING,Confidences[3]);
        values.put(DataBaseHelper.COL_STILL,Confidences[4]);
        values.put(DataBaseHelper.COL_TILT,Confidences[5]);
        values.put(DataBaseHelper.COL_UNKNOWN,Confidences[6]);
        values.put(DataBaseHelper.COL_WALK,Confidences[7]);
        values.put(DataBaseHelper.COL_SENT,false);

        long insertId = database.insertWithOnConflict(DataBaseHelper.TABLE_ACTIVITIES, null,
                values,SQLiteDatabase.CONFLICT_REPLACE);

        return (insertId != -1);
    }

    public boolean insertWiFiRecord(ScanResult wifi, String timestamp)
    {
        ContentValues values = new ContentValues();
        values.put(DataBaseHelper.COL_TS,timestamp);
        values.put(DataBaseHelper.COL_MAC,wifi.BSSID);
        values.put(DataBaseHelper.COL_SSID,wifi.SSID);
        values.put(DataBaseHelper.COL_STR,wifi.level);
        values.put(DataBaseHelper.COL_FREQ,wifi.frequency);
        values.put(DataBaseHelper.COL_SENT,false);

        //orientation fields are nullified
        long insertId = database.insertWithOnConflict(DataBaseHelper.TABLE_WIFI, null,
                values,SQLiteDatabase.CONFLICT_REPLACE);

        return (insertId != -1);
    }

    /*
    REMEMBER TO CLOSE THE CURSOR AFTER USE!
     */
    public Cursor getAllLocations() {
        return database.rawQuery("select * from "+DataBaseHelper.TABLE_LOCATIONS,null);

        /*
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {

            cursor.moveToNext();
        }
        */
        // Make sure to close the cursor
       // cursor.close();

    }

    public List<LocationTuple>  getLocationListFromCursor(Cursor c, String dev_id)
    {
        List<LocationTuple> locationsList = new ArrayList<LocationTuple>();
        c.moveToFirst();
        while (!c.isAfterLast()) {
            LocationTuple location = cursorToLocation(c);
            location.device_id = dev_id;
            locationsList.add(location);
            c.moveToNext();
        }
        return locationsList;
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
        t.is_sent =  c.getInt(10);
        return t;
    }
}


/*
SAMPLES
------------------------

  public List<Program> getAllPrograms() {
	    List<Program> Programs = new ArrayList<Program>();

	    Cursor cursor = database.query(MySQLiteHelper.TABLE_Programs,
	        allColumns_programs, null, null, null, null, null);

	    cursor.moveToFirst();
	    while (!cursor.isAfterLast()) {
	      Program Program = cursorToProgram(cursor);
	      Programs.add(Program);
	      cursor.moveToNext();
	    }
	    // Make sure to close the cursor
	    cursor.close();
	    return Programs;
	  }

  	private Program cursorToProgram(Cursor cursor) {
  	    Program pr = new Program();
	    pr.ID = cursor.getLong(0);
	    pr.Name = cursor.getString(1);
	    pr.Duration = cursor.getString(2);
	    pr.DateTime = cursor.getString(3);
	    pr.Icon = cursor.getString(4);
	    pr.Type = cursor.getLong(5);
	    return pr;
  	}
 */