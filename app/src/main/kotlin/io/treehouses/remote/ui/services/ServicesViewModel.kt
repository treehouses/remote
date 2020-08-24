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
import io.treehouses.remote.utils.*

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

    val selectedService = MutableLiveData<ServiceInfo>()
    val clickedService = MutableLiveData<ServiceInfo>()

    val serviceAction = MutableLiveData<Resource<ServiceInfo>>()

    val autoRunAction = MutableLiveData<Resource<Boolean>>()

    val error = MutableLiveData<String>()

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
//        getApplication<MainApplication>().getString()
    }

    /**
     * Saves the updated services to cache
     */
    fun updateServicesCache() {
    }

    override fun onRead(output: String) {
        Log.e("GOT", output)
        when {
            match(output) == RESULTS.ERROR -> error.value = output
            rawServicesData.value?.status == LOADING -> receiveJSON(output.trim())
            serviceAction.value?.status == LOADING && containsServiceAction(output) -> matchServiceAction(output.trim())
            autoRunAction.value?.status == LOADING && output.contains("service autorun set") -> {
                autoRunAction.value = Resource.success(autoRunAction.value?.data)
            }
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
    private fun matchServiceAction(output: String) : Boolean {
        if (output.contains("started")) {
            selectedService.value?.serviceStatus = ServiceInfo.SERVICE_RUNNING
        } else if (output.contains("stopped and removed")) {
            selectedService.value?.serviceStatus = ServiceInfo.SERVICE_AVAILABLE
        } else if (output.contains("stopped") || output.contains("installed")) {
            selectedService.value?.serviceStatus = ServiceInfo.SERVICE_INSTALLED
        } else {
            return false
        }
        formattedServices.sort()
        serviceAction.value = Resource.success(selectedService.value)
        return true
    }

    fun onInstallClicked(service: ServiceInfo) {
        selectedService.value = service

        if (service.serviceStatus == ServiceInfo.SERVICE_AVAILABLE)
            sendMessage(getString(R.string.TREEHOUSES_SERVICES_INSTALL, service.name))
        else if (service.serviceStatus == ServiceInfo.SERVICE_INSTALLED || service.serviceStatus == ServiceInfo.SERVICE_RUNNING)
            sendMessage(getString(R.string.TREEHOUSES_SERVICES_CLEANUP, service.name))

        serviceAction.value = Resource.loading(service)
    }

    fun onStartClicked(service: ServiceInfo) {
        selectedService.value = service

        if (service.serviceStatus == ServiceInfo.SERVICE_INSTALLED)
            sendMessage(getString(R.string.TREEHOUSES_SERVICES_UP, service.name))
        else if (service.serviceStatus == ServiceInfo.SERVICE_RUNNING)
            sendMessage(getString(R.string.TREEHOUSES_SERVICES_STOP, service.name))

        serviceAction.value = Resource.loading(service)
    }

    fun editEnvVariable(service: ServiceInfo) {}

    fun switchAutoRun(service: ServiceInfo, newValue: Boolean) {
        sendMessage(getString(R.string.TREEHOUSES_SERVICES_AUTORUN, service.name, newValue.toString()))
        autoRunAction.value = Resource.loading()
    }

    fun getLocalLink(service: ServiceInfo) {

    }
    fun getTorLink(service: ServiceInfo) {}


}