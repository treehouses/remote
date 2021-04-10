package io.treehouses.remote.bases

import android.annotation.SuppressLint
import android.app.Application
import android.os.Handler
import android.os.Message
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.treehouses.remote.Constants
import io.treehouses.remote.MainApplication
import io.treehouses.remote.network.BluetoothChatService
import io.treehouses.remote.utils.logE

open class FragmentViewModel(application: Application) : AndroidViewModel(application) {
    /**
     * To access bluetooth service for derived View Models
     */
    protected lateinit var mChatService : BluetoothChatService
    var lastCommand = ""
    /**
     * Monitors connection Status (has the specific connection state)
     */
    private val _connectionStatus = MutableLiveData<Int>(Constants.STATE_NONE)
    val connectionStatus : LiveData<Int>
        get() = _connectionStatus

//    /**
//     * Contains the boolean whether this device is currently connected to bluetooth or not
//     */
//    val connected : LiveData<Boolean> = Transformations.map(_connectionStatus) {
//        Log.e("UPDATED", "CONNECTED to $it")
//        return@map it == Constants.STATE_CONNECTED
//    }

    /**
     * Handler to handle all messages from the bluetooth service
     */
    protected open val mHandler: Handler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                Constants.MESSAGE_STATE_CHANGE -> {
                    if (msg.arg1 == Constants.STATE_NONE) {
                        try { Toast.makeText(application, "Bluetooth disconnected", Toast.LENGTH_LONG).show() }
                        catch (exception: NullPointerException) { logE("Error $exception") }
                    }
                    logE("RECEIVED, CONNECTION ${msg.arg1}")
                    _connectionStatus.value= msg.arg1
                }
                Constants.MESSAGE_WRITE -> onWrite(String(msg.obj as ByteArray))
                Constants.MESSAGE_READ -> {
                    if (checkPythonError(msg.obj as String)) onError(msg.obj as String)
                    else onRead(msg.obj as String)
                }
                else -> onOtherMessage(msg)
            }
            onAnyMessage(msg)
        }
    }

    fun checkPythonError(output: String): Boolean {
        return output.contains("Traceback (most recent call last): ")
    }

    /**
     * Called whenever a message is successfully delivered to the Raspberry Pi
     * @param input : String = The value that was written to the Raspberry Pi
     */
    open fun onWrite(input: String) {}

    /**
     * Called when an output is received from the Raspberry Pi
     * @param output : String = The value that was read from the Raspberry Pi
     */
    open fun onRead(output: String) {}

    /**
     * Called when an error is received from the Raspberry Pi
     * @param output : String = The value that was read from the Raspberry Pi
     */
    open fun onError(output: String) {}

    /**
     * Called when the handler message received is not a read, or a write message
     * @param msg : Message = The message that was received
     */
    open fun onOtherMessage(msg: Message) {}

    /**
     * Called when any message is received from the Bluetooth Service
     * @param msg : Message = The message that was received
     */
    open fun onAnyMessage(msg: Message) {}

    /**
     * @param toSend : String = A string to send to the Raspberry Pi
     */
    fun sendMessage(toSend: String) {
        logE("SENDING $toSend")
        lastCommand = toSend
        if (_connectionStatus.value != Constants.STATE_CONNECTED) {
            Toast.makeText(getApplication(), "Not Connected to Bluetooth", Toast.LENGTH_LONG).show()
        }
        else mChatService.write(toSend.toByteArray())
    }

    /**
     * Load the bluetooth service and update the handler and connection status
     */
    fun loadBT() {
        mChatService = getApplication<MainApplication>().getCurrentBluetoothService()!!
        mChatService.updateHandler(mHandler)
        _connectionStatus.value = mChatService.state
    }

    /**
     * Update the current handler back to the scope of this ViewModel
     */
    fun refreshHandler() {
        mChatService.updateHandler(mHandler)
        _connectionStatus.value = mChatService.state
    }

    /**
     * Disconnects from the current connected device (NOTE: this does not stop the Android Service)
     */
    fun disconnectBT() {
        mChatService.stop()
    }

    /**
     * Convenience function to get a string from resources
     * @param stringRes : Int = The resource ID to retrieve
     * @return = The resolved string resource
     */
    protected fun getString(stringRes: Int, vararg params: Any) : String {
        return getApplication<MainApplication>().getString(stringRes, *params)
    }

    init {
        loadBT()
    }
}