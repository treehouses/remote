package io.treehouses.remote.pojo;

import io.treehouses.remote.utils.SaveUtils;

public class NetworkProfile {
    public String ssid;
    public String password;
    public String option;
    //Hotspot
    public NetworkProfile(String ssid, String password, String option) {
        this.ssid = ssid;
        this.password = password;
        this.option = option;
    }

    //Wifi
    public NetworkProfile(String ssid, String password) {
        this.ssid = ssid;
        this.password = password;
        this.option = SaveUtils.NONE;
    }
}
