/*
* Copyright 2017 The Android Open Source Project, Inc.
*
* Licensed to the Apache Software Foundation (ASF) under one or more contributor
* license agreements. See the NOTICE file distributed with this work for additional
* information regarding copyright ownership. The ASF licenses this file to you under
* the Apache License, Version 2.0 (the "License"); you may not use this file except
* in compliance with the License. You may obtain a copy of the License at

* http://www.apache.org/licenses/LICENSE-2.0

* Unless required by applicable law or agreed to in writing, software distributed under
* the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF
* ANY KIND, either express or implied. See the License for the specific language
* governing permissions and limitations under the License.

*/
package io.treehouses.remote.Network

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.*
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import io.treehouses.remote.Constants
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.Serializable
import java.util.*


/**
 * Created by yubo on 7/11/17.
 */
/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 */
class BluetoothChatService @JvmOverloads constructor(handler: Handler? = null, applicationContext: Context? = null) : Service(), Serializable {
    // Member fields
    private val mAdapter: BluetoothAdapter
    private var mDevice: BluetoothDevice? = null

    //    private AcceptThread mSecureAcceptThread;
    //private AcceptThread mInsecureAcceptThread;
    private var mConnectThread: ConnectThread? = null
    private var mConnectedThread: ConnectedThread? = null

    private val mBinder = LocalBinder()

    /**
     * Return the current connection state.
     */
    @get:Synchronized
    var state: Int
        private set
    private var mNewState: Int
    private var bNoReconnect = false
    var context: Context?


    fun updateHandler(handler: Handler) {
        mHandler = handler
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.e("Bluetooth Service", "ON BIND")
        return mBinder
    }

    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        val service: BluetoothChatService
            get() = this@BluetoothChatService
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.e("BLUETOOTH","START COMMAND")
        if (intent?.action.equals("stop")) {
            stopSelf();
        }
        else if (intent != null && intent.hasExtra("DEVICE")) {
            mDevice = intent.getParcelableExtra("DEVICE")
        }
        if (mDevice != null) connect(mDevice!!, false)
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) startNotification() else startForeground(1, Notification())
    }

