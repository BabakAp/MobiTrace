package com.uf.nomad.mobitrace.activity;

/**
 * Created by Babak on 3/31/2015.
 */
public final class ActivityUtils {

    /*
     * Define a request code to send to Google Play services
     * This code is returned in Activity.onActivityResult
     */
    public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    // Intent actions and extras for sending information from the IntentService to the Activity
    public static final String ACTION_CONNECTION_ERROR =
            "com.uf.nomad.mobitrace.ACTION_CONNECTION_ERROR";

    public static final String ACTION_REFRESH_STATUS_LIST =
            "com.uf.nomad.mobitrace.ACTION_REFRESH_STATUS_LIST";

    public static final String CATEGORY_LOCATION_SERVICES =
            "com.uf.nomad.mobitrace.CATEGORY_LOCATION_SERVICES";

    public static final String EXTRA_CONNECTION_ERROR_CODE =
            "com.uf.nomad.mobitrace.EXTRA_CONNECTION_ERROR_CODE";

    public static final String EXTRA_CONNECTION_ERROR_MESSAGE =
            "com.uf.nomad.mobitrace.EXTRA_CONNECTION_ERROR_MESSAGE";

    // Shared Preferences repository name
    public static final String SHARED_PREFERENCES =
            "com.uf.nomad.mobitrace.SHARED_PREFERENCES";

    // Key in the repository for the previous activity
    public static final String KEY_PREVIOUS_ACTIVITY_TYPE =
            "com.uf.nomad.mobitrace.KEY_PREVIOUS_ACTIVITY_TYPE";
    public static final String KEY_PREVIOUS_ACTIVITY_CONFIDENCE =
            "com.uf.nomad.mobitrace.KEY_PREVIOUS_ACTIVITY_CONFIDENCE";


}

