package com.uf.nomad.mobitrace.uploader;

import android.content.Context;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.net.URL;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by Babak on 11/5/2015.
 */
public class UploadAsyncTask extends AsyncTask<URL, Integer, Long> {

    Context context;

    public UploadAsyncTask(Context context) {
        this.context = context;
    }

    protected Long doInBackground(URL... urls) {
        if (urls.length == 0) {
            Log.e("UploadAsyncTask", "NO URL PROVIDED TO UPLOADASYNCTASK...UPLOAD FAILED");
            return 0l;
        } else {
            UploadHandler up = new UploadHandler();
            up.performPostCall(urls[0], databasetoHashMap());
            return 0l;
        }
    }

    private HashMap<String, Object> databasetoHashMap() {
        String deviceId = getDevideID(context);
//        HashMap<String,Obj>
        return null;
    }

    private String getDevideID(Context context) {
        final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        final String tmDevice, tmSerial, androidId;
        tmDevice = "" + tm.getDeviceId();
        tmSerial = "" + tm.getSimSerialNumber();
        androidId = "" + android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

        UUID deviceUuid = new UUID(androidId.hashCode(), ((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
        String deviceId = deviceUuid.toString();
        return deviceId;
    }

    @Override
    protected void onPostExecute(Long result) {

    }
}