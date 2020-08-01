package io.treehouses.remote

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.parse.Parse
import io.treehouses.remote.Network.BluetoothChatService
import io.treehouses.remote.utils.SaveUtils
import java.util.*

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Intent(this, BluetoothChatService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
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
            Log.e("Bluetooth Service", "CONNECTED")
            val binder = service as BluetoothChatService.LocalBinder
            mChatService = binder.service
//            sendBroadcast(Intent().setAction(BLUETOOTH_SERVICE_CONNECTED))
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            Log.e("Bluetooth Service", "DISCONNECTED")
        }
    }

    fun getCurrentBluetoothService() : BluetoothChatService? {
        return mChatService
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

    }
}