package com.uf.nomad.mobitrace.android_activity;

import android.app.ActivityManager;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.uf.nomad.mobitrace.Constants;
import com.uf.nomad.mobitrace.LocationUpdateService;
import com.uf.nomad.mobitrace.R;
import com.uf.nomad.mobitrace.activity.MyActivityRecognitionIntentService;
import com.uf.nomad.mobitrace.wifi.WifiScanningService;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;


public class MainActivity extends ActionBarActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, FragmentDrawer.FragmentDrawerListener {

    public static MainActivity mThis = null;

    private GoogleApiClient mGoogleApiClient;

    private File mLog;
    private BufferedWriter bufferedWriter;

    private Toolbar mToolbar;
    private FragmentDrawer drawerFragment;

    // Request code to use when launching the resolution activity
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    // Unique tag for the error dialog fragment
    private static final String DIALOG_ERROR = "dialog_error";
    // Bool to track whether the app is already resolving an error
    private boolean mResolvingError = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /**
         * Setup log file
         */
        setupLogFile();

        /**
         * Constructing the toolbar
         */
        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        drawerFragment = (FragmentDrawer)
                getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
        drawerFragment.setUp(R.id.fragment_navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), mToolbar);
        drawerFragment.setDrawerListener(this);

        // display the first navigation drawer view on app launch
        displayView(0);

        mResolvingError = savedInstanceState != null
                && savedInstanceState.getBoolean(STATE_RESOLVING_ERROR, false);

        /**
         * Build GoogleApiClient
         */
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(ActivityRecognition.API)
                .build();
        //If there is a savedInstance, use those values
        updateValuesFromBundle(savedInstanceState);

        /**
         * Start WiFi Scanning Service
         */
        if (!isMyServiceRunning(WifiScanningService.class)) {
            Intent pushIntentWIFI = new Intent(getApplicationContext(), WifiScanningService.class);
            getApplicationContext().startService(pushIntentWIFI);
        }
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // Update the value of mRequestingLocationUpdates from the Bundle, and
            // make sure that the Start Updates and Stop Updates buttons are
            // correctly enabled or disabled.
            if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(
                        REQUESTING_LOCATION_UPDATES_KEY);
