package com.uf.nomad.mobitrace.booter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.uf.nomad.mobitrace.LocationUpdateService;
import com.uf.nomad.mobitrace.activity.ActivityRecognitionUpdateService;

/**
 * Created by Babak on 4/1/2015.
 */
public class BootCompletedIntentReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //TODO: STOP ALL SERVICES IF FALSE
        SharedPreferences myPref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean isActive =  myPref.getBoolean("pref_key_services", true);
        System.out.println("SERVICES ARE ACTIVE: " + isActive);
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction()) && isActive) {
            System.err.println("BOOT COMPLETED");
            //TODO: BackgroundService.class should be implemented
            Intent pushIntent1 = new Intent(context, LocationUpdateService.class);
            context.startService(pushIntent1);

            Intent pushIntent2 = new Intent(context, ActivityRecognitionUpdateService.class);
            context.startService(pushIntent2);
        }
    }
}
