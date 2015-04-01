package com.uf.nomad.mobitrace.booter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Babak on 4/1/2015.
 */
public class BootCompletedIntentReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            //TODO: BackgroundService.class should be implemented
//            Intent pushIntent = new Intent(context, BackgroundService.class);
//            context.startService(pushIntent);
        }
    }
}
