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

package io.treehouses.remote.Network;

/**
 * Created by yubo on 7/11/17.
 */

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.github.ivbaranov.rxbluetooth.BluetoothConnection;
import com.github.ivbaranov.rxbluetooth.RxBluetooth;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

import io.reactivex.FlowableOperator;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import io.treehouses.remote.Constants;
import io.treehouses.remote.callback.BluetoothDeviceCallback;

/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 */

public class BluetoothChatService implements Serializable{
    // Debugging
    private static final String TAG = "BluetoothChatService";
    // Name for the SDP record when creating server socket
    private static final String NAME_SECURE = "BluetoothChatSecure";

    // well-known SPP UUID 00001101-0000-1000-8000-00805F9B34FB
    private static final UUID MY_UUID_SECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private CompositeDisposable compositeDisposable;
    private BluetoothConnection bluetoothConnection;


    private static String connectedDeviceName = "NULL";
    // Member fields
    private BluetoothDevice mDevice;
    private static Handler mHandler;

    private int mCurrentState;
    private RxBluetooth bluetooth;

    private Context context;

    private Queue<String> sentCommands;
    private boolean switchedHandler = false;
    private Handler tempHandler;
    private Queue<String> tempToSendCommands;
    /**
     * Constructor. Prepares a new BluetoothChat session.
     *
     *  The UI Activity Context
     * @param handler A Handler to send messages back to the UI Activity
     */
    public BluetoothChatService(Handler handler, Context applicationContext) {
        this.context = applicationContext;
        init();
    }

    private void init() {
        compositeDisposable = new CompositeDisposable();
        mCurrentState = Constants.STATE_NONE;
        mHandler = null;
        sentCommands = new LinkedList<>();
        tempToSendCommands = new LinkedList<>();
        bluetooth = new RxBluetooth(context);
    }

    public void updateHandler(Handler handler, boolean... args){
        Log.e(TAG, "updateHandler:");
        if (!sentCommands.isEmpty() && mCurrentState == Constants.STATE_CONNECTED) {
            Log.e(TAG, "updateHandler: with overflow");
            switchedHandler = true;
            tempHandler = handler;
            tempToSendCommands.clear();
        } else {
            Log.e(TAG, "updateHandler: without overflow");
            mHandler = handler;
        }
    }

    private synchronized void updateUserInterfaceTitle() {
        Log.d(TAG, "updateUserInterfaceTitle() " + " -> " + mCurrentState);
        // Give the new state to the Handler so the UI Activity can update
        mHandler.sendMessage(mHandler.obtainMessage(Constants.MESSAGE_STATE_CHANGE, mCurrentState, -1));
    }

    public synchronized int getState() {
        return mCurrentState;
    }

    public String getConnectedDeviceName(){
        if (mDevice != null) return  mDevice.getName();
        return "NO DEVICE CONNECTED";
    }

    public void connectToDevice(BluetoothDevice device) {
        stopDiscovery();
        mCurrentState = Constants.STATE_CONNECTING;
        compositeDisposable.add(bluetooth.connectAsClient(device, MY_UUID_SECURE).subscribeOn(Schedulers.computation()).subscribe(
                bluetoothSocket -> {
                    Log.e(TAG, "connectToDevice: CONNECTED to " + device.getName());
                    mDevice = device;
                    mCurrentState = Constants.STATE_CONNECTED;
                    bluetooth.cancelDiscovery();
                    updateUserInterfaceTitle();
                    compositeDisposable.clear();
                    startChat(bluetoothSocket);
                }, throwable -> {
                    Log.e(TAG, "connectToDevice: FAILED TO CONNECT");
                    mCurrentState = Constants.STATE_FAILED;
                    updateUserInterfaceTitle();
                    init();
                }));
    }

    private void startChat(BluetoothSocket socket) throws Exception {
        Log.e(TAG, "startChat: "+"START READING" );
        bluetoothConnection = new BluetoothConnection(socket);

        compositeDisposable.add(bluetoothConnection.observeByteStream().lift((FlowableOperator<String, Byte>) this::getWriter).onBackpressureBuffer().observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io())
                .subscribe(s -> {
                    // READ COMMAND RESPONSE
                    Log.d(TAG, "READ: " + s);
                    //Remove the last command because response has been received
                    if (!sentCommands.isEmpty()) sentCommands.remove();
                    //Send the response to the target
                    if (!switchedHandler) mHandler.obtainMessage(Constants.MESSAGE_READ, s).sendToTarget();

                    //Sent all the waiting commands
                    if (switchedHandler && sentCommands.isEmpty()) {
                        Log.e(TAG, "SENT ALL PREVIOUS");
                        switchedHandler = false;
                        mHandler = tempHandler;
                        writeOverflow();
                    }

                }, throwable -> {
                    Log.e(TAG, "startChat: "+ "ERROR OCCURRED WHILE READING");
                    mCurrentState = Constants.STATE_FAILED;
                    updateUserInterfaceTitle();
                    disconnect();
                }));
    }

    private void writeOverflow() {
        while (!tempToSendCommands.isEmpty()) {
            write(tempToSendCommands.remove());
        }
    }

    public void disconnect() {
        Log.e(TAG, "DISCONNECTING");
        if (bluetoothConnection != null) {
            bluetoothConnection.closeConnection();
            bluetoothConnection = null;
            mDevice = null;
        }
        mCurrentState = Constants.STATE_NONE;
        updateUserInterfaceTitle();
        destroy();
        init();
    }

    public void startDiscovery(BluetoothDeviceCallback callback) {
        Log.e(TAG, "STARTING DISCOVERY");
        if (bluetooth.isDiscovering()) bluetooth.cancelDiscovery();
        bluetooth.startDiscovery();
        mCurrentState = Constants.STATE_LISTEN;
        updateUserInterfaceTitle();
        compositeDisposable.add(bluetooth.observeDevices()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(bluetoothDevice -> {
                    callback.onDeviceFound(bluetoothDevice);
                    Log.e(TAG, "DEVICE FOUND: "+ bluetoothDevice.getName()+ " ADDRESS: " + bluetoothDevice.getAddress());
                }));
    }

    public void stopDiscovery() {
        bluetooth.cancelDiscovery();
    }

    public Set<BluetoothDevice> getPairedDevices() {
        return bluetooth.getBondedDevices();
    }

    public boolean isBluetoothSupported() {
        return bluetooth.isBluetoothAvailable();
    }

    public boolean isBluetoothEnabled() {
        return bluetooth.isBluetoothEnabled();
    }

    private void destroy() {
        Log.e(TAG, "DESTROYING");
        if (bluetooth != null) {
            bluetooth.cancelDiscovery();
        }
        compositeDisposable.clear();
        mCurrentState = Constants.STATE_NONE;
    }

    public void write(String message) {
        if (bluetoothConnection != null) {

            Log.d(TAG, "write: " + message);
            if (switchedHandler) {
                Log.e(TAG, "WRITING TO TEMP: "+message);
                tempToSendCommands.add(message);
            } else {
                Log.e(TAG, "SENDING TO BLUETOOTH: "+message);
                sentCommands.add(message);
                bluetoothConnection.send(message);
            }
            mHandler.obtainMessage(Constants.MESSAGE_WRITE, message).sendToTarget();
        } else {
            Log.e(TAG, "Error while writing to bluetooth");
        }
    }

    private Subscriber getWriter(Subscriber subscriber) {
        return new MyWriter(subscriber);
    }

}
