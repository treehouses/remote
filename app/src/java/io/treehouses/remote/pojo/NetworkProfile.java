package io.treehouses.remote.pojo;

public class NetworkProfile {
    public String ssid;
    public String password;
    public String option;

    //For Bridge
    public String hotspot_ssid;
    public String hotspot_password;

    private int profileType;

    //Wifi
    public NetworkProfile(String ssid, String password) {
        this.ssid = ssid;
        this.password = password;
        this.profileType = 0;
    }

    //Hotspot
    public NetworkProfile(String ssid, String password, String option) {
        this.ssid = ssid;
        this.password = password;
        this.option = option;
        this.profileType = 1;
    }

    //Bridge
    public NetworkProfile(String ssid, String password, String hotspotSSID, String hotspotPassword) {
        this.ssid = ssid;
        this.password = password;
        this.hotspot_ssid = hotspotSSID;
        this.hotspot_password = hotspotPassword;
        this.profileType = 2;
    }

    public boolean isWifi() {
        return this.profileType == 0;
    }

    public boolean isHotspot() {
        return this.profileType == 1;
    }

    public boolean isBridge() {
        return this.profileType == 2;
    }
}
