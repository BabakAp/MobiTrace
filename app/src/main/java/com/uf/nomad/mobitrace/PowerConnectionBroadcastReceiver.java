package com.uf.nomad.mobitrace;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.util.Log;

/**
 * Created by Babak on 4/6/2015.
 */
public class PowerConnectionBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //TODO: Take action based on battery information
        //Charging?
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;
        Log.e("POWERCONNECTIONBROADCASTRECEIVER", "charging: " + isCharging);

        //charging via AC or USB
        int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
        Log.e("POWERCONNECTIONBROADCASTRECEIVER", "usb plugged: " + usbCharge);
        boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
        Log.e("POWERCONNECTIONBROADCASTRECEIVER", "AC plugged: " + acCharge);


        //battery remaining percentage
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        float batteryPct = level / (float) scale;
    }
}