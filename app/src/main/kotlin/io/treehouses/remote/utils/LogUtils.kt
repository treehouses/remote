package io.treehouses.remote.utils

import android.util.Log
import android.os.Message

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
        Log.d("TREEHOUSES ", message!!)
    }

    fun writeMsg(msg: Message) {
        val write_msg = String((msg.obj as ByteArray))
        Log.d("WRITE", write_msg)
    }
}