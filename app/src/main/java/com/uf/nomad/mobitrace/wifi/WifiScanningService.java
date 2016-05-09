package com.uf.nomad.mobitrace.wifi;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.util.Log;

import com.uf.nomad.mobitrace.Constants;
import com.uf.nomad.mobitrace.database.DataBaseHandler;

import java.util.Calendar;
import java.util.List;

public class WifiScanningService extends Service {

    private WifiManager wifi;
    private List<ScanResult> results;
    private int size;
    private BroadcastReceiver receiver;

    private Intent fromMyWifiBroadcastReceiver;

    public WifiScanningService() {
    }


    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("WIFI SCANNER STARTED");
        wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        wifi.startScan();

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
                System.out.println("wifi onReceive");
                results = wifi.getScanResults();
                size = results.size();


                /**
                 * Store the last ScanResult, to be used to populate WiFi list view
                 */
                SharedPreferences.Editor editor = getSharedPreferences(Constants.LAST_SCANRESULT, MODE_PRIVATE).edit();
                //Clear old data
                editor.clear();
                DataBaseHandler dataBaseHandler = new DataBaseHandler(getApplicationContext());
                dataBaseHandler.openWritable();
                for (ScanResult sr : results) {
                    dataBaseHandler.insertWiFiRecord(sr, Constants.getTimestamp());
//                    System.out.println("WIFI RESULTS: " + sr.SSID + " " + sr.BSSID + " " + sr.capabilities);
                    editor.putString(sr.SSID, sr.BSSID + Constants.DELIMITER + sr.capabilities);
                }
                dataBaseHandler.close();
                editor.apply();

                /**
                 * This round of scanning has yielded its results, kill yourself! :)
                 */
                unregisterReceiver(receiver);
                WifiScanningService.this.stopSelf();

                /**
                 * Wake me up (again!) when september ends
                 */
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                Intent MyWifiBroadcastReceiverIntent = new Intent(getApplicationContext(), MyWifiBroadcastReceiver.class);
                PendingIntent pintent = PendingIntent.getBroadcast(getApplicationContext(), 0, MyWifiBroadcastReceiverIntent, 0);
                AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                alarm.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis() + Constants.WIFI_INTERVAL_MILLISECONDS, pintent);

                /**
                 * Release the wakelock acquired by MyWifiBroadcastReceiver
                 */
                MyWifiBroadcastReceiver.completeWakefulIntent(fromMyWifiBroadcastReceiver);
            }
        };
        registerReceiver(receiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("WifiScanningService", "Received start id " + startId + ": " + intent);
        fromMyWifiBroadcastReceiver = intent;
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
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
