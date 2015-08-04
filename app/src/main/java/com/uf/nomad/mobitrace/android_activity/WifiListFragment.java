package com.uf.nomad.mobitrace.android_activity;

/**
 * Created by Babak on 7/27/2015.
 */

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.uf.nomad.mobitrace.Constants;
import com.uf.nomad.mobitrace.R;

import java.util.Map;


public class WifiListFragment extends Fragment {

    private Context appContext;

    public WifiListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_wifi_list, container, false);

        /**
         * Populate text view with last scan results from the stored values upon view creation
         */
        SharedPreferences preferences = appContext.getSharedPreferences(Constants.LAST_SCANRESULT, Context.MODE_PRIVATE);
        Map<String, ?> map = preferences.getAll();
        for (String s : map.keySet()) {
            TextView mtext = (TextView) rootView.findViewById(R.id.wifi_list);
            mtext.append("\n SSID: " + s + "\n");
            String ss[] = ((String) map.get(s)).split(Constants.DELIMITER);
            mtext.append("BSSID: " + ss[0] + "\n Capabilities: " + ss[1]);
        }
        // Inflate the layout for this fragment
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        appContext = activity.getApplicationContext();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


}