//    override fun onTaskRemoved(rootIntent: Intent?) {
//        super.onTaskRemoved(rootIntent)
//        Log.e("SERVICE","TASK REMOVE")
//        val broadcastIntent = Intent()
//        if (mDevice != null) broadcastIntent.putExtra("DEVICE", mDevice)
//        broadcastIntent.action = "restartservice"
//        broadcastIntent.setClass(this, BluetoothConnectionReceiver::class.java)
//        this.sendBroadcast(broadcastIntent)
//    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e("SERVICE","OnDestroy")
        val broadcastIntent = Intent()
        if (mDevice != null) broadcastIntent.putExtra("DEVICE", mDevice)
        broadcastIntent.action = "restartservice"
        broadcastIntent.setClass(this, BluetoothConnectionReceiver::class.java)
        this.sendBroadcast(broadcastIntent)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun startNotification() {
        val NOTIFICATION_CHANNEL_ID = "bluetooth"
        val channelName = "Bluetooth Connection"
        val chan = NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE)
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE

        val manager = (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
        manager.createNotificationChannel(chan)

        val notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        val notification: Notification = notificationBuilder.setOngoing(true)
                .setContentTitle("App is running in background")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build()
        startForeground(2, notification)
    }

    /**
     * Update UI title according to the current state of the chat connection
     */
    @Synchronized
    private fun updateUserInterfaceTitle() {
        state = state
        Log.d(TAG, "updateUserInterfaceTitle() " + mNewState + " -> " + state)
        mNewState = state

        // Give the new state to the Handler so the UI Activity can update
        mHandler!!.sendMessage(mHandler!!.obtainMessage(Constants.MESSAGE_STATE_CHANGE, mNewState, -1))
    }

    var connectedDeviceName: String = ""

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    @Synchronized
    fun start() {
        bNoReconnect = false
        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread!!.cancel()
            mConnectThread = null
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread!!.cancel()
            mConnectedThread = null
        }

        // Update UI title
        updateUserInterfaceTitle()
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     *
     * @param device The BluetoothDevice to connect
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    @Synchronized
    fun connect(device: BluetoothDevice, secure: Boolean) {
        Log.d(TAG, "connect to: $device")

        // Cancel any thread attempting to make a connection
        if (state == Constants.STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread!!.cancel()
                mConnectThread = null
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread!!.cancel()
            mConnectedThread = null
        }

        // Start the thread to connect with the given device
        mConnectThread = ConnectThread(device, secure)
        mConnectThread!!.start()
        // Update UI title
        updateUserInterfaceTitle()
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    @Synchronized
    fun connected(socket: BluetoothSocket?, device: BluetoothDevice, socketType: String) {
        Log.d(TAG, "connected, Socket Type:$socketType")
        connectedDeviceName = device.name
        mDevice = device
        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread!!.cancel()
            mConnectThread = null
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread!!.cancel()
            mConnectedThread = null
        }

        // Cancel the accept thread because we only want to connect to one device
//        if (mSecureAcceptThread != null) {
//            mSecureAcceptThread.cancel();
//            mSecureAcceptThread = null;
//        }
//        if (mInsecureAcceptThread != null) {
//            mInsecureAcceptThread.cancel();
//            mInsecureAcceptThread = null;
//        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = ConnectedThread(socket, socketType)
        mConnectedThread!!.start()

        // Send the name of the connected device back to the UI Activity
        updateUserInterfaceTitle()
        val msg = mHandler?.obtainMessage(Constants.MESSAGE_DEVICE_NAME)
        val bundle = Bundle()
        bundle.putString(Constants.DEVICE_NAME, device.name)
        msg?.data = bundle
        mHandler?.sendMessage(msg ?: Message())
        // Update UI title
        Log.e(TAG, "Connected")
    }

    /**
     * Stop all threads
     */
    @Synchronized
    fun stop() {
        bNoReconnect = true
        if (mConnectThread != null) {
            mConnectThread!!.cancel()
            mConnectThread = null
        }
        if (mConnectedThread != null) {
            mConnectedThread!!.cancel()
            mConnectedThread = null
        }
        
        state = Constants.STATE_NONE
        // Update UI title
        updateUserInterfaceTitle()
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread.write
     */
    fun write(out: ByteArray?) {
        // Create temporary object
        Log.d(TAG, "write: " + String(out!!))
        var r: ConnectedThread?
        // Synchronize a copy of the ConnectedThread
        synchronized(this) {
            if (state != Constants.STATE_CONNECTED) return
            r = mConnectedThread
        }
        // Perform the write unsynchronized
        r!!.write(out)
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private fun connectionFailed() {
        // Send a failure message back to the Activity
        callHandler("Unable to connect to device")
        state = Constants.STATE_NONE
        // Update UI title
        updateUserInterfaceTitle()

        // Start the service over to restart listening mode
        start()
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private fun connectionLost() {
        // Send a failure message back to the Activity
        callHandler("Device connection was lost")
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        if (mDevice != null && !bNoReconnect && preferences.getBoolean("reconnectBluetooth", true)) {
            connect(mDevice!!, true)
        } else {
            state = Constants.STATE_NONE
            // Update UI title
            updateUserInterfaceTitle()
            // Start the service over to restart listening mode
            start()
        }
    }

    fun callHandler(message: String?) {
        val msg = mHandler?.obtainMessage(Constants.MESSAGE_TOAST)
        val bundle = Bundle()
        bundle.putString(Constants.TOAST, message)
        msg?.data = bundle
        mHandler?.sendMessage(msg ?: Message())
    }
    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private inner class ConnectThread(private val mmDevice: BluetoothDevice, secure: Boolean) : Thread() {
        private val mmSocket: BluetoothSocket?
        private val mSocketType: String
        override fun run() {
            Log.i(TAG, "BEGIN mConnectThread SocketType:$mSocketType")
            name = "ConnectThread$mSocketType"

            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery()

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket!!.connect()
            } catch (e: Exception) {
                // Close the socket
                try {
                    mmSocket!!.close()
                } catch (e2: Exception) {
                    Log.e(TAG, "unable to close() " + mSocketType +
                            " socket during connection failure", e2)
                }
                connectionFailed()
                return
            }

            // Reset the ConnectThread because we're done
            synchronized(this@BluetoothChatService) { mConnectThread = null }

            // Start the connected thread
            connected(mmSocket, mmDevice, mSocketType)
        }

        fun cancel() {
            try {
                mmSocket!!.close()
            } catch (e: Exception) {
                Log.e(TAG, "close() of connect $mSocketType socket failed", e)
            }
        }

        init {
            var tmp: BluetoothSocket? = null
            mSocketType = if (secure) "Secure" else "Insecure"

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
//                if (secure) {
                tmp = mmDevice.createRfcommSocketToServiceRecord(MY_UUID_SECURE)
                //                } else {
//                    tmp = device.createInsecureRfcommSocketToServiceRecord(
//                            MY_UUID_INSECURE);
//                }
                this@BluetoothChatService.state = Constants.STATE_CONNECTING
            } catch (e: Exception) {
                Log.e(TAG, "Socket Type: " + mSocketType + "create() failed", e)
                this@BluetoothChatService.state = Constants.STATE_NONE
            }
            mmSocket = tmp
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private inner class ConnectedThread(socket: BluetoothSocket?, socketType: String) : Thread() {
        private val mmSocket: BluetoothSocket?
        private val mmInStream: InputStream?
        private val mmOutStream: OutputStream?
        override fun run() {
            val buffer = ByteArray(10000)
            var bytes: Int
            var out: String

            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream!!.read(buffer)
                    out = String(buffer, 0, bytes)
                    Log.d(TAG, "out = " + out + "size of out = " + out.length + ", bytes = " + bytes)
                    mHandler?.obtainMessage(Constants.MESSAGE_READ, bytes, -1, out)?.sendToTarget()
                    //                    mEmulatorView.write(buffer, bytes);
                    // Send the obtained bytes to the UI Activity
                    //mHandler.obtainMessage(BlueTerm.MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                } catch (e: IOException) {
                    Log.e(TAG, "disconnected", e)
                    connectionLost()
                    break
                }
            }
        }

        /**
         * Write to the connected OutStream.
         *
         * @param buffer The bytes to write
         */
        fun write(buffer: ByteArray) {
            try {
                Log.d(TAG, "write: I am in inside write method")
                mmOutStream!!.write(buffer)

                // Share the sent message back to the UI Activity
                mHandler?.obtainMessage(Constants.MESSAGE_WRITE, -1, -1, buffer)?.sendToTarget()
            } catch (e: IOException) {
                Log.e(TAG, "Exception during write", e)
            }
        }

        fun cancel() {
            try {
                mmSocket!!.close()
            } catch (e: Exception) {
                Log.e(TAG, "close() of connect socket failed", e)
            }
        }

        init {
            Log.d(TAG, "create ConnectedThread: $socketType")
            mmSocket = socket
            var tmpIn: InputStream? = null
            var tmpOut: OutputStream? = null

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket!!.inputStream
                Log.d(TAG, " tmpIn = $tmpIn")
                tmpOut = socket.outputStream
                Log.d(TAG, " tmpOut = $tmpOut")
            } catch (e: IOException) {
                Log.e(TAG, "temp sockets not created", e)
            }
            mmInStream = tmpIn
            mmOutStream = tmpOut
            this@BluetoothChatService.state = Constants.STATE_CONNECTED
        }
    }

    companion object {
        // Debugging
        private const val TAG = "BluetoothChatService"

        //private static final String NAME_INSECURE = "BluetoothChatInsecure";
        // well-known SPP UUID 00001101-0000-1000-8000-00805F9B34FB
        private val MY_UUID_SECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

        private var mHandler: Handler? = null
    }
    //    private BluetoothSocket socket = null;
    /**
     * Constructor. Prepares a new BluetoothChat session.
     *
     * The UI Activity Context
     * @param handler A Handler to send messages back to the UI Activity
     */
    init {
        mAdapter = BluetoothAdapter.getDefaultAdapter()
        state = Constants.STATE_NONE
        mNewState = state
        mHandler = handler
        context = applicationContext
    }
}