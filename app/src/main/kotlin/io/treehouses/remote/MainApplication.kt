package io.treehouses.remote

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ProcessLifecycleOwner
import com.parse.Parse
import io.treehouses.remote.network.BluetoothChatService
import io.treehouses.remote.utils.AppLifecycleObserver
import io.treehouses.remote.utils.AppLifecycleTracker
import io.treehouses.remote.utils.GPSService
import io.treehouses.remote.utils.SaveUtils

class MainApplication : Application() {
    var logSent = false
    private lateinit var appLifecycleObserver: AppLifecycleObserver
    private lateinit var activityLifecycleTracker: AppLifecycleTracker
//    private var bluetoothService: BluetoothChatService? = null

    override fun onCreate() {
        super.onCreate()

        context = this
        createNotificationChannel()
        startBluetoothService()
        terminalList = ArrayList()
        tunnelList = ArrayList()
        commandList = ArrayList()
        Parse.initialize(
            Parse.Configuration.Builder(this)
                .applicationId(Constants.PARSE_APPLICATION_ID)
                .clientKey(null)
                .server(Constants.PARSE_URL)
                .build()
        )
        SaveUtils.initCommandsList(applicationContext)

        appLifecycleObserver = AppLifecycleObserver()
        ProcessLifecycleOwner.get().lifecycle.addObserver(appLifecycleObserver)

        activityLifecycleTracker = AppLifecycleTracker()
        registerActivityLifecycleCallbacks(activityLifecycleTracker)
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as BluetoothChatService.LocalBinder
            mChatService = binder.service
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mChatService = null
        }
    }

    fun getCurrentBluetoothService(): BluetoothChatService? {
        if (mChatService == null) {
            mChatService = BluetoothChatService()
        }
        return mChatService
    }

    fun startBluetoothService() {
        Intent(this, BluetoothChatService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    fun stopBluetoothService() {
        try {
            unbindService(connection)
            mChatService = null
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val bluetoothIntent = Intent(this, BluetoothChatService::class.java)
        stopService(bluetoothIntent)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val bluetoothChannel = NotificationChannel(
                getString(R.string.bt_notification_ID),
                getString(R.string.bt_notification_channel),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.bt_notification_description)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PRIVATE
            }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannels(listOf(bluetoothChannel))
        }
    }

    fun stopAllServices() {
        stopBluetoothService()
        val gpsIntent = Intent(this, GPSService::class.java)
        stopService(gpsIntent)
        Log.d("MainApplication", "All services stopped")
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
        lateinit var context: Context
        var mChatService: BluetoothChatService? = null
    }
}