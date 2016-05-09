package com.uf.nomad.mobitrace;

import android.util.Log;

import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Babak on 3/30/2015.
 */
public final class Constants {

    public static final String APPTAG = "com.uf.nomad.mobitrace";

    public static final int MILLISECONDS_PER_SECOND = 1000;
    public static final int SUCCESS_RESULT = 0;
    public static final int FAILURE_RESULT = 1;
    public static final String PACKAGE_NAME = "com.uf.nomad.mobitrace";
    public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
    public static final String RESULT_DATA_KEY = PACKAGE_NAME + ".RESULT_DATA_KEY";
    public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME + ".LOCATION_DATA_EXTRA";


    /**
     * Activity Detection interval in seconds
     */
    public static final int DETECTION_INTERVAL_SECONDS = 10;

    /**
     * Activity Detection interval in milliseconds (derived from detection_interval_seconds)
     */
    public static final int DETECTION_INTERVAL_MILLISECONDS =
            MILLISECONDS_PER_SECOND * DETECTION_INTERVAL_SECONDS;

    /**
     * Location update request frequency
     */
    public static final int LOCATION_INTERVAL_SECONDS = 30;
    public static final int LOCATION_INTERVAL_MILLISECONDS =
            MILLISECONDS_PER_SECOND * LOCATION_INTERVAL_SECONDS;


    /**
     * Wifi scanning interval
     */
    public static final int WIFI_INTERVAL_SECONDS = 60;
    public static final int WIFI_INTERVAL_MILLISECONDS =
            MILLISECONDS_PER_SECOND * WIFI_INTERVAL_SECONDS;
    /**
     * Custom intent action
     */
    public static final String BROADCAST_ACTION =
            "com.uf.nomad.mobitrace.BROADCAST";
    // Defines the key for the status "extra" in an Intent
    public static final String EXTENDED_DATA_STATUS =
            "com.uf.nomad.mobitrace.STATUS";

    /**
     * Key for accessing WiFi ScanResult
     */
    public static final String LAST_SCANRESULT = "com.uf.nomad.mobitrace.SCANRESULT";

    /**
     * Default delimiter for logging
     */
    public static final String DELIMITER = ";;";

    /**
     * Key used for HomeFragment bundle
     */
    public static final String HomeFragment_BUNDLEKEY = "com.uf.nomad.mobitrace.HomeFragment_BUNDLEKEY";

    /**
     * @return timestamp string with format yyyy-MM-dd HH:mm:ss.SSSZ
     */
    public static String getTimestamp() {
        SimpleDateFormat mDateFormat = null;
        // Get a date formatter, and catch errors in the returned timestamp
        try {
            mDateFormat = (SimpleDateFormat) DateFormat.getDateTimeInstance();
        } catch (Exception e) {
            Log.e(Constants.APPTAG, "Internal error: date formatting exception.");
            return null;
        }
        // Format the timestamp according to the pattern, then localize the pattern
        mDateFormat.applyPattern("yyyy-MM-dd HH:mm:ss.SSSZ");
        mDateFormat.applyLocalizedPattern(mDateFormat.toLocalizedPattern());
        return mDateFormat.format(new Date());
    }

    /**
     *
     * @param source
     * @return SHA-256 of input string
     */
    public static String SHA256(String source) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(source.getBytes("UTF-8"));
            byte[] digest = md.digest();
            return String.format("%064x", new java.math.BigInteger(1, digest));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}