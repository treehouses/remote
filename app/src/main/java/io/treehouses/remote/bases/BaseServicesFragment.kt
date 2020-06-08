package io.treehouses.remote.bases

import android.R
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.google.gson.Gson
import io.treehouses.remote.pojo.ServiceInfo
import io.treehouses.remote.pojo.ServicesData
import org.json.JSONException
import org.json.JSONObject
import java.util.*

open class BaseServicesFragment : BaseFragment() {
    private var startJson = ""
    private var gettingJSON = false
    protected var servicesData: ServicesData? = null
    protected fun openLocalURL(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://$url"))
        Log.d("OPENING: ", "http://$url||")
        val title = "Select a browser"
        val chooser = Intent.createChooser(intent, title)
        if (intent.resolveActivity(requireContext().packageManager) != null) startActivity(chooser)
    }

    protected fun openTorURL(url: String) {
        val intent = requireContext().packageManager.getLaunchIntentForPackage("org.torproject.torbrowser")
        if (intent != null) {
            intent.data = Uri.parse("http://$url")
            intent.action = Intent.ACTION_VIEW
            startActivity(intent)
        } else {
            val s = "Please install Tor Browser from: \n\n https://play.google.com/store/apps/details?id=org.torproject.torbrowser"
            val spannableString = SpannableString(s)
            Linkify.addLinks(spannableString, Linkify.ALL)
            val alertDialog = AlertDialog.Builder(context).setTitle("Tor Browser Not Found").setMessage(spannableString).create()
            alertDialog.show()
            val alertTextView = alertDialog.findViewById<View>(R.id.message) as TextView
            alertTextView.movementMethod = LinkMovementMethod.getInstance()
        }
    }

    protected fun checkVersion(versionIntNumber: IntArray): Boolean {
        if (versionIntNumber[0] > MINIMUM_VERSION[0]) return true
        return if (versionIntNumber[0] == MINIMUM_VERSION[0] && versionIntNumber[1] > MINIMUM_VERSION[1]) true else versionIntNumber[0] == MINIMUM_VERSION[0] && versionIntNumber[1] == MINIMUM_VERSION[1] && versionIntNumber[2] >= MINIMUM_VERSION[2]
    }

    protected fun writeToRPI(ping: String) {
        mChatService.write(ping.toByteArray())
    }

    protected fun performService(action: String, command: String, name: String) {
        Log.d("SERVICES", "$action $name")
        Toast.makeText(context, "$name $action", Toast.LENGTH_LONG).show()
        writeToRPI(command)
    }

    protected fun installedOrRunning(selected: ServiceInfo): Boolean {
        return selected.serviceStatus == ServiceInfo.SERVICE_INSTALLED || selected.serviceStatus == ServiceInfo.SERVICE_RUNNING
    }

    private fun constructServiceList(servicesData: ServicesData?, services: ArrayList<ServiceInfo>) {
        if (servicesData == null || servicesData.available == null) {
            Toast.makeText(context, "Error Occurred. Please Refresh", Toast.LENGTH_SHORT).show()
            return
        }
        services.clear()
        for (service in servicesData.available) {
            if (inServiceList(service, services) == -1) {
                services.add(ServiceInfo(service, ServiceInfo.SERVICE_AVAILABLE, servicesData.icon[service],
                        servicesData.info[service], servicesData.autorun[service]))
            }
        }
        for (service in servicesData.installed) {
            if (inServiceList(service, services) == -1) continue
            services[inServiceList(service, services)].serviceStatus = ServiceInfo.SERVICE_INSTALLED
        }
        for (service in servicesData.running) {
            if (inServiceList(service, services) == -1) continue
            services[inServiceList(service, services)].serviceStatus = ServiceInfo.SERVICE_RUNNING
        }
        formatList(services)
    }

    private fun formatList(services: ArrayList<ServiceInfo>) {
        if (inServiceList("Installed", services) == -1) services.add(0, ServiceInfo("Installed", ServiceInfo.SERVICE_HEADER_INSTALLED))
        if (inServiceList("Available", services) == -1) services.add(0, ServiceInfo("Available", ServiceInfo.SERVICE_HEADER_AVAILABLE))
        Collections.sort(services)
    }

    protected fun inServiceList(name: String, services: ArrayList<ServiceInfo>): Int {
        for (i in services.indices) {
            if (services[i].name == name) return i
        }
        return -1
    }

    protected fun isVersionNumber(s: String, versionNumber: IntArray?): Boolean {
        if (!s.contains(".")) return false
        val parts = s.split("[.]".toRegex()).toTypedArray()
        val intParts = IntArray(3)
        if (parts.size != 3) return false
        for (i in parts.indices) {
            try {
                intParts[i] = parts[i].trim { it <= ' ' }.toInt()
            } catch (e: NumberFormatException) {
                return false
            }
        }
        System.arraycopy(intParts, 0, versionNumber, 0, 3)
        return true
    }

    private fun isError(output: String): Boolean {
        return output.toLowerCase().startsWith("usage:") || output.toLowerCase().contains("error") || output.toLowerCase().contains("unknown")
    }

    protected fun performAction(output: String, services: ArrayList<ServiceInfo>): Int {
        var i = -1
        if (isError(output)) {
            i = 0
        } else if (gettingJSON) {
            startJson += output.trim { it <= ' ' }
            if (startJson.endsWith("}}")) {
                startJson += output.trim { it <= ' ' }
                try {
                    val jsonObject = JSONObject(startJson)
                    servicesData = Gson().fromJson(jsonObject.toString(), ServicesData::class.java)
                    constructServiceList(servicesData, services)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                gettingJSON = false
            }
            i = 1
        } else if (output.trim { it <= ' ' }.startsWith("{")) {
            Log.d("STARTED", "performAction: ")
            startJson = output.trim { it <= ' ' }
            gettingJSON = true
        }
        return i
    }

    protected fun isTorURL(output: String, received: Boolean): Boolean {
        return output.contains(".onion") && !received
    }

    protected fun isLocalUrl(output: String, received: Boolean): Boolean {
        return output.contains(".") && output.contains(":") && output.length < 25 && !received
    }

    companion object {
        private val MINIMUM_VERSION = intArrayOf(1, 14, 1)
    }
}