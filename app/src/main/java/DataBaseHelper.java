import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.content.Context;
import android.util.Log;

/**
 * Created by Roozbeh on 3/30/2015.
 */
public class DataBaseHelper extends SQLiteOpenHelper
{

    private static final String DATABASE_NAME = "mobitrace.db";
    private static final int DATABASE_VERSION = 1;


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

    public static final String COL_SENT = "is_sent"; //THIS COLUMN IS USED ON ALL TABLES


    public static final String TABLE_TRACES = "traces";
    public static final String TRACE_ID = "trace_id";
    public static final String COL_TIME = "date_time";
    public static final String COL_LOC_X = "location_x";
    public static final String COL_LOC_Y = "location_y";
    public static final String COL_STREET = "street_addr";
    public static final String COL_ACT_ID = "act_id"; //referencing activities.act_id
    public static final String COL_COMP = "compass";
    public static final String COL_ORI_X = "orient_x";
    public static final String COL_ORI_Y = "orient_y";
    public static final String COL_ORI_Z = "orient_z";
    // IS SENT COLUMN SHARED

    public static final String TABLE_WIFI = "wifi";
    public static final String COL_TRACE_ID = "trace_id";
    public static final String COL_MAC = "mac_addr"; //mac address of the access point
    public static final String COL_SSID = "ssid";
    public static final String COL_STR = "strength";
    //IS SENT COLUMN SHARED


    private static final String DATABASE_CREATE_ACTIVITIES =
            "create table " + TABLE_TRACES + " ( " +
                    ACT_ID + " integer primary key, " +
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

    private static final String DATABASE_CREATE_TRACES =
            "create table " + TABLE_ACTIVITIES + " ( " +
                    TRACE_ID + " integer primary key, " +
                    COL_TIME + " timestamp unique not null, " +
                    COL_LOC_X + " text, " +
                    COL_LOC_Y + " text, " +
                    COL_STREET + " text, " +
                    COL_ACT_ID + " integer, " +
                    COL_COMP + " text, " +
                    COL_ORI_X + " text, " +
                    COL_ORI_Y + " text, " +
                    COL_ORI_Z + " text, " +
                    COL_SENT + " boolean, " +
                    "FOREIGN KEY("+COL_ACT_ID+") REFERENCES "+TABLE_ACTIVITIES+"("+ACT_ID+") ON DELETE CASCADE" +
                    "); ";


    private static final String DATABASE_CREATE_WIFI =
            "create table " + TABLE_WIFI + " ( " +
                    COL_TRACE_ID + " integer, " +
                    COL_MAC + " text not null, " +
                    COL_SSID + " text, " +
                    COL_STR + " text, " +
                    COL_SENT + "boolean, " +
                    "FOREIGN KEY("+COL_TRACE_ID +") REFERENCES "+TABLE_TRACES+"("+TRACE_ID+") ON DELETE CASCADE, " +
                    "PRIMARY KEY("+COL_TRACE_ID+","+COL_MAC+") " +
                    "); ";

    public DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE_ACTIVITIES);
        db.execSQL(DATABASE_CREATE_TRACES);
        db.execSQL(DATABASE_CREATE_WIFI);
        db.execSQL("PRAGMA foreign_keys = ON;");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(DataBaseHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WIFI);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRACES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACTIVITIES);
        onCreate(db);
    }
}
