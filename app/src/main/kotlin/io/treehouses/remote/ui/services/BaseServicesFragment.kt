package io.treehouses.remote.ui.services

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import io.treehouses.remote.bases.BaseFragment
import io.treehouses.remote.R
import io.treehouses.remote.pojo.ServiceInfo
import io.treehouses.remote.pojo.ServicesData
import io.treehouses.remote.utils.RESULTS
import io.treehouses.remote.utils.match
import org.json.JSONException
import org.json.JSONObject
import java.lang.NullPointerException
import java.util.*

open class BaseServicesFragment() : BaseFragment() {
    private var startJson = ""
    private var gettingJSON = false
    private var servicesData: ServicesData? = null

    lateinit var viewModel: ServicesViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        arguments?.let {
//            if (it.containsKey("services"))
//                services = it.getSerializable("services") as ArrayList<ServiceInfo>
//        }
    }

    fun getViewModel() {
        viewModel = activity?.run {ViewModelProvider(this)[ServicesViewModel::class.java]}!!
    }

    protected fun openLocalURL(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://$url"))
        Log.d("OPENING: ", "http://$url||")
        val title = "Select a browser"
        val chooser = Intent.createChooser(intent, title)
        if (intent.resolveActivity(requireContext().packageManager) != null) startActivity(chooser)
    }

    protected fun writeToRPI(ping: String) {
        mChatService.write(ping.toByteArray())
    }

    protected fun performService(action: String, name: String) {
        var command = ""
        if (action == "Starting") command = getString(R.string.TREEHOUSES_SERVICES_UP, name)
        else if (action == "Installing") command = getString(R.string.TREEHOUSES_SERVICES_INSTALL, name)
        else if (action == "Stopping") command = getString(R.string.TREEHOUSES_SERVICES_STOP, name)
        else command = getString(R.string.TREEHOUSES_SERVICES_CLEANUP, name)
        Log.d("SERVICES", "$action $name")
        Toast.makeText(context, "$name $action", Toast.LENGTH_LONG).show()
        writeToRPI(command)
    }

    protected fun installedOrRunning(selected: ServiceInfo): Boolean {
        return selected.serviceStatus == ServiceInfo.SERVICE_INSTALLED || selected.serviceStatus == ServiceInfo.SERVICE_RUNNING
    }

    private fun constructServiceList(servicesData: ServicesData?, services: ArrayList<ServiceInfo>) {
        if (servicesData?.available == null) {
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
                try {
                    services.add(ServiceInfo(service, servicesData!!.size[service]?.toInt()!!, ServiceInfo.SERVICE_AVAILABLE, servicesData!!.icon[service],
                            servicesData!!.info[service], servicesData!!.autorun[service]))
                } catch (exception:NullPointerException){
                    Log.e("Error", exception.toString())
                }
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
        if (inServiceList("Installed", services) == -1) addToServices(services, "Installed", ServiceInfo.SERVICE_HEADER_INSTALLED)
        if (inServiceList("Available", services) == -1) addToServices(services, "Available", ServiceInfo.SERVICE_HEADER_AVAILABLE)
        services.sort()
    }

    private fun addToServices(services: ArrayList<ServiceInfo>, str: String, flag: Int) {
        services.add(0, ServiceInfo(str, flag))
    }

    protected fun inServiceList(name: String, services: ArrayList<ServiceInfo>): Int {
        for (i in services.indices) {
            if (services[i].name == name) return i
        }
        return -1
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

}