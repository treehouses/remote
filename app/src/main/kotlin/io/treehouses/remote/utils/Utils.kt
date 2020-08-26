package io.treehouses.remote.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Base64
import android.widget.Toast
import io.treehouses.remote.callback.HomeInteractListener
import java.io.ByteArrayOutputStream
import java.net.NetworkInterface
import java.nio.charset.Charset
import java.security.MessageDigest
import java.util.*
import java.util.zip.DeflaterOutputStream


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

    fun hashString(toHash: String) : String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(toHash.toByteArray(Charset.forName("UTF-8")))

        val hexString = StringBuffer()
        for (c in hash) {
            val hex = Integer.toHexString(0xff and c.toInt())
            if (hex.length == 1) hexString.append('0')
            hexString.append(hex)
        }
        return hexString.toString()
    }

    fun compressString(toCompress: String) : String{
        val baos = ByteArrayOutputStream()
        val dos = DeflaterOutputStream(baos)
        dos.write(toCompress.toByteArray(Charset.forName("UTF-8")))
        dos.flush()
        dos.close()
        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)
    }

    fun sendMessage(listener: HomeInteractListener, msg: String, toastMsg: String, c: Context?) {
        listener.sendMessage(msg)
        Toast.makeText(c, toastMsg, Toast.LENGTH_LONG).show()
    }
}