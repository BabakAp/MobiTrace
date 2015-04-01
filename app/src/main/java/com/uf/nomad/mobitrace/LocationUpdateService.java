package com.uf.nomad.mobitrace;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;

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


    public LocationUpdateService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

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

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
//        if (mLastLocation != null) {
//            // Determine whether a Geocoder is available.
//            if (!Geocoder.isPresent()) {
//                Toast.makeText(this, R.string.no_geocoder_available,
//                        Toast.LENGTH_LONG).show();
//                return;
//            }
//        }
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

    }


    private class LocationResponseReceiver extends BroadcastReceiver {
        // Prevents instantiation
        private LocationResponseReceiver() {
        }
        // Called when the BroadcastReceiver gets an Intent it's registered to receive

        @Override
        public void onReceive(Context context, Intent intent) {
            /**
             * Handle Intents here.
             */

        }
    }
}
