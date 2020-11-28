package io.treehouses.remote.ui.discover

import android.app.Application
import android.view.View
import androidx.lifecycle.MutableLiveData
import io.treehouses.remote.R
import io.treehouses.remote.bases.FragmentViewModel
import io.treehouses.remote.fragments.DiscoverFragment
import io.treehouses.remote.utils.logD
import io.treehouses.remote.utils.logE
import kotlinx.android.synthetic.main.activity_discover_fragment.view.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class DiscoverViewModel(application: Application) : FragmentViewModel(application) {
    val isLoading1: MutableLiveData<Boolean> = MutableLiveData()
    val isLoading2: MutableLiveData<Boolean> = MutableLiveData()
    val deviceContainer: MutableLiveData<Boolean> = MutableLiveData()
    val swiperefreshEnabled: MutableLiveData<Boolean> = MutableLiveData()
    val isRefreshing: MutableLiveData<Boolean> = MutableLiveData()
    val gatewayIconVisible: MutableLiveData<Boolean> = MutableLiveData()
    val deviceContainerVisible: MutableLiveData<Boolean> = MutableLiveData()
    val deviceContainerWidth: MutableLiveData<Float> = MutableLiveData()
    val deviceContainerHeight: MutableLiveData<Float> = MutableLiveData()



    fun onLoad()
    {
        loadBT()
        isLoading1.value = true
        isLoading2.value = true
        gatewayIconVisible.value = false
        isRefreshing.value = false
        swiperefreshEnabled.value = false
        requestNetworkInfo()
    }

    private fun requestNetworkInfo() {
        logD("Requesting Network Information")
        try {
            sendMessage(getString(R.string.TREEHOUSES_DISCOVER_GATEWAY_LIST))
            sendMessage(getString(R.string.TREEHOUSES_DISCOVER_GATEWAY))
            sendMessage(getString(R.string.TREEHOUSES_DISCOVER_SELF))
        } catch (e: Exception) {
            logE("Error Requesting Network Information")
        }
    }

    fun onRead(output: String, addDevices: Boolean, updateGatewayInfo: Boolean, updatePiInfo: Boolean) {
        super.onRead(output)
        logD("READ = $output")
        if(!addDevices)
            if(!updateGatewayInfo)
                updatePiInfo




    }



    override fun onWrite(input: String) {
        super.onWrite(input)
        logD("WRITE $input")
    }

    fun onTransition()
    {
        swiperefreshEnabled.value = true
        isLoading1.value = false
        isLoading1.value = false
        deviceContainerVisible.value = true
    }




}