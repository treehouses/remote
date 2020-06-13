package io.treehouses.remote.pojo;

public class NetworkProfile {
    public String ssid;
    public String password;
    public String option;

    //For Bridge
    public String hotspot_ssid;
    public String hotspot_password;

    public boolean isHidden = false;

    private int profileType;

    //Wifi
    public NetworkProfile(String ssid, String password, boolean isHidden) {
        setCommon(ssid, password, isHidden, 0);
    }

    //Hotspot
    public NetworkProfile(String ssid, String password, String option, boolean isHidden) {
        setCommon(ssid, password, isHidden, 1);
        this.option = option;
    }

    //Bridge
    public NetworkProfile(String ssid, String password, String hotspotSSID, String hotspotPassword) {
        setCommon(ssid, password, false, 2);
        this.hotspot_ssid = hotspotSSID;
        this.hotspot_password = hotspotPassword;
    }

    private void setCommon(String ssid, String password, boolean isHidden, int profileType) {
        this.ssid = ssid;
        this.password = password;
        this.isHidden = isHidden;
        this.profileType = profileType;
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
