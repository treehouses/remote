package io.treehouses.remote.pojo;

import io.treehouses.remote.utils.SaveUtils;

public class NetworkProfile {
    public String essid;
    public String password;
    public String option;
    //Hotspot
    public NetworkProfile(String essid, String password, String option) {
        this.essid = essid;
        this.password = password;
        this.option = option;
    }

    //Wifi
    public NetworkProfile(String essid, String password) {
        this.essid = essid;
        this.password = password;
        this.option = SaveUtils.NONE;
    }
}
