package io.treehouses.remote.ui.services

import android.app.AlertDialog
import android.app.Application
import android.content.DialogInterface
import android.util.Log
import android.view.ContextThemeWrapper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.google.gson.Gson
import com.google.gson.JsonParseException
import io.treehouses.remote.MainApplication
import io.treehouses.remote.R
import io.treehouses.remote.bases.FragmentViewModel
import io.treehouses.remote.pojo.ServiceInfo
import io.treehouses.remote.pojo.ServicesData
import io.treehouses.remote.pojo.enum.Resource
import io.treehouses.remote.pojo.enum.Status.LOADING
import io.treehouses.remote.pojo.enum.Status.SUCCESS
import io.treehouses.remote.utils.constructServiceListFromData
import io.treehouses.remote.utils.formatServices

class ServicesViewModel(application: Application) : FragmentViewModel(application) {

    private var servicesJSON = ""
    private var currentlyReceivingJSON = false

    private val rawServicesData = MutableLiveData<Resource<ServicesData>>()
    val servicesData : LiveData<Resource<HashMap<String, ServiceInfo>>> = Transformations.map(rawServicesData) {
        return@map when (it.status) {
            SUCCESS -> {
                val services = constructServiceListFromData(it.data)
                if (services.isNotEmpty()) {
                    formattedServices = formatServices(services)
                    Resource.success(services)
                }
                else Resource.error("Uh oh! Something went wrong! Please refresh.", services)
            }
            else -> Resource(it.status, hashMapOf(), it.message)
        }
    }
    var formattedServices = mutableListOf<ServiceInfo>()

    val clickedService = MutableLiveData<ServiceInfo>()

    /**
     * Send the request to receive the JSON file containing all services data
     * @see R.string.TREEHOUSES_REMOTE_ALLSERVICES
     */
    fun fetchServicesFromServer() {
        sendMessage(getString(R.string.TREEHOUSES_REMOTE_ALLSERVICES))
        rawServicesData.value = Resource.loading()
    }

    /**
     * Get the services from the cache if it exists
     */
    fun fetchServicesFromCache() {

    }

    /**
     * Saves the updated services to cache
     */
    fun updateServicesCache() {
    }

    override fun onRead(output: String) {
        Log.e("GOT", output)
        when {
            rawServicesData.value?.status == LOADING -> receiveJSON(output.trim())
        }
    }

    /**
     * Receive JSON All Services data
     */
    private fun receiveJSON(current: String) {
        servicesJSON += current
        if (currentlyReceivingJSON && servicesJSON.endsWith("}}")) {
            try {
                Log.e("GOT", servicesJSON)
                val data = Gson().fromJson(servicesJSON, ServicesData::class.java)
                rawServicesData.value = if (data != null) Resource.success(data) else Resource.error("Unknown Error")
            } catch (e: JsonParseException) {
                rawServicesData.value = Resource.error("Error in receiving Services Data ")
            }
            currentlyReceivingJSON = false
        }
        else if (current.startsWith("{") && !currentlyReceivingJSON) {
            servicesJSON = current
            currentlyReceivingJSON = true
        }
    }

    fun onInstallClicked(service: ServiceInfo?) {
        if (service == null) return
//        if (service.serviceStatus == ServiceInfo.SERVICE_AVAILABLE)
//            runServiceCommand("Installing", s.name)
//        else if (installedOrRunning(s)) {
//            val dialog = AlertDialog.Builder(ContextThemeWrapper(activity, R.style.CustomAlertDialogStyle))
//                    .setTitle("Delete " + selected!!.name + "?")
//                    .setMessage("Are you sure you would like to delete this service? All of its data will be lost and the service must be reinstalled.")
//                    .setPositiveButton("Delete") { _: DialogInterface?, _: Int ->
//                        runServiceCommand("Uninstalling", s.name)
//                    }.setNegativeButton("Cancel") { dialog: DialogInterface, _: Int -> dialog.dismiss() }.create()
//            dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
//            dialog.show()
//        }
    }

    fun onStartClicked(service: ServiceInfo?) {
        if (service == null) return
        if (service.serviceStatus == ServiceInfo.SERVICE_INSTALLED)
            sendMessage(getApplication<MainApplication>().getString(R.string.TREEHOUSES_SERVICES_UP))
        else if (service.serviceStatus == ServiceInfo.SERVICE_RUNNING)
            sendMessage(getApplication<MainApplication>().getString(R.string.TREEHOUSES_SERVICES_UP))
    }

    fun editEnvVariable(service: ServiceInfo) {}

    fun switchAutoRun(service: ServiceInfo) {}

    fun getLocalLink(service: ServiceInfo) {}
    fun getTorLink(service: ServiceInfo) {}


}