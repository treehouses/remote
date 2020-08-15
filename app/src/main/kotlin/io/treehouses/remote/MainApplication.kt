package io.treehouses.remote

import android.app.Activity
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import com.parse.Parse
import io.treehouses.remote.Network.BluetoothChatService
import io.treehouses.remote.utils.SaveUtils
import java.util.*

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startBluetoothService()
        terminalList = ArrayList()
        tunnelList = ArrayList()
        commandList = ArrayList()
        Parse.initialize(Parse.Configuration.Builder(this)
                .applicationId(Constants.PARSE_APPLICATION_ID)
                .clientKey(null)
                .server(Constants.PARSE_URL)
                .build()
        )
        SaveUtils.initCommandsList(applicationContext)
        registerLifecycleCallbacks()
    }

    private fun registerLifecycleCallbacks() {
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityDestroyed(activity: Activity) {
                Log.e("ACTIVITY", "DESTROYED. Remaining: $numActivities")
                numActivities--
                if (numActivities <= 0) stopBluetoothService()
            }
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                Log.e("ACTIVITY", "CREATED. Number: $numActivities")
                numActivities++
            }
            override fun onActivityResumed(activity: Activity) {}
        })
    }

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            Log.e("Bluetooth Service", "CONNECTED")
            val binder = service as BluetoothChatService.LocalBinder
            mChatService = binder.service
//            sendBroadcast(Intent().setAction(BLUETOOTH_SERVICE_CONNECTED))
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mChatService = null
            Log.e("Bluetooth Service", "DISCONNECTED")
        }
    }

    fun getCurrentBluetoothService() : BluetoothChatService? {
        return mChatService
    }

    fun startBluetoothService() {
        Intent(this, BluetoothChatService::class.java).also { intent -> bindService(intent, connection, Context.BIND_AUTO_CREATE) }
    }

    fun stopBluetoothService() {
        if (!PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Constants.KEEP_BLUETOOTH_ALIVE, false)) {
            Log.e("Unbinding Service", "Bluetooth Service")
            try {unbindService(connection)} catch (e: Exception) {}
        }

    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val bluetoothChannel = NotificationChannel(
                    getString(R.string.bt_notification_ID),
                    getString(R.string.bt_notification_channel),
                    NotificationManager.IMPORTANCE_HIGH).apply {
                description = getString(R.string.bt_notification_description)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PRIVATE
            }

            // Register the channel with the system
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannels(listOf(bluetoothChannel))
        }
    }

    companion object {
        const val BLUETOOTH_SERVICE_CONNECTED = "BLUETOOTH_SERVICE_CONNECTED"
        @JvmStatic
        var terminalList: ArrayList<String>? = null
            private set
        @JvmStatic
        var tunnelList: ArrayList<String>? = null
            private set
        @JvmStatic
        lateinit var commandList: ArrayList<String>
            private set
        @JvmField
        var showLogDialog = true
        @JvmField
        var ratingDialog = true

        var logSent = false

        var mChatService : BluetoothChatService? = null

        var numActivities = 0

    }
}