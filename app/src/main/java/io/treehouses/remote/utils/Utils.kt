package io.treehouses.remote.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import java.net.NetworkInterface
import java.util.*

object Utils {
    fun copyToClipboard(context: Context, clickedData: String) {
        var clickedData = clickedData
        if (clickedData.contains("Command: ") || clickedData.contains(" Command:") || clickedData.contains("Command:")) {
            clickedData = clickedData.substring(10)
        }
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("label", clickedData)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied to clipboard: $clickedData", Toast.LENGTH_LONG).show()
    }

    @JvmStatic
    val macAddr: String
        get() {
            try {
                val all: List<NetworkInterface> = Collections.list(NetworkInterface.getNetworkInterfaces())
                for (nif in all) {
                    if (!nif.name.equals("wlan0", ignoreCase = true)) continue
                    return getAddress(nif)
                }
            } catch (ignored: Exception) {
            }
            return ""
        }

    //    public static String  getAndroidId(Context context){
    //         return Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
    //
    //    }
    @Throws(Exception::class)
    private fun getAddress(nif: NetworkInterface): String {
        val macBytes = nif.hardwareAddress ?: return ""
        val res1 = StringBuilder()
        for (b in macBytes) {
            res1.append(String.format("%02X:", b))
        }
        if (res1.length > 0) {
            res1.deleteCharAt(res1.length - 1)
        }
        return res1.toString()
    }

    fun toast(context: Context?, s: String?) {
        Toast.makeText(context, s, Toast.LENGTH_LONG).show()
    }
}