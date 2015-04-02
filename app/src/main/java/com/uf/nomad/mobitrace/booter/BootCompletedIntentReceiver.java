package com.uf.nomad.mobitrace.booter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.uf.nomad.mobitrace.LocationUpdateService;

/**
 * Created by Babak on 4/1/2015.
 */
public class BootCompletedIntentReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            //TODO: BackgroundService.class should be implemented
            Intent pushIntent1 = new Intent(context, LocationUpdateService.class);
            context.startService(pushIntent1);

//            Intent pushIntent2 = new Intent(context,activity class);
//            context.startService(pushIntent2);
        }
    }
}
