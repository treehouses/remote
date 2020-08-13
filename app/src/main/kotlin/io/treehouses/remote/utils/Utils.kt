package io.treehouses.remote.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import java.net.NetworkInterface
import java.util.*

object Utils {
    fun Context.copyToClipboard(clickedData: String) {
        var clickedData = clickedData
        if (clickedData.contains("Command: ") || clickedData.contains(" Command:") || clickedData.contains("Command:")) {
            clickedData = clickedData.substring(10)
        }
        val clipboard = this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("label", clickedData)
        clipboard.setPrimaryClip(clip)

        this.toast("Copied to clipboard: $clickedData", Toast.LENGTH_LONG)
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

    @Throws(Exception::class)
    private fun getAddress(nif: NetworkInterface): String {
        val macBytes = nif.hardwareAddress ?: return ""
        val res1 = StringBuilder()
        for (b in macBytes) {
            res1.append(String.format("%02X:", b))
        }
        if (res1.isNotEmpty()) {
            res1.deleteCharAt(res1.length - 1)
        }
        return res1.toString()
    }

    fun Context?.toast(s: String, duration: Int = Toast.LENGTH_LONG): Toast {
        return Toast.makeText(this, s, duration).apply { show() }
    }

    fun checkAppIsInstalled(c:Context, v: View, intent:Intent, String1:String, String2:String, String3:String):Boolean{
        val activities = c.packageManager.queryIntentActivities(intent, 0)
        return if (activities.size == 0) {
            Snackbar.make(v, String1, Snackbar.LENGTH_LONG).setAction(String2) {
                val intent1 = Intent(Intent.ACTION_VIEW)
                intent1.data = Uri.parse(String3)
                c.startActivity(intent1)
            }.show()
            true
        }
        else false
    }
}