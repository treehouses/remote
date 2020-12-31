package io.treehouses.remote.ui.socks

import android.app.Application
import android.app.Dialog
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import android.widget.Toast.*
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.MutableLiveData
import io.treehouses.remote.Constants
import io.treehouses.remote.MainApplication
import io.treehouses.remote.R
import io.treehouses.remote.bases.FragmentViewModel
import io.treehouses.remote.utils.logD

class SocksViewModel (application: Application) : FragmentViewModel(application){
    val addProfileButtonText: MutableLiveData<String> = MutableLiveData()
    val addProfileButtonEnbaled: MutableLiveData<Boolean> = MutableLiveData()
    val textStatusText: MutableLiveData<String> = MutableLiveData()
    val startButtonText: MutableLiveData<String> = MutableLiveData()
    val startButtonEnabled: MutableLiveData<Boolean> = MutableLiveData()
    val dialogText: MutableLiveData<String> = MutableLiveData()
    val passwordText: MutableLiveData<String> = MutableLiveData()
    val serverPortText: MutableLiveData<String> = MutableLiveData()
    val localPortText: MutableLiveData<String> = MutableLiveData()
    val localAddressText: MutableLiveData<String> = MutableLiveData()
    val serverHostText: MutableLiveData<String> = MutableLiveData()
    val profileNameText: MutableLiveData<ArrayList<String>> = MutableLiveData()
    val profilesAdapter: MutableLiveData<ArrayAdapter<String>> = MutableLiveData()
    private var adapter: ArrayAdapter<String>? = null

    fun onLoad()
    {
        loadBT()

    }

    override fun onRead(output: String) {
        super.onRead(output)
        logD("SOCKS MESSAGE " + output)


            if (output.contains("inactive")) {
                serverHostText.value = "-"; startButtonText.value = "Start Tor"
                startButtonEnabled.value = true
                sendMessage(getString(R.string.TREEHOUSES_TOR_NOTICE))
            }
            else if(output.contains("Error when")){
                profileNameText.value = ArrayList()
                sendMessage("treehouses shadowsocks list")
            }
            else if(output.contains("Use `treehouses shadowsock")){
                addProfileButtonText.value = "Add Profile"
                addProfileButtonEnbaled.value = true
                profileNameText.value = ArrayList()
                sendMessage("treehouses shadowsocks list")
            }
            else{
                getMessage2(output)
            }

    }

    private fun getMessage2(readMessage: String) {
        if(readMessage.contains("removed")){
            makeText(MainApplication.context, "Removed, retrieving list again", LENGTH_SHORT).show()
            profileNameText.value = ArrayList()
            sendMessage("treehouses shadowsocks list")
        }
        else if (readMessage.contains("tmptmp") && !readMessage.contains("disabled") && !readMessage.contains("stopped")){

            if(readMessage.contains(' '))
                profileNameText.value?.add(readMessage.split(' ')[0])
            else
                profileNameText.value?.add(readMessage)
        }
    }
}