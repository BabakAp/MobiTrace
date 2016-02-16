package com.uf.nomad.mobitrace;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

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
import com.uf.nomad.mobitrace.database.DataBaseHandler;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LocationUpdateService extends Service implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
        , LocationListener, SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;

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
        createLocationRequest();

        mGoogleApiClient.connect();

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        initListeners();
    }

    public void initListeners()
    {
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocationUpdateService", "Received start id " + startId + ": " + intent);
//        pendingIntent = PendingIntent.getService(getApplicationContext(),0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        mGoogleApiClient.connect();
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
                        if (mLocationRequestHighAccuracy == null) {
                            createLocationRequest();
                        }
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
        mLocationRequestHighAccuracy.setFastestInterval(Constants.LOCATION_INTERVAL_MILLISECONDS / 10);
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
        Toast.makeText(this, "LOCATION UPDATED",
                Toast.LENGTH_SHORT).show();
        /**
         * Write activity confidences to database
         */
        if (orientation[0] == 0) {
            Log.e("LocationUpdateService", "Orientation is 0");
        }
        DataBaseHandler dataBaseHandler = new DataBaseHandler(getApplicationContext());
        dataBaseHandler.openWritable();
        boolean success = dataBaseHandler.insertLocationRecord(mLastLocation,orientation,Constants.getTimestamp());
        dataBaseHandler.close();
        if (!success) {
            Log.e("LocationUpdateService", "INSERTION OF LAST LOCATION INTO DATABASE FAILED");
        }
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
        return mDateFormat.format(new Date());
    }

    /**
     * Thanks to http://www.ahotbrew.com/how-to-detect-forward-and-backward-tilt/
     */
    float[] mGravity;
    float[] mGeomagnetic;
    float orientation[] = new float[3];
    /**
     * Called when sensor values have changed.
     * <p>See {@link SensorManager SensorManager}
     * for details on possible sensor types.
     * <p>See also {@link SensorEvent SensorEvent}.
     * <p/>
     * <p><b>NOTE:</b> The application doesn't own the
     * {@link SensorEvent event}
     * object passed as a parameter and therefore cannot hold on to it.
     * The object may be part of an internal pool and may be reused by
     * the framework.
     *
     * @param event the {@link SensorEvent SensorEvent}.
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mGravity = event.values;
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            mGeomagnetic = event.values;
        }

        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];

            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);

            if (success) {
                SensorManager.getOrientation(R, orientation);
            }
        }
    }

    /**
     * Called when the accuracy of the registered sensor has changed.
     * <p/>
     * <p>See the SENSOR_STATUS_* constants in
     * {@link SensorManager SensorManager} for details.
     *
     * @param sensor
     * @param accuracy The new accuracy of this sensor, one of
     *                 {@code SensorManager.SENSOR_STATUS_*}
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
