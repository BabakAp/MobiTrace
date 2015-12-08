package com.uf.nomad.mobitrace.uploader;

import android.content.Context;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

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
            DataBaseHandler db = new DataBaseHandler(context);
            db.openWritable();
            db.updateAllNotSendToSend();
            db.close();
            return 1l;
        }
    }

    private HashMap<String, Object> databasetoHashMap() {
        String deviceId = getDeviceID(context);
        HashMap<String, Object> map = new HashMap<>();
        DataBaseHandler db = new DataBaseHandler(context);
        db.openReadable();
        map.put("device_id",deviceId);
        map.put("w", db.getWiFiList());
        map.put("l", db.getLocationList());
        map.put("a", db.getActivityList());
        db.close();
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
        String msg = "UPLOAD FINISHED";
        Log.d("UploadAsyncTask", msg);
        Toast.makeText(context, msg,
                Toast.LENGTH_SHORT).show();
    }
}