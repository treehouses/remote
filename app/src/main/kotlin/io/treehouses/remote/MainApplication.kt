package io.treehouses.remote

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.Configuration
import android.os.Build
import android.os.IBinder
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import androidx.preference.SeekBarPreference
import com.parse.Parse
import io.treehouses.remote.network.BluetoothChatService
import io.treehouses.remote.utils.SaveUtils
import io.treehouses.remote.utils.logD
import io.treehouses.remote.utils.logE
import java.util.*

class MainApplication : Application() {
    var logSent = false
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    override fun onCreate() {
        super.onCreate()
        adjustFontScale(resources.configuration)
        context = this
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
    }

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            logD("Bluetooth Service CONNECTED")
            val binder = service as BluetoothChatService.LocalBinder
            mChatService = binder.service
//            sendBroadcast(Intent().setAction(BLUETOOTH_SERVICE_CONNECTED))
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mChatService = null
            logE("Bluetooth Service DISCONNECTED")
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
            logE("Unbinding Service Bluetooth Service")
            try {unbindService(connection)} catch (e: Exception) {}
        }

    }


    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun adjustFontScale(configuration: Configuration?) {
        configuration?.let {
            it.fontScale = 5000.0F
            val metrics: DisplayMetrics = resources.displayMetrics
            val wm: WindowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            wm.defaultDisplay.getMetrics(metrics)
            metrics.scaledDensity = configuration.fontScale * metrics.density

            baseContext.applicationContext.createConfigurationContext(it)
            baseContext.resources.displayMetrics.setTo(metrics)

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
        lateinit var context:Context

        var mChatService : BluetoothChatService? = null
    }
}