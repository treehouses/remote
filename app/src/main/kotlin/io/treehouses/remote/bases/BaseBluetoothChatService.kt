package io.treehouses.remote.bases

import android.app.Notification
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import io.treehouses.remote.Constants
import io.treehouses.remote.InitialActivity
import io.treehouses.remote.R
import java.io.Serializable
import java.util.*

open class BaseBluetoothChatService @JvmOverloads constructor(handler: Handler? = null, applicationContext: Context? = null) : Service(), Serializable {
    var mDevice: BluetoothDevice? = null
    var context: Context? = null
    var mNewState: Int
    var bNoReconnect = false
    var state: Int

    val mAdapter: BluetoothAdapter? by lazy {
        (applicationContext?.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter
    }

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

    protected fun startNotification() {
        context?.let {
            val disconnectIntent = Intent(DISCONNECT_ACTION)
            val disconnectPendingIntent = PendingIntent.getBroadcast(this, 0, disconnectIntent, FLAG_IMMUTABLE)

            val onClickIntent = Intent(this, InitialActivity::class.java)
            val pendingClickIntent = PendingIntent.getActivity(this, 0, onClickIntent, FLAG_IMMUTABLE)

            val notificationBuilder: NotificationCompat.Builder =
                NotificationCompat.Builder(this, getString(R.string.bt_notification_ID))
            val notification: Notification = notificationBuilder.setOngoing(true)
                .setContentTitle("Treehouses Remote is currently running")
                .setContentText("Connected to ${mDevice?.name}")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setSmallIcon(R.drawable.treehouses2)
                .setContentIntent(pendingClickIntent)
                .addAction(R.drawable.bluetooth, "Disconnect", disconnectPendingIntent)
                .build()
            ServiceCompat.startForeground(this, 2,
                notification,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
                } else {
                    0
                }
            )
        }
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
        this.context = applicationContext
    }

    companion object {
        const val TAG = "BluetoothChatService"
        const val DISCONNECT_ACTION = "disconnect"
        val MY_UUID_SECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        var mHandler: Handler? = null
    }
}
