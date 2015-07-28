package com.uf.nomad.mobitrace;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class MyLocationServicesTurnedOnBroadcastReceiver extends BroadcastReceiver {
    public MyLocationServicesTurnedOnBroadcastReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.location.GPS_ENABLED_CHANGE") || intent.getAction().equals("android.location.PROVIDERS_CHANGED")) {
            CharSequence text = "Location services changed!";
            int duration = Toast.LENGTH_SHORT;
            Toast.makeText(context, text, duration).show();
        }
    }
}
