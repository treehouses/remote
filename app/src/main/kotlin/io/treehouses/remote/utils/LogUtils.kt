package io.treehouses.remote.utils

import android.util.Log
import android.os.Message
import androidx.lifecycle.ViewModel
import io.treehouses.remote.BuildConfig
import io.treehouses.remote.bases.BaseDialogFragment
import io.treehouses.remote.bases.BaseFragment

object LogUtils {
    fun mOffline() {
        if (BuildConfig.DEBUG) Log.e("STATUS", "OFFLINE")
    }

    fun mIdle() {
        if (BuildConfig.DEBUG) Log.e("STATUS", "IDLE")
    }

    fun mConnect() {
        if (BuildConfig.DEBUG) Log.e("STATUS", "CONNECTED")
    }

    fun log(message: String?) {
        if (BuildConfig.DEBUG) {
            Log.d("TREEHOUSES ", message!!)
        }
    }

    fun writeMsg(msg: Message) {
        val write_msg = String((msg.obj as ByteArray))
        Log.d("WRITE", write_msg)
    }
}

fun BaseFragment.logD(msg: String) { logD(this.javaClass.simpleName, msg) }

fun BaseFragment.logE(msg: String, e: Throwable? = null) { logE(this.javaClass.simpleName, msg, e) }

fun BaseDialogFragment.logD(msg: String) {logD(this.javaClass.simpleName, msg)}

fun BaseDialogFragment.logE(msg: String, e: Throwable? = null) {logE(this.javaClass.simpleName, msg, e)}

fun ViewModel.logE(msg: String, e: Throwable? = null) { logE(this.javaClass.simpleName, msg, e) }

private fun logD(tag : String, msg: String) {
    if (BuildConfig.DEBUG) Log.d(tag, msg)
}

private fun logE(tag : String, msg: String, e: Throwable? = null) {
    if (BuildConfig.DEBUG) Log.e(tag, msg)
}