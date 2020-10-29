package io.treehouses.remote.network

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import com.parse.ParseObject
import io.treehouses.remote.utils.Utils.macAddr
import io.treehouses.remote.utils.VersionUtils.getVersionCode
import io.treehouses.remote.utils.VersionUtils.getVersionName
import java.util.*

object ParseDbService {
    fun sendLog(context: Context?, rpiName: String, map: HashMap<String, String?>, preferences: SharedPreferences) {
        val testObject = ParseObject("userlog")
        testObject.put("title", rpiName + "")
        testObject.put("description", "Connected to bluetooth")
        testObject.put("type", "BT Connection")
        testObject.put("versionCode", getVersionCode(context))
        testObject.put("versionName", getVersionName(context))
        testObject.put("deviceName", Build.DEVICE)
        testObject.put("deviceManufacturer", Build.MANUFACTURER)
        testObject.put("deviceModel", Build.MODEL)
        testObject.put("macAddress", macAddr)
        testObject.put("androidVersion", Build.VERSION.SDK_INT.toString() + "")
        testObject.put("gps_latitude", preferences.getString("last_lat", "")!!)
        testObject.put("gps_longitude", preferences.getString("last_lng", "")!!)
        testObject.put("imageVersion", map["imageVersion"].toString() + "")
        testObject.put("treehousesVersion", map["treehousesVersion"].toString() + "")
        testObject.put("bluetoothMacAddress", map["bluetoothMacAddress"].toString() + "")
        testObject.saveInBackground()
    }
    fun sendFeedback(map: HashMap<String, String>) {
        val obj = ParseObject("feedback")
        obj.put("name", map["name"].toString() + "")
        obj.put("email", map["email"].toString() + "")
        obj.put("phoneNumber", map["phoneNumber"].toString() + "")
        obj.put("feedbackType", map["feedbackType"].toString() + "")
        obj.put("message", map["message"].toString() + "")
        obj.saveEventually()
    }
}