package io.treehouses.remote.pojo

class NetworkProfile {
    var ssid: String
    var password: String
    var option: String? = null

    //For Bridge
    var hotspot_ssid: String? = null
    var hotspot_password: String? = null
    private var profileType: Int

    //Wifi
    constructor(ssid: String, password: String) {
        this.ssid = ssid
        this.password = password
        profileType = 0
    }

    //Hotspot
    constructor(ssid: String, password: String, option: String?) {
        this.ssid = ssid
        this.password = password
        this.option = option
        profileType = 1
    }

    //Bridge
    constructor(ssid: String, password: String, hotspotSSID: String?, hotspotPassword: String?) {
        this.ssid = ssid
        this.password = password
        hotspot_ssid = hotspotSSID
        hotspot_password = hotspotPassword
        profileType = 2
    }

    val isWifi: Boolean
        get() = profileType == 0

    val isHotspot: Boolean
        get() = profileType == 1

    val isBridge: Boolean
        get() = profileType == 2
}