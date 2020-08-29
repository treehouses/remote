package io.treehouses.remote.bases

import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.util.Log
import io.treehouses.remote.Constants
import io.treehouses.remote.Network.BluetoothChatService
import java.io.Serializable
import java.util.*

open class BaseBluetoothChatService @JvmOverloads constructor(handler: Handler? = null, applicationContext: Context? = null) : Service(), Serializable {

    var mDevice: BluetoothDevice? = null
    var context: Context?
    var mNewState: Int
    var bNoReconnect = false
    //    private AcceptThread mSecureAcceptThread;
    //private AcceptThread mInsecureAcceptThread;

    /**
     * Return the current connection state.
     */
    @get:Synchronized
    var state: Int
        set

    val mAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    fun connectionFailed() {
        // Send a failure message back to the Activity
        callHandler("Unable to connect to device")
        mHandler?.obtainMessage(Constants.MESSAGE_ERROR, "Error while connecting; Unable to connect to device")?.sendToTarget()

        state = Constants.STATE_NONE
        // Update UI title
        updateUserInterfaceTitle()

        // Start the service over to restart listening mode
        start()
    }

    open fun start() {}



    /**
     * Update UI title according to the current state of the chat connection
     */
    @Synchronized
    fun updateUserInterfaceTitle() {
        Log.e(TAG, "updateUserInterfaceTitle() $mNewState -> $state")
        if (mNewState != state) mHandler?.sendMessage(mHandler!!.obtainMessage(Constants.MESSAGE_STATE_CHANGE, state, -1))
        mNewState = state
    }

    fun callHandler(message: String?) {
        val msg = mHandler?.obtainMessage(Constants.MESSAGE_TOAST)
        val bundle = Bundle()
        bundle.putString(Constants.TOAST, message)
        msg?.data = bundle
        mHandler?.sendMessage(msg ?: Message())
    }


    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    /**
     * Constructor. Prepares a new BluetoothChat session.
     *
     * The UI Activity Context
     * @param handler A Handler to send messages back to the UI Activity
     */
    init {
        state = Constants.STATE_NONE
        mNewState = state
        mHandler = handler
        context = applicationContext
    }

    companion object {
        // Debugging
        const val TAG = "BluetoothChatService"
        const val DISCONNECT_ACTION = "disconnect"
        //private static final String NAME_INSECURE = "BluetoothChatInsecure";
        // well-known SPP UUID 00001101-0000-1000-8000-00805F9B34FB
        val MY_UUID_SECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        var mHandler: Handler? = null

    }
    //    private BluetoothSocket socket = null;

}