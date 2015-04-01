package com.uf.nomad.mobitrace;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class LocationUpdateService extends Service implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleApiClient mGoogleApiClient;

    private LocationRequest mLocationRequest;
    private Location mLastLocation;

    // Request code to use when launching the resolution activity
    private static final int REQUEST_RESOLVE_ERROR = 1001;

    private NotificationManager mNM;
    private int NOTIFICATION = R.string.location_service_started;

    public LocationUpdateService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Display a notification about us starting.  We put an icon in the status bar.
        showNotification();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(ActivityRecognition.API)
                .build();
        startLocationUpdates();
    }

    /**
     * Starts location updates
     */
    private void startLocationUpdates() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocationUpdateService", "Received start id " + startId + ": " + intent);
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
    }

    //TODO implement this
    private void stopLocationUpdates() {

    }

    @Override
    public IBinder onBind(Intent intent) {
//        throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (mLocationRequest == null) {
            createLocationRequest();
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    /**
     * Creates a LocationRequest object using the Constants.LOCATION_INTERVAL_MILLISECONDS interval
     */
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(Constants.LOCATION_INTERVAL_MILLISECONDS);
        mLocationRequest.setFastestInterval(Constants.LOCATION_INTERVAL_MILLISECONDS / 100);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (!mGoogleApiClient.isConnecting() &&
                !mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        //TODO: Store last received location into database
    }

    /**
     * Show a notification while this service is running.
     */
    private void showNotification() {
        //TODO: fix the code here
        // In this sample, we'll use the same text for the ticker and the expanded notification
//        CharSequence text = getText(R.string.location_service_started);
//
//        // Set the icon, scrolling text and timestamp
//        Notification notification = new Notification(R.drawable.stat_sample, text,
//                System.currentTimeMillis());
//
//        // The PendingIntent to launch our activity if the user selects this notification
//        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
//                new Intent(this, LocalServiceActivities.Controller.class), 0);
//
//        // Set the info for the views that show in the notification panel.
//        notification.setLatestEventInfo(this, getText(R.string.local_service_label),
//                text, contentIntent);
//
//        // Send the notification.
//        mNM.notify(NOTIFICATION, notification);
    }

//    private class LocationResponseReceiver extends BroadcastReceiver {
//        // Prevents instantiation
//        private LocationResponseReceiver() {
//        }
//        // Called when the BroadcastReceiver gets an Intent it's registered to receive
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            /**
//             * Handle Intents here.
//             */
//
//        }
//    }
}
