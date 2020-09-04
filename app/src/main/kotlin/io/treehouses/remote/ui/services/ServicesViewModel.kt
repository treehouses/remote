package io.treehouses.remote.ui.services

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.JsonParseException
import io.treehouses.remote.R
import io.treehouses.remote.bases.FragmentViewModel
import io.treehouses.remote.pojo.ServiceInfo
import io.treehouses.remote.pojo.ServicesData
import io.treehouses.remote.pojo.enum.Resource
import io.treehouses.remote.pojo.enum.Status.LOADING
import io.treehouses.remote.pojo.enum.Status.SUCCESS
import io.treehouses.remote.utils.*

class ServicesViewModel(application: Application) : FragmentViewModel(application) {

    /**
     * Private services JSON value that contains the raw string it retrieves from the Raspberry Pi
     * That upon receiving, should be parsed as ServicesData.
     * @see ServicesData
     */
    private var servicesJSON = ""

    /**
     * A boolean value indicating whether the first '{' has been detected of the JSON file
     * implying that the consequent outputs are part of the JSON string
     * @see servicesJSON
     */
    private var currentlyReceivingJSON = false

    /**
     * LiveData to fetch the cached services data
     */
    private val cacheServiceData = MutableLiveData<ServicesData>()

    /**
     * LiveData to observe the status of the Server Services result
     */
    val serverServiceData = MutableLiveData<Resource<ServicesData>>()

    /**
     * MediatorLiveData to consolidate server and cached values
     * @see serverServiceData
     * @see cacheServiceData
     */
    private val rawServicesData = MediatorLiveData<Resource<ServicesData>>()

    /**
     * Format for fast retrieval of a services info; Publicly observable
     */
    val servicesData : LiveData<Resource<HashMap<String, ServiceInfo>>> = Transformations.map(rawServicesData) {
        return@map when (it.status) {
            SUCCESS -> {
                val services = constructServiceListFromData(it.data)
                if (services.isNotEmpty()) {
                    formattedServices.clear()
                    formattedServices.addAll(formatServices(services))
                    Resource.success(services)
                }
                else Resource.error("Uh oh! Something went wrong! Please refresh.", services)
            }
            else -> Resource(it.status, hashMapOf(), it.message)
        }
    }
    var formattedServices = mutableListOf<ServiceInfo>()

    /**
     * Stores the currently selected service in the Details Screen
     */
    val selectedService = MutableLiveData<ServiceInfo>()

    /**
     * If a service was clicked in the OverView screen, this value is set
     */
    val clickedService = MutableLiveData<ServiceInfo>()

    /**
     * LiveData to observe different service actions
     */
    val serviceAction = MutableLiveData<Resource<ServiceInfo>>()
    val autoRunAction = MutableLiveData<Resource<Boolean>>()
    val getLinkAction = MutableLiveData<Resource<String>>()
    val editEnvAction = MutableLiveData<Resource<MutableList<String>>>()

    /**
     * Easy array to iterate over livedatas and update their values upon error
     */
    private val lives = listOf(serviceAction, autoRunAction, getLinkAction, editEnvAction)
    val error = MutableLiveData<String>()

    /**
     * Send the request to receive the JSON file containing all services data
     * @see R.string.TREEHOUSES_REMOTE_ALLSERVICES
     */
    fun fetchServicesFromServer() {
        sendMessage(getString(R.string.TREEHOUSES_REMOTE_ALLSERVICES))
        serverServiceData.value = Resource.loading()
    }

    /**
     * Get the services from the cache if it exists
     */
    fun fetchServicesFromCache() {
        val data = PreferenceManager.getDefaultSharedPreferences(getApplication()).getString(SERVICES_CACHE, "")
        val rawData = try {
            Gson().fromJson(data, ServicesData::class.java)
        } catch (e: Exception) { null}
        if (rawData?.available != null) {
            logE("SUCCESSFUL R CACHE GOT:$rawData")
            cacheServiceData.value = rawData!!
        }
    }

    /**
     * Saves a JSON string to the cache
     */
    fun updateServicesCache(newJSON: String) {
        logE("SAVING $newJSON")
        PreferenceManager.getDefaultSharedPreferences(getApplication()).edit().putString(SERVICES_CACHE, newJSON).apply()
    }

    /**
     * Ideally called as a result of one of two actions:
     *  1. Getting all the services data
     *  2. A specific service action was triggered
     */
    override fun onRead(output: String) {
        logE("GOT $output")
        when {
            match(output) == RESULTS.ERROR && !output.contains("kill [") && !output.contains("tput: No") -> {       //kill to fix temporary issue
                error.value = output
                lives.forEach { it.value = Resource.nothing() }
            }
            editEnvAction.value?.status == LOADING && output.startsWith("treehouses services") -> editEnvAction.value = Resource.success(output.split(" ").toMutableList())
            serverServiceData.value?.status == LOADING -> receiveJSON(output)
            serviceAction.value?.status == LOADING && containsServiceAction(output) -> matchServiceAction(output.trim())
            autoRunAction.value?.status == LOADING && output.contains("service autorun set") -> {
                autoRunAction.value = Resource.success(output.contains("true"))
            }
            getLinkAction.value?.status == LOADING && isURL(output) -> getLinkAction.value = Resource.success(output)
            getLinkAction.value?.status == LOADING && output.contains("returns nothing") -> {
                error.value = "Please start Tor"
                getLinkAction.value = Resource.nothing()
            }
        }
    }

