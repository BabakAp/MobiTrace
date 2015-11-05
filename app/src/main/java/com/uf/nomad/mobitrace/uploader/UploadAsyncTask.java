package com.uf.nomad.mobitrace.uploader;

import android.content.Context;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.uf.nomad.mobitrace.Constants;
import com.uf.nomad.mobitrace.database.DataBaseHandler;

import java.net.URL;
import java.util.HashMap;

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
            return -1l;
        } else {
            UploadHandler up = new UploadHandler();
            //TODO: WHAT DOES THE SERVER RESPOND? WHAT TO DO WITH IT?
            up.performPostCall(urls[0], databasetoHashMap());
            return 1l;
        }
    }

    private HashMap<String, Object> databasetoHashMap() {
        String deviceId = getDeviceID(context);
        HashMap<String, Object> map = new HashMap<>();
        DataBaseHandler db = new DataBaseHandler(context);
        map.put("w", db.getWiFiList(deviceId));
        map.put("l", db.getLocationList(deviceId));
        map.put("a", db.getActivityList(deviceId));
        return map;
    }

    /**
     * @param context
     * @return A hash of Telephony Manager DeviceID, SimSerialNumber
     */
    private String getDeviceID(Context context) {
        final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        final String tmDevice, tmSerial, androidId;
        tmDevice = "" + tm.getDeviceId();
        tmSerial = "" + tm.getSimSerialNumber();
        String deviceId = Constants.SHA256(tmDevice + tmSerial);
        return deviceId;
    }

    @Override
    protected void onPostExecute(Long result) {
        Log.d("UploadAsyncTask", "UPLOAD FINISHED");
    }
}