package io.treehouses.remote.utils

import android.app.AlertDialog
import android.content.*
import android.net.Uri
import android.view.View
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import android.util.Base64
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import io.treehouses.remote.R
import io.treehouses.remote.callback.HomeInteractListener
import io.treehouses.remote.callback.NotificationCallback
import io.treehouses.remote.interfaces.FragmentDialogInterface
import java.io.ByteArrayOutputStream
import java.net.NetworkInterface
import java.nio.charset.Charset
import java.security.MessageDigest
import io.treehouses.remote.pojo.ReverseData
import java.util.*
import java.util.zip.DeflaterOutputStream


object Utils: FragmentDialogInterface {
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


    fun checkAppIsInstalled(c:Context, v: View, intent:Intent, StringArr:Array<String>):Boolean {
        val activities = c.packageManager.queryIntentActivities(intent, 0)
        return if (activities.size == 0) {
            Snackbar.make(v, StringArr[0], Snackbar.LENGTH_LONG).setAction(StringArr[1]) {
                val intent1 = Intent(Intent.ACTION_VIEW)
                intent1.data = Uri.parse(StringArr[2])
                c.startActivity(intent1)
            }.show()
            true
        } else false
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

    fun sendMessage(listener: HomeInteractListener, msg: Pair<String, String>, c: Context?, length: Int) {
        listener.sendMessage(msg.first)
        Toast.makeText(c, msg.second, length).show()
    }

    fun attach(context: Context?): NotificationCallback? {
        return try { context as NotificationCallback?
        } catch (e: ClassCastException) {
            throw ClassCastException("Activity must implement NotificationListener")
        }
    }
  
    fun <T> String.convertToObject(thisClass: Class<T>): T? {
        return try { Gson().fromJson(this, thisClass) }
        catch (e: Exception) { null }
    }

    fun showRemoteReverse(output: String, reverseText: MutableLiveData<String>){
        val reverseData = Gson().fromJson(output, ReverseData::class.java)
        val ip = "ip: " + reverseData.ip
        val postal = "postal: " + reverseData.postal
        val city = "city: " + reverseData.city
        val country = "country: " + reverseData.country
        val org = "org: " + reverseData.org
        val timezone = "timezone: " + reverseData.timezone
        reverseText.value = ip + "\n" + org  + "\n" + country + "\n" + city + "\n" + postal + "\n" + timezone
    }

    fun createRemoteReverseDialog(context: Context?): AlertDialog? {
        val a  = createAlertDialog(context, R.style.CustomAlertDialogStyle, "Reverse Lookup", "Calling...")
                .setNegativeButton("Dismiss") { dialog: DialogInterface, _: Int -> dialog.dismiss() }.create()
        a.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        return a
    }

    fun setObserverMessage(dialog: AlertDialog?, it:String){
        dialog!!.setMessage(it)
    }
}