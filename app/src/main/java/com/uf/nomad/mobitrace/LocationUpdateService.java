package com.uf.nomad.mobitrace;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.uf.nomad.mobitrace.activity.ActivityUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LocationUpdateService extends Service implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
        , LocationListener {

    private GoogleApiClient mGoogleApiClient;

    private LocationRequest mLocationRequestHighAccuracy;
    //    private LocationRequest mLocationRequestBalancedPowerAccuracy;
    private Location mLastLocation;
    private String mLastUpdateTime;
    LocationSettingsRequest.Builder builder;

    private PendingIntent pendingIntent;
    private SimpleDateFormat mDateFormat;

    // Request code to use when launching the resolution activity
    private static final int REQUEST_RESOLVE_ERROR = 1001;

    public LocationUpdateService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocationUpdateService", "Received start id " + startId + ": " + intent);
//        pendingIntent = PendingIntent.getService(getApplicationContext(),0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Override
    public boolean stopService(Intent intent) {
        return super.stopService(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
    }


    @Override
    public IBinder onBind(Intent intent) {
//        throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (mLocationRequestHighAccuracy == null) {
            createLocationRequest();
        }
        /**
         * Notifying user to turn on location services, continue otherwise
         */
        builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequestHighAccuracy);
//                .addLocationRequest(mLocationRequestBalancedPowerAccuracy);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates locationSettingsStates = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        showNotification();
                        break;
                }
            }
        });
        /**
         * Register periodic location request
         */
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequestHighAccuracy, this);
    }

    /**
     * Creates a LocationRequest object using the Constants.LOCATION_INTERVAL_MILLISECONDS interval
     */
    private void createLocationRequest() {
        mLocationRequestHighAccuracy = new LocationRequest();
        mLocationRequestHighAccuracy.setInterval(Constants.LOCATION_INTERVAL_MILLISECONDS);
        mLocationRequestHighAccuracy.setFastestInterval(Constants.LOCATION_INTERVAL_MILLISECONDS / 100);
        mLocationRequestHighAccuracy.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

//        mLocationRequestBalancedPowerAccuracy = new LocationRequest();
//        mLocationRequestBalancedPowerAccuracy.setInterval(Constants.LOCATION_INTERVAL_MILLISECONDS);
//        mLocationRequestBalancedPowerAccuracy.setFastestInterval(Constants.LOCATION_INTERVAL_MILLISECONDS / 100);
//        mLocationRequestBalancedPowerAccuracy.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    @Override
    public void onConnectionSuspended(int i) {
        System.out.println("Connected Suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        System.out.println("Connection failed");
        if (!mGoogleApiClient.isConnecting() &&
                !mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        mLastUpdateTime = getTimestamp();

        if (mLastLocation == null) {
            Log.e("LocationUpdateService", "Cannot retrieve location");
        }
        //TODO: Store last received location into database
        Log.e("LocationUpdateService", "No database selected");
    }

    /**
     * Show a notification while this service is running.
     */
    private void showNotification() {
        // Set the Intent action to open Location Settings
        Intent gpsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);

        // Create a PendingIntent to start an Activity
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, gpsIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Create a notification builder that's compatible with platforms >= version 4
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(getApplicationContext());

        // Set the title, text, and icon
        builder.setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.turn_on_GPS))
                .setSmallIcon(R.drawable.ic_notification)
                .setAutoCancel(true)
                        // Get the Intent that starts the Location settings panel
                .setContentIntent(pendingIntent);

        // Get an instance of the Notification Manager
        NotificationManager notifyManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);

        // Build the notification and post it
        notifyManager.notify(0, builder.build());
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
        String timeStamp = mDateFormat.format(new Date());
        return timeStamp;
    }
}
