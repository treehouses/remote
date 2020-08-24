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

//    lateinit var viewModel: ServicesViewModel


//    fun getViewModel() {
//        viewModel = activity?.run {ViewModelProvider(this)[ServicesViewModel::class.java]}!!
//    }

    protected fun openLocalURL(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://$url"))
        Log.d("OPENING: ", "http://$url||")
        val title = "Select a browser"
        val chooser = Intent.createChooser(intent, title)
        if (intent.resolveActivity(requireContext().packageManager) != null) startActivity(chooser)
    }

    protected fun writeToRPI(ping: String) {
//        mChatService.write(ping.toByteArray())
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

//    protected fun installedOrRunning(selected: ServiceInfo): Boolean {
//        return selected.serviceStatus == ServiceInfo.SERVICE_INSTALLED || selected.serviceStatus == ServiceInfo.SERVICE_RUNNING
//    }

    protected fun indexOfService(name: String, services: MutableList<ServiceInfo>): Int {
        for (i in services.indices) {
            if (services[i].name == name) return i
        }
        return -1
    }

    private fun isError(output: String): Int {
        return if (match(output) === RESULTS.ERROR) 0 else -1
    }


}