package io.treehouses.remote.pojo

class NetworkProfile {
    @JvmField
    var ssid: String = ""
    var password: String = ""
    var option: String = ""

    //For Bridge
    var hotspot_ssid: String = ""
    var hotspot_password: String = ""
    var isHidden = false
    private var profileType = 0

    //Wifi
    constructor(ssid: String, password: String, isHidden: Boolean) {
        setCommon(ssid, password, isHidden, 0)
    }

    //Hotspot
    constructor(ssid: String, password: String, option: String, isHidden: Boolean) {
        setCommon(ssid, password, isHidden, 1)
        this.option = option
    }

    //Bridge
    constructor(ssid: String, password: String, hotspotSSID: String, hotspotPassword: String) {
        setCommon(ssid, password, false, 2)
        hotspot_ssid = hotspotSSID
        hotspot_password = hotspotPassword
    }

    private fun setCommon(ssid: String, password: String, isHidden: Boolean, profileType: Int) {
        this.ssid = ssid
        this.password = password
        this.isHidden = isHidden
        this.profileType = profileType
    }

    val isWifi: Boolean
        get() = profileType == 0

    val isHotspot: Boolean
        get() = profileType == 1

    val isBridge: Boolean
        get() = profileType == 2
}