package io.treehouses.remote.ui.services

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.google.gson.Gson
import com.google.gson.JsonParseException
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

    var servicesJSON = ""
    var currentlyReceivingJSON = false

    private val rawServicesData = MutableLiveData<Resource<ServicesData>>()
    val servicesData : LiveData<Resource<HashMap<String, ServiceInfo>>> = Transformations.map(rawServicesData) {
        return@map when (it.status) {
            SUCCESS -> {
                val services = constructServiceListFromData(it.data)
                if (services.isNotEmpty()) Resource.success(services)
                else Resource.error("Uh oh! Something went wrong! Please refresh.", services)
            }
            else -> Resource(it.status, hashMapOf(), it.message)
        }
    }

    val formattedServices = Transformations.map(servicesData) {
        return@map when(it.status) {
            SUCCESS -> Resource.success(formatServices(it?.data!!))
            else -> Resource(it.status, mutableListOf(), it.message)
        }
    }

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

    override fun onRead(output: String) {
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
}