    /**
     * Receive JSON All Services data
     * @param current : String = the current line of output
     */
    private fun receiveJSON(current: String) {
        servicesJSON += current
        if (currentlyReceivingJSON && servicesJSON.trim().endsWith("}}")) {
            try {
                logE("GOT $servicesJSON")
                val data = Gson().fromJson(servicesJSON, ServicesData::class.java)
                serverServiceData.value = if (data != null) {
                    updateServicesCache(servicesJSON)
                    Resource.success(data)
                } else Resource.error("Unknown Error")
            } catch (e: JsonParseException) {
                serverServiceData.value = Resource.error("Error in receiving Services Data ")
            }
            currentlyReceivingJSON = false
        }
        else if (current.startsWith("{") && !currentlyReceivingJSON) {
            servicesJSON = current
            currentlyReceivingJSON = true
        }
    }

    /**
     * For a service state change, update the corresponding service status
     * @param output : String = current line of output
     */
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

    /**
     * Called to Install or delete a service
     * @param service : ServiceInfo = Selected service to perform action
     */
    fun onInstallClicked(service: ServiceInfo) {
        selectedService.value = service

        if (service.serviceStatus == ServiceInfo.SERVICE_AVAILABLE)
            sendMessage(getString(R.string.TREEHOUSES_SERVICES_INSTALL, service.name))
        else if (service.serviceStatus == ServiceInfo.SERVICE_INSTALLED || service.serviceStatus == ServiceInfo.SERVICE_RUNNING)
            sendMessage(getString(R.string.TREEHOUSES_SERVICES_CLEANUP, service.name))

        serviceAction.value = Resource.loading(service)
    }

    /**
     * Called to stop or start a service
     * @param service : ServiceInfo = The service to switch the running/stopped state
     *
     */
    fun onStartClicked(service: ServiceInfo) {
        selectedService.value = service

        if (service.serviceStatus == ServiceInfo.SERVICE_INSTALLED)
            sendMessage(getString(R.string.TREEHOUSES_SERVICES_UP, service.name))
        else if (service.serviceStatus == ServiceInfo.SERVICE_RUNNING)
            sendMessage(getString(R.string.TREEHOUSES_SERVICES_STOP, service.name))

        serviceAction.value = Resource.loading(service)
    }

    /**
     * Called to edit a service Environment Configuration
     * @param service : ServiceInfo = Clicked
     */
    fun editEnvVariableRequest(service: ServiceInfo) {
        sendMessage("treehouses services " + service.name + " config edit request")
        editEnvAction.value = Resource.loading()
    }

    /**
     * Called to switch the autorun boolean
     * @param service : ServiceInfo = Service to switch
     * @param newValue : Boolean = the new value of the desired autorun config
     */
    fun switchAutoRun(service: ServiceInfo, newValue: Boolean) {
        sendMessage(getString(R.string.TREEHOUSES_SERVICES_AUTORUN, service.name, newValue.toString()))
        autoRunAction.value = Resource.loading()
    }

    /**
     * Get the service's local URL link. Can be opened with any web browser.
     * @param service : ServiceInfo = Service clicked
     */
    fun getLocalLink(service: ServiceInfo) {
        sendMessage(getString(R.string.TREEHOUSES_SERVICES_URL_LOCAL, service.name))
        getLinkAction.value = Resource.loading()
    }

    /**
     * Retrieves the service's Tor link. Upon receiving this value, the observer
     * should attempt to open the Tor Browser app
     * @param service : ServiceInfo = Service clicked
     */
    fun getTorLink(service: ServiceInfo) {
        sendMessage(getString(R.string.TREEHOUSES_SERVICES_URL_TOR, service.name))
        getLinkAction.value = Resource.loading()
    }

    /**
     * When the viewModel is cleared, make sure to save the most recent Service data
     */
    override fun onCleared() {
        super.onCleared()
        if (error.value.isNullOrEmpty() && rawServicesData.value != null) {
            updateServicesCache(Gson().toJson(rawServicesData.value!!.data))
        }
    }

    /**
     * Add the sources for the raw Services Data
     * Pull from the server, and the local cache
     */
    init {
        rawServicesData.addSource(serverServiceData) {
            rawServicesData.value = it
        }
        rawServicesData.addSource(cacheServiceData) {
            if (it != null && rawServicesData.value?.data == null) {
                rawServicesData.value = Resource.success(it)
            }
        }
    }
    companion object {
        const val SERVICES_CACHE = "services_cache"
    }
}