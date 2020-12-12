package io.treehouses.remote.ui.socks

import android.app.Application
import android.app.Dialog
import android.widget.EditText
import androidx.lifecycle.MutableLiveData
import io.treehouses.remote.bases.FragmentViewModel

class SocksViewModel (application: Application) : FragmentViewModel(application){
    val addProfileButtonText: MutableLiveData<String> = MutableLiveData()
    val addProfileButtonEnbaled: MutableLiveData<Boolean> = MutableLiveData()
    val textStatusText: MutableLiveData<String> = MutableLiveData()
    val startButtonText: MutableLiveData<String> = MutableLiveData()
    val startButtonEnabled: MutableLiveData<Boolean> = MutableLiveData()
    val dialogText: MutableLiveData<Dialog> = MutableLiveData()
    val passwordText: MutableLiveData<EditText> = MutableLiveData()
    val serverPortText: MutableLiveData<EditText> = MutableLiveData()
    val localPortText: MutableLiveData<EditText> = MutableLiveData()
    val localAddressText: MutableLiveData<EditText> = MutableLiveData()
    val serverHostText: MutableLiveData<EditText> = MutableLiveData()

    fun onLoad()
    {
        loadBT()

    }
}