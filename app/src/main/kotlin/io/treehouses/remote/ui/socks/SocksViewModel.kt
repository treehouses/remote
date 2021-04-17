package io.treehouses.remote.ui.socks

import android.app.AlertDialog
import android.app.Application
import android.view.ContextThemeWrapper
import android.widget.ArrayAdapter
import android.widget.Toast
import android.widget.Toast.*
import androidx.lifecycle.MutableLiveData
import io.treehouses.remote.MainApplication
import io.treehouses.remote.MainApplication.Companion.context
import io.treehouses.remote.R
import io.treehouses.remote.bases.FragmentViewModel
import io.treehouses.remote.utils.logD

class SocksViewModel (application: Application) : FragmentViewModel(application){
    val addProfileButtonText: MutableLiveData<String> = MutableLiveData()
    val addProfileButtonEnabled: MutableLiveData<Boolean> = MutableLiveData()
    val textStatusText: MutableLiveData<String> = MutableLiveData()
    val startButtonText: MutableLiveData<String> = MutableLiveData()
    val startButtonEnabled: MutableLiveData<Boolean> = MutableLiveData()
    val passwordText: MutableLiveData<String> = MutableLiveData()
    val localPortText: MutableLiveData<String> = MutableLiveData()
    val localAddressText: MutableLiveData<String> = MutableLiveData()
    val serverHostText: MutableLiveData<String> = MutableLiveData()
    val profileNameText: MutableLiveData<ArrayList<String>> = MutableLiveData()
    val profileDialogDismiss: MutableLiveData<Boolean> = MutableLiveData()

    fun onLoad()
    {
        loadBT()

    }

    override fun onRead(output: String) {
        super.onRead(output)
        logD("SOCKS MESSAGE " + output)


            if (output.contains("inactive")) {
                textStatusText.value = "-"; startButtonText.value = "Start Tor"
                startButtonEnabled.value = true
                sendMessage(getString(R.string.TREEHOUSES_TOR_NOTICE))
            }
            else if(output.contains("Error when")){
                profileNameText.value = ArrayList()
                sendMessage("treehouses shadowsocks list")
            }
            else if(output.contains("Use `treehouses shadowsock")){
                addProfileButtonText.value = "Add Profile"
                addProfileButtonEnabled.value = true
                profileNameText.value = ArrayList()
                sendMessage("treehouses shadowsocks list")
            }
            else{
                getMessage2(output)
            }

    }

    fun portListListener()
    {
        val builder = AlertDialog.Builder(ContextThemeWrapper(context, R.style.CustomAlertDialogStyle))
        val selectedString = profileNameText.value
        builder.setTitle("Delete Profile $selectedString ?")
        builder.setPositiveButton("Confirm") { dialog, _ ->
            sendMessage("treehouses shadowsocks remove $selectedString ")
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel", null)

        // create and show the alert dialog
        val dialog = builder.create()
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
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

    fun addProfile(stringMap: Map<String, String>){
        profileDialogDismiss.value = false

        val serverHost = stringMap.getValue("serverHost")
        val localAddress = stringMap.getValue("localAddress")
        val localPort = stringMap.getValue("localPort")
        val serverPort = stringMap.getValue("serverPort")
        val password = stringMap.getValue("password")
        if (serverHost.isNotEmpty() && localAddress.isNotEmpty() && localPort.isNotEmpty() && serverPort.isNotEmpty() && password.isNotEmpty()) {

            val message = "treehouses shadowsocks add { \\\"server\\\": \\\"$serverHost\\\", \\\"local_address\\\": \\\"$localAddress\\\", \\\"local_port\\\": $localPort, \\\"server_port\\\": $serverPort, \\\"password\\\": \\\"$password\\\", \\\"method\\\": \\\"rc4-md5\\\" }"
            sendMessage(message)
            addProfileButtonText.value = "Adding......"
            addProfileButtonEnabled.value = false

            profileDialogDismiss.value = true
        } else {
            makeText(context, "Missing Information", LENGTH_SHORT).show()
        }
    }

    fun listenerInitialized(){
        profileNameText.value = ArrayList()
        sendMessage("treehouses shadowsocks list")
    }
}