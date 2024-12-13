package io.treehouses.remote.network

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.*
import androidx.annotation.RequiresApi
import androidx.preference.PreferenceManager
import io.treehouses.remote.Constants
import io.treehouses.remote.bases.BaseBluetoothChatService
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class BluetoothChatService @JvmOverloads constructor(handler: Handler? = null, applicationContext: Context? = null) : BaseBluetoothChatService(handler, applicationContext) {
    inner class DisconnectReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            if (action == DISCONNECT_ACTION) {
                stop()
            }
        }
    }

    private val mBinder = LocalBinder()
    private var mConnectThread: ConnectThread? = null
    private var mConnectedThread: ConnectedThread? = null
    private val receiver = DisconnectReceiver()

    @Synchronized
    override fun start() {
        bNoReconnect = false
        mConnectThread?.cancel()
        mConnectThread = null
        mConnectedThread?.cancel()
        mConnectedThread = null
        updateUserInterfaceTitle()
    }

    fun updateHandler(handler: Handler) {
        mHandler = handler
    }

    override fun onBind(intent: Intent?): IBinder {
        return mBinder
    }

    inner class LocalBinder : Binder() {
        val service: BluetoothChatService
            get() = this@BluetoothChatService
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        val i = IntentFilter()
        i.addAction(DISCONNECT_ACTION)
        registerReceiver(receiver, i, RECEIVER_NOT_EXPORTED)
    }

    override fun onDestroy() {
        stop()
        unregisterReceiver(receiver)
        super.onDestroy()
    }

    var connectedDeviceName: String? = ""

    @Synchronized
    fun connect(device: BluetoothDevice?, secure: Boolean) {
        if (state == Constants.STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread?.cancel()
                mConnectThread = null
            }
        }
        if (mConnectedThread != null) {
            mConnectedThread?.cancel()
            mConnectedThread = null
        }

        mConnectThread = ConnectThread(device, secure)
        mConnectThread?.start()
        updateUserInterfaceTitle()
    }

    @Synchronized
    fun connected(socket: BluetoothSocket?, device: BluetoothDevice?) {
        connectedDeviceName = device?.name
        mDevice = device
        if (mConnectThread != null) {
            mConnectThread?.cancel()
            mConnectThread = null
        }

        if (mConnectedThread != null) {
            mConnectedThread?.cancel()
            mConnectedThread = null
        }

        startNotification()
        mConnectedThread = ConnectedThread(socket)
        mConnectedThread?.start()

        updateUserInterfaceTitle()
        val msg = mHandler?.obtainMessage(Constants.MESSAGE_DEVICE_NAME)
        val bundle = Bundle()
        bundle.putString(Constants.DEVICE_NAME, device?.name)
        msg?.data = bundle
        mHandler?.sendMessage(msg ?: Message())
    }

    @Synchronized
    fun stop() {
        bNoReconnect = true
        if (mConnectThread != null) {
            mConnectThread?.cancel()
            mConnectThread = null
        }
        if (mConnectedThread != null) {
            mConnectedThread?.cancel()
            mConnectedThread = null
        }

        state = Constants.STATE_NONE
        stopForeground(true)
        updateUserInterfaceTitle()
    }

    fun write(out: ByteArray?) {
        var r: ConnectedThread?
        synchronized(this) {
            if (state != Constants.STATE_CONNECTED) return
            r = mConnectedThread
        }
        if (out != null) {
            r?.write(out)
        }
    }

    private fun connectionLost() {
        callHandler("Device connection was lost")
        stopForeground(true)

        val preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        if (mDevice != null && !bNoReconnect && preferences.getBoolean("reconnectBluetooth", true)) {
            connect(mDevice, true)
        } else {
            state = Constants.STATE_NONE
            updateUserInterfaceTitle()
            start()
        }
    }

    private inner class ConnectThread(private val mmDevice: BluetoothDevice?, secure: Boolean) : Thread() {
        private val mmSocket: BluetoothSocket?
        private val mSocketType: String
        override fun run() {
            name = "ConnectThread$mSocketType"
            this@BluetoothChatService.state = Constants.STATE_CONNECTING
            mAdapter?.cancelDiscovery()
            try {
                mmSocket?.connect()
            } catch (e: Exception) {
                e.printStackTrace()
                closeSocket()
                connectionFailed()
                return
            }
            synchronized(this@BluetoothChatService) { mConnectThread = null }
            connected(mmSocket, mmDevice)
        }

        fun cancel() { closeSocket() }

        fun closeSocket() {
            try { mmSocket?.close() }
            catch (e: Exception) { e.printStackTrace() }
        }

        init {
            var tmp: BluetoothSocket? = null
            mSocketType = if (secure) "Secure" else "Insecure"
            try {
                tmp = mmDevice?.createRfcommSocketToServiceRecord(MY_UUID_SECURE)
                this@BluetoothChatService.state = Constants.STATE_CONNECTING
            } catch (e: Exception) {
                e.printStackTrace()
                this@BluetoothChatService.state = Constants.STATE_NONE
            }
            this@BluetoothChatService.updateUserInterfaceTitle()
            mmSocket = tmp
        }
    }

    private inner class ConnectedThread(socket: BluetoothSocket?) : Thread() {
        private val mmSocket: BluetoothSocket? = socket
        private val mmInStream: InputStream?
        private val mmOutStream: OutputStream?
        override fun run() {
            val buffer = ByteArray(10000)
            var bytes: Int
            var out: String
            while (true) {
                try {
                    bytes = mmInStream?.read(buffer) ?: 0
                    out = String(buffer, 0, bytes)
                    mHandler?.obtainMessage(Constants.MESSAGE_READ, bytes, -1, out)?.sendToTarget()
                } catch (e: IOException) {
                    e.printStackTrace()
                    connectionLost()
                    break
                }
            }
        }

        fun write(buffer: ByteArray) {
            try {
                mmOutStream?.write(buffer)
                mHandler?.obtainMessage(Constants.MESSAGE_WRITE, -1, -1, buffer)?.sendToTarget()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        fun cancel() {
            try {
                mmSocket?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        init {
            var tmpIn: InputStream? = null
            var tmpOut: OutputStream? = null
            try {
                tmpIn = socket?.inputStream
                tmpOut = socket?.outputStream
            } catch (e: IOException) {
                e.printStackTrace()
            }
            mmInStream = tmpIn
            mmOutStream = tmpOut
            this@BluetoothChatService.state = Constants.STATE_CONNECTED
            this@BluetoothChatService.updateUserInterfaceTitle()
        }
    }
}