package com.uf.nomad.mobitrace;

/**
 * Created by Babak on 3/30/2015.
 */
public final class Constants {
    public static final int SUCCESS_RESULT = 0;
    public static final int FAILURE_RESULT = 1;
    public static final String PACKAGE_NAME = "com.uf.nomad.mobitrace";
    public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
    public static final String RESULT_DATA_KEY = PACKAGE_NAME + ".RESULT_DATA_KEY";
    public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME +".LOCATION_DATA_EXTRA";


    /**
     * Detection interval in seconds
     */
    public static final int DETECTION_INTERVAL_SECONDS = 10;

    public static final int MILLISECONDS_PER_SECOND = 1000;

    /**
     * Detection interval in milliseconds (derived from detection_interval_seconds)
     */
    public static final int DETECTION_INTERVAL_MILLISECONDS =
            MILLISECONDS_PER_SECOND * DETECTION_INTERVAL_SECONDS;
}