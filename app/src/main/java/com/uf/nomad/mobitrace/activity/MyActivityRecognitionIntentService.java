package com.uf.nomad.mobitrace.activity;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.uf.nomad.mobitrace.R;
import com.uf.nomad.mobitrace.database.DataBaseHandler;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 */
public class MyActivityRecognitionIntentService extends IntentService {

    // Formats the timestamp in the log
    private static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss.SSSZ";

    // Delimits the timestamp from the log info
    private static final String LOG_DELIMITER = ";;";

    // A date formatter
    private SimpleDateFormat mDateFormat;

    // Store the app's shared preferences repository
    private SharedPreferences mPrefs;


    private DataBaseHandler dataBaseHandler;
    private static final String TAG = "activity-intent-service";

    public MyActivityRecognitionIntentService() {
        // Set the label for the service's background thread
        super("ActivityRecognitionIntentService");
    }

    /**
     * Called when a new activity detection update is available.
     */
    @Override
    protected void onHandleIntent(Intent intent) {

        // Get a handle to the repository
        mPrefs = getApplicationContext().getSharedPreferences(
                ActivityUtils.SHARED_PREFERENCES, Context.MODE_PRIVATE);


        // If the intent contains an update
        if (ActivityRecognitionResult.hasResult(intent)) {

            // Get the update
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);



            //TODO: store activities into database
            logActivityRecognitionResult(result);
//            Boolean insertedIntoDB = dataBaseHandler.insertActivityRecord(confidences, getTimestamp());
            Log.i(TAG, "Activity successfully inserted into DB");

            System.out.println("MOST PROBABLE ACTIVITY :: " + result.getMostProbableActivity());
            // Get the most probable activity from the list of activities in the update
            DetectedActivity mostProbableActivity = result.getMostProbableActivity();

            // Get the confidence percentage for the most probable activity
            int confidence = mostProbableActivity.getConfidence();

            // Get the type of activity
            int activityType = mostProbableActivity.getType();

            Editor editor = mPrefs.edit();
            editor.putInt(ActivityUtils.KEY_PREVIOUS_ACTIVITY_TYPE, activityType);
            editor.putInt(ActivityUtils.KEY_PREVIOUS_ACTIVITY_CONFIDENCE, confidence);
            editor.apply();
        }
    }

    /**
     * Get a content Intent for the notification
     *
     * @return A PendingIntent that starts the device's Location Settings panel.
     */
    private PendingIntent getContentIntent() {

        // Set the Intent action to open Location Settings
        Intent gpsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);

        // Create a PendingIntent to start an Activity
        return PendingIntent.getActivity(getApplicationContext(), 0, gpsIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Tests to see if the activity has changed
     *
     * @param currentType The current activity type
     * @return true if the user's current activity is different from the previous most probable
     * activity; otherwise, false.
     */
    private boolean activityChanged(int currentType) {

        // Get the previous type, otherwise return the "unknown" type
        int previousType = mPrefs.getInt(ActivityUtils.KEY_PREVIOUS_ACTIVITY_TYPE,
                DetectedActivity.UNKNOWN);

        // If the previous type isn't the same as the current type, the activity has changed
        // Otherwise, it hasn't.
        return previousType != currentType;
    }

    /**
     * Determine if an activity means that the user is moving.
     *
     * @param type The type of activity the user is doing (see DetectedActivity constants)
     * @return true if the user seems to be moving from one location to another, otherwise false
     */
    private boolean isMoving(int type) {
        switch (type) {
            // These types mean that the user is probably not moving
            case DetectedActivity.STILL:
            case DetectedActivity.TILTING:
            case DetectedActivity.UNKNOWN:
                return false;
            default:
                return true;
        }
    }

    /**
     * Write the activity recognition update to the database
     *
     * @param result The result extracted from the incoming Intent
     */
    private void logActivityRecognitionResult(ActivityRecognitionResult result) {
        // Get all the probably activities from the updated result
        for (DetectedActivity detectedActivity : result.getProbableActivities()) {

            // Get the activity type, confidence level, and human-readable name
            int activityType = detectedActivity.getType();
            int confidence = detectedActivity.getConfidence();
            String activityName = getNameFromType(activityType);

            // Make a timestamp
            if (mDateFormat == null) {
                mDateFormat = new SimpleDateFormat();
            }
            String timeStamp = mDateFormat.format(new Date());

            /**
             * Write each activity to database
             */
            //TODO: INSERT ACTIVITY RECOGNITION DATA INTO DATABASE HERE
            Log.d("ActivityRecognition",
                    timeStamp + LOG_DELIMITER +
                            getString(R.string.log_message, activityType, activityName, confidence)
            );
        }
    }

    /**
     * @return timestamp string with format yyyy-MM-dd HH:mm:ss.SSSZ
     */
    private String getTimestamp() {
        if (mDateFormat == null) {
            // Get a date formatter, and catch errors in the returned timestamp
            try {
                mDateFormat = (SimpleDateFormat) DateFormat.getDateTimeInstance();
            } catch (Exception e) {
                Log.e(ActivityUtils.APPTAG, getString(R.string.date_format_error));
                return null;
            }
            // Format the timestamp according to the pattern, then localize the pattern
            mDateFormat.applyPattern("yyyy-MM-dd HH:mm:ss.SSSZ");
            mDateFormat.applyLocalizedPattern(mDateFormat.toLocalizedPattern());
        }
        return mDateFormat.format(new Date());
    }

    /**
     * Map detected activity types to strings
     *
     * @param activityType The detected activity type
     * @return A user-readable name for the type
     */
    private String getNameFromType(int activityType) {
        switch (activityType) {
            case DetectedActivity.IN_VEHICLE:
                return "in_vehicle";
            case DetectedActivity.ON_BICYCLE:
                return "on_bicycle";
            case DetectedActivity.ON_FOOT:
                return "on_foot";
            case DetectedActivity.STILL:
                return "still";
            case DetectedActivity.UNKNOWN:
                return "unknown";
            case DetectedActivity.TILTING:
                return "tilting";
            case DetectedActivity.WALKING:
                return "walking";
            case DetectedActivity.RUNNING:
                return "running";
        }
        return "unknown";
    }


}
