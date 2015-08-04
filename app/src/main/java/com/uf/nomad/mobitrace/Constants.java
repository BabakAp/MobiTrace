package com.uf.nomad.mobitrace;

/**
 * Created by Babak on 3/30/2015.
 */
public final class Constants {

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
    public static final int LOCATION_INTERVAL_SECONDS = 60;
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
}