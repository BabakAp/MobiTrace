package com.uf.nomad.mobitrace.booter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.uf.nomad.mobitrace.Constants;
import com.uf.nomad.mobitrace.LocationUpdateService;
import com.uf.nomad.mobitrace.activity.ActivityRecognitionUpdateService;
import com.uf.nomad.mobitrace.wifi.WifiScanningService;

/**
 * Created by Babak on 4/1/2015.
 */
public class BootCompletedIntentReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences myPref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean isActive = myPref.getBoolean("pref_key_services", true);
        Log.i(Constants.APPTAG, "SERVICES ARE ACTIVE: " + isActive);
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction()) && isActive) {
            Log.i(Constants.APPTAG,"BOOT COMPLETED");
            Intent pushIntent1 = new Intent(context, LocationUpdateService.class);
            context.startService(pushIntent1);

            Intent pushIntent2 = new Intent(context, ActivityRecognitionUpdateService.class);
            context.startService(pushIntent2);

            Intent pushIntentWIFI = new Intent(context, WifiScanningService.class);
            context.startService(pushIntentWIFI);
        }
    }
}
