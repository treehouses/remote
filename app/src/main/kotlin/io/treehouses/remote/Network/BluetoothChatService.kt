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
import android.app.PendingIntent
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import io.treehouses.remote.Constants
import io.treehouses.remote.InitialActivity
import io.treehouses.remote.R
import io.treehouses.remote.bases.BaseBluetoothChatService
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream


/**
 * Created by yubo on 7/11/17.
 */
/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 */
class BluetoothChatService @JvmOverloads constructor(handler: Handler? = null, applicationContext: Context? = null) : BaseBluetoothChatService(handler, applicationContext) {
    inner class DisconnectReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            if (action == DISCONNECT_ACTION) {
                stop()
            }
        }
    }

    // Member fields

    private val mBinder = LocalBinder()
    private var mConnectThread: ConnectThread? = null
    private var mConnectedThread: ConnectedThread? = null

    private val receiver = DisconnectReceiver()

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    @Synchronized
    override fun start() {
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



    fun updateHandler(handler: Handler) {
        mHandler = handler
    }

    override fun onBind(intent: Intent?): IBinder? {
        return mBinder
    }

    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        val service: BluetoothChatService
            get() = this@BluetoothChatService
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.e("BLUETOOTH", "START COMMAND")
        return START_NOT_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        val i = IntentFilter()
        i.addAction(DISCONNECT_ACTION)
        registerReceiver(receiver, i)
    }

    override fun onDestroy() {
        Log.e("BLUETOOTH", "Destroying...")
        stop()
        unregisterReceiver(receiver)
        super.onDestroy()
    }

    private fun startNotification() {
        val disconnectIntent = Intent(DISCONNECT_ACTION)
        val disconnectPendingIntent: PendingIntent = PendingIntent.getBroadcast(this, 0, disconnectIntent, 0)

        val onClickIntent = Intent(this, InitialActivity::class.java)
        val pendingClickIntent = PendingIntent.getActivity(this, 0, onClickIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(this, getString(R.string.bt_notification_ID))
        val notification: Notification = notificationBuilder.setOngoing(true)
                .setContentTitle("Treehouses Remote is currently running")
                .setContentText("Connected to ${mDevice?.name}")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setSmallIcon(R.drawable.treehouses2)
                .setContentIntent(pendingClickIntent)
                .addAction(R.drawable.bluetooth, "Disconnect", disconnectPendingIntent)
                .build()
        startForeground(2, notification)
    }



    var connectedDeviceName: String = ""



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

        startNotification()

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
        stopForeground(true)
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
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private fun connectionLost() {
        // Send a failure message back to the Activity
        callHandler("Device connection was lost")
        stopForeground(true)

        val preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
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
            this@BluetoothChatService.state = Constants.STATE_CONNECTING
            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery()

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket!!.connect()
            } catch (e: Exception) {
                // Close the socket
                Log.e("ERROR WHILE CONNECTING", e.toString(), e)
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
                this@BluetoothChatService.state = Constants.STATE_CONNECTING
            } catch (e: Exception) {
                Log.e(TAG, "Socket Type: " + mSocketType + "create() failed", e)
                this@BluetoothChatService.state = Constants.STATE_NONE
            }
            this@BluetoothChatService.updateUserInterfaceTitle()
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
            this@BluetoothChatService.updateUserInterfaceTitle()
        }
    }

//    inner class DisconnectReceiver : BroadcastReceiver() {
//        override fun onReceive(context: Context?, intent: Intent?) {
//            if (intent?.action == DISCONNECT_ACTION) {
//                stop()
//            }
//        }
//
//    }



}