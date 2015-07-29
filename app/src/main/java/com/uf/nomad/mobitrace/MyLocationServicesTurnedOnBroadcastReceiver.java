package com.uf.nomad.mobitrace;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.uf.nomad.mobitrace.android_activity.MainActivity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MyLocationServicesTurnedOnBroadcastReceiver extends BroadcastReceiver {

    private static int location = 0;

    public MyLocationServicesTurnedOnBroadcastReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.location.PROVIDERS_CHANGED")) {
            String text = "";
            ContentResolver contentResolver = context.getContentResolver();
            // Find out what the settings say about which providers are enabled
            int mode = Settings.Secure.getInt(
                    contentResolver, Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_OFF);

            if (mode == Settings.Secure.LOCATION_MODE_OFF) {
                // Location is turned OFF!
                text = "Location services turned off, please turn on location services...";
                /**
                 * Initialize location variable
                 */
                if (location == 0) {
                    location = -1;
                }
                /**
                 * Detect duplicate 'location turned off' broadcasts
                 */
                else if (location == -1) {
                    //return if duplicate
                    return;
                } else {
                    //set to 'off' otherwise
                    location = -1;
                }

                showNotificationLocation(context);
            } else {
                /**
                 * Initialize location variable
                 */
                if (location == 0) {
                    location = 1;
                }
                /**
                 * Detect duplicate 'location turned on' broadcasts
                 */
                else if (location == 1) {
                    //return if duplicate
                    return;
                } else {
                    //set to 'on' otherwise
                    location = 1;
                }
                // Location is turned ON!
                text = "Location services turned on...";
                //Cancel notification
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancelAll();
            }
            /**
             * Do a toast with relevant info
             */
            int duration = Toast.LENGTH_SHORT;
            Toast.makeText(context, text, duration).show();

            //if MainActivity is visible, log info to both display and log file
            if (MainActivity.mThis != null) {
                MainActivity.mThis.logInfo(text);
            }
            //else just log to file
            else {
                File lastFile = getLatestFilefromDir(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS));
                try {
                    BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(lastFile));
                    bufferedWriter.write("\n" + text);
                    bufferedWriter.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    /**
     * @param dir
     * @return last modified file in the given directory
     */
    private File getLatestFilefromDir(File dir) {
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            return null;
        }

        File lastModifiedFile = files[0];
        for (int i = 1; i < files.length; i++) {
            if (lastModifiedFile.lastModified() < files[i].lastModified()) {
                lastModifiedFile = files[i];
            }
        }
        return lastModifiedFile;
    }

    /**
     * Show a notification to turn on location services
     *
     * @param context
     */
    private void showNotificationLocation(Context context) {
        // Set the Intent action to open Location Settings
        Intent gpsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);

        // Create a PendingIntent to start an Activity
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, gpsIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Create a notification builder that's compatible with platforms >= version 4
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context);

        // Set the title, text, and icon
        builder.setContentTitle(context.getString(R.string.app_name))
                .setContentText(context.getString(R.string.turn_on_GPS))
                .setSmallIcon(R.drawable.ic_notification)
                .setAutoCancel(true)
                        // Get the Intent that starts the Location settings panel
                .setContentIntent(pendingIntent);

        // Get an instance of the Notification Manager
        NotificationManager notifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Build the notification and post it
        notifyManager.notify(0, builder.build());
    }
}
