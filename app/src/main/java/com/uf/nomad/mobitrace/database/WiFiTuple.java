package com.uf.nomad.mobitrace.database;

/**
 * Created by Roozbeh on 11/3/2015.
 */
public class WiFiTuple {
    public String device_id;
    public int wifi_id;
    public String date_time;
    public String mac_addr;
    public String ssid;
    public int strength;
    public int frequency;
    public int is_sent;
    //TODO: maybe add setter getters for these Tuple classes. Specially if used in android gui.
}
