package io.treehouses.remote.utils

import android.util.Log

object LogUtils {
    fun mOffline() {
        Log.e("STATUS", "OFFLINE")
    }

    fun mIdle() {
        Log.e("STATUS", "IDLE")
    }

    fun mConnect() {
        Log.e("STATUS", "CONNECTED")
    }

    fun log(message: String?) {
        Log.d("TREEHOUSES ", message)
    }
}