//                setButtonsEnabledState();
            }
            // Update the value of mCurrentLocation from the Bundle and update the
            // UI to show the correct latitude and longitude.
            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                // Since LOCATION_KEY was found in the Bundle, we can be sure that
                // mCurrentLocation is not null.
                mCurrentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }
            // Update the value of mLastUpdateTime from the Bundle and update the UI.
            if (savedInstanceState.keySet().contains(LAST_UPDATED_TIME_STRING_KEY)) {
                mLastUpdateTime = savedInstanceState.getString(
                        LAST_UPDATED_TIME_STRING_KEY);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    protected void onResume() {
        super.onResume();
        mThis = this;
        int code = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (code == 0) {
            System.out.println("up to date");
        } else {
            GooglePlayServicesUtil.getErrorDialog(code, this, 0);
        }
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean location_updates_enabled = sharedPref.getBoolean("pref_key_location_updates", false);
        if (!location_updates_enabled) {
            stopLocationUpdates();
        } else {
            startLocationUpdates();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mResolvingError) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mThis = null;
    }

    @Override
    public void onConnected(Bundle bundle) {
        logInfo("Connected to GoogleApiClient...");
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            // Determine whether a Geocoder is available.
            if (!Geocoder.isPresent()) {
                Toast.makeText(this, R.string.no_geocoder_available,
                        Toast.LENGTH_LONG).show();
                return;
            }
        }
        /**
         * Retrieve and print last known location
         */
        getLocClicked();
        /**
         * Start Activity Recognition IntentService
         */
        startMyActivityRecognitionIntentService();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (mResolvingError) {
            // Already attempting to resolve an error.
            return;
        } else if (result.hasResolution()) {
            try {
                mResolvingError = true;
                result.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                mGoogleApiClient.connect();
            }
        } else {
            // Show dialog using GooglePlayServicesUtil.getErrorDialog()
            showErrorDialog(result.getErrorCode());
            mResolvingError = true;
        }
    }

    /**
     * This code is all about building the error dialog
     */
    /* Creates a dialog for an error message */
    private void showErrorDialog(int errorCode) {
        // Create a fragment for the error dialog
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
        // Pass the error that should be displayed
        Bundle args = new Bundle();
        args.putInt(DIALOG_ERROR, errorCode);
        dialogFragment.setArguments(args);
        dialogFragment.show(getSupportFragmentManager(), "errordialog");
    }

    /* Called from ErrorDialogFragment when the dialog is dismissed. */
    public void onDialogDismissed() {
        mResolvingError = false;
    }


    /* A fragment to display an error dialog */
    public static class ErrorDialogFragment extends DialogFragment {
        public ErrorDialogFragment() {
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get the error code and retrieve the appropriate dialog
            int errorCode = this.getArguments().getInt(DIALOG_ERROR);
            return GooglePlayServicesUtil.getErrorDialog(errorCode,
                    this.getActivity(), REQUEST_RESOLVE_ERROR);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            ((MainActivity) getActivity()).onDialogDismissed();
        }
    }

    @Override
    public void onDrawerItemSelected(View view, int position) {
        displayView(position);
    }

    private void displayView(int position) {
        Fragment fragment = null;
        String title = getString(R.string.app_name);
        switch (position) {
            case 0:
                fragment = new HomeFragment();
                Bundle bundle = new Bundle();
                String info = "";
                try {
                    BufferedReader br = new BufferedReader(new FileReader(mLog));
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        info += "\n" + line;
                    }
                } catch (IOException e) {
//                    e.printStackTrace();
                    System.err.println("No log file found");
                }
                bundle.putCharSequence(Constants.HomeFragment_BUNDLEKEY, info);
                fragment.setArguments(bundle);
                title = getString(R.string.title_home);
                break;
            case 1:
                fragment = new WifiListFragment();
                title = getString(R.string.title_wifi_list);
                break;
            case 2:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container_body, fragment);
            fragmentTransaction.commit();

            // set the toolbar title
            getSupportActionBar().setTitle(title);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_RESOLVE_ERROR) {
            mResolvingError = false;
            if (resultCode == RESULT_OK) {
                // Make sure the app is not already connected or attempting to connect
                if (!mGoogleApiClient.isConnecting() &&
                        !mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.connect();
                }
            }
        }
    }


    private static final String STATE_RESOLVING_ERROR = "resolving_error";
    private static final String REQUESTING_LOCATION_UPDATES_KEY = "requesting_location_updates_key";
    private static final String LOCATION_KEY = "location_key";
    private static final String LAST_UPDATED_TIME_STRING_KEY = "last_updated_time_string_key";

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        //keep state if resolving error
        savedInstanceState.putBoolean(STATE_RESOLVING_ERROR, mResolvingError);

        //keep state if updating location
        savedInstanceState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, mRequestingLocationUpdates);
        savedInstanceState.putParcelable(LOCATION_KEY, mCurrentLocation);
        savedInstanceState.putString(LAST_UPDATED_TIME_STRING_KEY, mLastUpdateTime);

        //call super!
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.main_activity:
                Intent intent = new Intent(this, this.getClass());
                startActivity(intent);
                return true;
            case R.id.action_settings:
                openSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void getLocClicked() {
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLastLocation == null) {
            logInfo("Err: Cannot get location, Please turn on location services...");
            showNotificationLocation();
        } else {
            logInfo("Last location: " +
                            String.valueOf(mLastLocation.getLatitude() + " , " + String.valueOf(mLastLocation.getLongitude()))
                            + "..."
            );
        }
    }

    /**
     * Create the location request and set the parameters as shown in this code sample:
     */
    LocationRequest mLocationRequest;
    Location mCurrentLocation;
    String mLastUpdateTime;
    Boolean mRequestingLocationUpdates = false;


    Boolean mRequestingActivityUpdates = false;
    String mLastActivityUpdateTime;

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(100);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public void startLocationUpdates() {
        if (!isMyServiceRunning(LocationUpdateService.class)) {
            System.out.println("Service not running");
            Context context = getApplicationContext();
            Intent pushIntent1 = new Intent(context, LocationUpdateService.class);
            context.startService(pushIntent1);
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void stopLocationUpdates() {
        if (isMyServiceRunning(LocationUpdateService.class)) {
            System.out.println("Service running");
            Context context = getApplicationContext();
            Intent pushIntent1 = new Intent(context, LocationUpdateService.class);
            context.stopService(pushIntent1);
        }
    }

//    @Override
//    public void onLocationChanged(Location location) {
//        mCurrentLocation = location;
//        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
//    }

    //    private void updateUI() {
//        TextView periodicLoc = (TextView) findViewById(R.id.periodicLoc);
//        periodicLoc.setText("Latitude: " + String.valueOf(mCurrentLocation.getLatitude()) +
//                " Longitude: " + String.valueOf(mCurrentLocation.getLongitude()) +
//                " Last Update Time: " + String.valueOf(mLastUpdateTime));
//
//        SharedPreferences mPrefs = getApplicationContext().getSharedPreferences(
//                ActivityUtils.SHARED_PREFERENCES, Context.MODE_PRIVATE);
//        int last_activity = mPrefs.getInt(ActivityUtils.KEY_PREVIOUS_ACTIVITY_TYPE, DetectedActivity.UNKNOWN);
//        mActivityOutput = getNameFromType(last_activity) + " Conf: " + mPrefs.getInt(ActivityUtils.KEY_PREVIOUS_ACTIVITY_CONFIDENCE, 0);
//        displayActivityOutput();
//    }
    PendingIntent callbackIntent;

    protected void startMyActivityRecognitionIntentService() {
        mActivityResultReceiver = new ActivityResultReceiver(new Handler());
        logInfo("Starting ActivityRecognitionIntentService...");
        // Create an intent for passing to the intent service responsible for fetching the address.
        Intent intent = new Intent(this, MyActivityRecognitionIntentService.class);

        // Pass the result receiver as an extra to the service.
        //TODO ADDING ANY EXTRAS MAKES THE ACTIVITY HASRESULT RETURN FALSE! What now??
//        intent.putExtra(Constants.RECEIVER, mActivityResultReceiver);
        callbackIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);


        mRequestingActivityUpdates = true;
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(
                mGoogleApiClient, Constants.DETECTION_INTERVAL_MILLISECONDS, callbackIntent);
    }


    public void stopActivityUpdates(View view) {
        stopActivityUpdates();
    }

    /**
     * Stops location updates (called in onPause), should not be used if app is going to collect locations in background
     */
    protected void stopActivityUpdates() {
        if (mRequestingActivityUpdates) {
            mRequestingActivityUpdates = false;
            ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(mGoogleApiClient, callbackIntent);
        }
    }

    private ActivityResultReceiver mActivityResultReceiver;
    protected String mActivityOutput;

    class ActivityResultReceiver extends ResultReceiver {
        public ActivityResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            // Display the address string
            // or an error message sent from the intent service.
            mActivityOutput = resultData.getString(Constants.RESULT_DATA_KEY);
            // Show a toast message if an address was found.
            if (resultCode == Constants.SUCCESS_RESULT) {
                Toast.makeText(getApplicationContext(), getString(R.string.activity_found), Toast.LENGTH_SHORT).show();
            }

        }
    }

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

    /**
     * Begin: Helper methods for logging
     */
    private void setupLogFile() {
        if (isExternalStorageWritable()) {
            mLog = getDocumentStorageDir(getApplicationContext(), "" + new Timestamp(new Date().getTime()).getTime());
            try {
                bufferedWriter = new BufferedWriter(new FileWriter(mLog));
            } catch (IOException e) {
//                e.printStackTrace();
                System.err.println("Could not create log file");
            }
        }
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    public File getDocumentStorageDir(Context context, String fileName) {
        // Get the directory for the app's private documents directory.
        File directory = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        String absolutePath = null;
        try {
            absolutePath = directory.getAbsolutePath();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return absolutePath != null ? new File(absolutePath + File.separator + fileName + ".txt") : null;
    }

    public void logInfo(String info) {
        try {
            Long timestamp = new Timestamp(new Date().getTime()).getTime();
            bufferedWriter.write("\n" + timestamp + " : " + info);
            bufferedWriter.flush();
            TextView mtext = (TextView) findViewById(R.id.global_info);
            mtext.append("\n" + timestamp + " : " + info);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * END: Helper methods for logging
     */


    /**
     * Show a notification to turn on location services
     */
    public void showNotificationLocation() {
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
     * Geo Coder code
     */
    //    /**
//     * Creates an intent, adds location data to it as an extra, and starts the intent service for
//     * fetching an address.
//     */
//    protected void startMyGeoCoderIntentService() {
//        // Create an intent for passing to the intent service responsible for fetching the address.
//        Intent intent = new Intent(this, MyGeoCoderIntentService.class);
//
//        // Pass the result receiver as an extra to the service.
////        intent.putExtra(Constants.RECEIVER, mAddressResultReceiver);
//
//        // Pass the location data as an extra to the service.
//        if (mCurrentLocation == null) {
//            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
//        }
//        intent.putExtra(Constants.LOCATION_DATA_EXTRA, mCurrentLocation);
//
//        // Start the service. If the service isn't already running, it is instantiated and started
//        // (creating a process for it if needed); if it is running then it remains running. The
//        // service kills itself automatically once all intents are processed.
//        startService(intent);
//    }
    //    public void startMyGeoCoderIntentService(View view) {
//        mAddressResultReceiver = new AddressResultReceiver(new Handler());
//        ((TextView) findViewById(R.id.address)).setText("Receiving Address...");
//        startMyGeoCoderIntentService();
//    }
//    protected void displayAddressOutput() {
//        TextView address = (TextView) findViewById(R.id.address);
//
//        address.setText(mAddressOutput);
//    }

//    protected void displayActivityOutput() {
//        TextView activity = (TextView) findViewById(R.id.periodicAct);
//
//        activity.setText(mActivityOutput);
//    }

//    private AddressResultReceiver mAddressResultReceiver;
//    protected String mAddressOutput;
//
//    class AddressResultReceiver extends ResultReceiver {
//        public AddressResultReceiver(Handler handler) {
//            super(handler);
//        }
//
//        @Override
//        protected void onReceiveResult(int resultCode, Bundle resultData) {
//
//            // Display the address string
//            // or an error message sent from the intent service.
//            mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
//            displayAddressOutput();
//            // Show a toast message if an address was found.
//            if (resultCode == Constants.SUCCESS_RESULT) {
//                showToast(getString(R.string.address_found));
//            }
//
//        }
//    }
}
