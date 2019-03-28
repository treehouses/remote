package io.treehouses.remote.utils;

import android.util.Log;

public class LogUtils {
    public static void mOffline() {
        Log.e("STATUS", "OFFLINE");
    }

    public static void mIdle() {
        Log.e("STATUS", "IDLE");
    }

    public static void mConnect() {
        Log.e("STATUS", "CONNECTED");
    }
}
