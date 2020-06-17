package io.treehouses.remote.bases

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.google.gson.Gson
import io.treehouses.remote.R
import io.treehouses.remote.pojo.ServiceInfo
import io.treehouses.remote.pojo.ServicesData
import io.treehouses.remote.utils.RESULTS
import io.treehouses.remote.utils.match
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
            val alertDialog = AlertDialog.Builder(ContextThemeWrapper(context, R.style.CustomAlertDialogStyle)).setTitle("Tor Browser Not Found").setMessage(spannableString).create()
            alertDialog.show()
            val alertTextView = alertDialog.findViewById<View>(android.R.id.message) as TextView
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
        addServicesToList(services)
        getServices(services)
        formatList(services)
    }

    private fun addServicesToList(services: ArrayList<ServiceInfo>) {
        for (service in servicesData!!.available) {
            if (inServiceList(service, services) == -1) {
                services.add(ServiceInfo(service, ServiceInfo.SERVICE_AVAILABLE, servicesData!!.icon[service],
                        servicesData!!.info[service], servicesData!!.autorun[service]))
            }
        }
    }

    private fun getServices(services: ArrayList<ServiceInfo>) {
        for (service in servicesData!!.installed) {
            checkInServicesList(service, services, true)
        }
        for (service in servicesData!!.running) {
            checkInServicesList(service, services, false)
        }
    }

    private fun checkInServicesList(service: String, services: ArrayList<ServiceInfo>, installedOrRunning: Boolean) {
        if (inServiceList(service, services) != -1) installedOrRunning(service, services, installedOrRunning)
    }

    private fun installedOrRunning(service: String, services: ArrayList<ServiceInfo>, installedOrRunning: Boolean) {
        if (installedOrRunning) services[inServiceList(service, services)].serviceStatus = ServiceInfo.SERVICE_INSTALLED else services[inServiceList(service, services)].serviceStatus = ServiceInfo.SERVICE_RUNNING
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

    private fun isError(output: String): Int {
        return if (match(output) === RESULTS.ERROR) 0 else -1
    }

    protected fun performAction(output: String, services: ArrayList<ServiceInfo>): Int {
        var i = isError(output)
        startJson += output.trim { it <= ' ' }
        if (gettingJSON && startJson.endsWith("}}")) {
            startJson += output.trim { it <= ' ' }
            try {
                val jsonObject = JSONObject(startJson)
                servicesData = Gson().fromJson(jsonObject.toString(), ServicesData::class.java)
                constructServiceList(servicesData, services)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            gettingJSON = false
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