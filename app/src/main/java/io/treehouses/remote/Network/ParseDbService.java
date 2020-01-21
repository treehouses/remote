package io.treehouses.remote.Network;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import com.parse.ParseObject;

import java.util.HashMap;

import io.treehouses.remote.utils.Utils;
import io.treehouses.remote.utils.VersionUtils;

public class ParseDbService {
    public ParseDbService() {
    }

    public static void sendLog(Context context, String rpiName, HashMap<String, String> map, SharedPreferences preferences) {
        ParseObject testObject = new ParseObject("userlog");
        testObject.put("title", rpiName + "");
        testObject.put("description", "Connected to bluetooth");
        testObject.put("type", "BT Connection");
        testObject.put("versionCode", VersionUtils.getVersionCode(context));
        testObject.put("versionName", VersionUtils.getVersionName(context));
        testObject.put("deviceName", Build.DEVICE);
        testObject.put("deviceManufacturer", Build.MANUFACTURER);
        testObject.put("deviceModel", Build.MODEL);
        testObject.put("deviceSerialNumber", Utils.getAndroidId(context));
        testObject.put("macAddress", Utils.getMacAddr());
        testObject.put("androidVersion", Build.VERSION.SDK_INT + "");
        testObject.put("gps_latitude", preferences.getString("last_lat", ""));
        testObject.put("gps_longitude", preferences.getString("last_lng", ""));
        testObject.put("imageVersion", map.get("imageVersion") + "");
        testObject.put("treehousesVersion", map.get("treehousesVersion") + "");
        testObject.put("bluetoothMacAddress", map.get("bluetoothMacAddress") + "");
        testObject.saveInBackground();
    }

}
