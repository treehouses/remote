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

import android.annotation.SuppressLint;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.github.ivbaranov.rxbluetooth.RxBluetooth;
import com.github.ivbaranov.rxbluetooth.events.ConnectionStateEvent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import io.reactivex.android.plugins.RxAndroidPlugins;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.treehouses.remote.Fragments.HomeFragment;
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


    private static String connectedDeviceName = "NULL";
    // Member fields
    private BluetoothDevice mDevice;
    private static Handler mHandler;

    private int mCurrentState;
    private boolean bNoReconnect;
    private RxBluetooth bluetooth;

    private Context context;
    /**
     * Constructor. Prepares a new BluetoothChat session.
     *
     *  The UI Activity Context
     * @param handler A Handler to send messages back to the UI Activity
     */
    public BluetoothChatService(Handler handler, Context applicationContext) {
        compositeDisposable = new CompositeDisposable();
        mCurrentState = Constants.STATE_NONE;
        mHandler = handler;
        this.context = applicationContext;
        bluetooth = new RxBluetooth(applicationContext);

    }

    public void updateHandler(Handler handler){
        mHandler = handler;
    }

    private synchronized void updateUserInterfaceTitle() {
        Log.d(TAG, "updateUserInterfaceTitle() " + " -> " + mCurrentState);

        // Give the new state to the Handler so the UI Activity can update
        mHandler.sendMessage(mHandler.obtainMessage(Constants.MESSAGE_STATE_CHANGE, mCurrentState, -1));
    }

    /**
     * Return the current connection state.
     */
    public synchronized int getState() {
        return mCurrentState;
    }

    public String getConnectedDeviceName(){return connectedDeviceName;}

    public void connectToDevice(BluetoothDevice device) {
        stopDiscovery();
        compositeDisposable.add(bluetooth.connectAsClient(device, MY_UUID_SECURE).subscribe(
                bluetoothSocket -> {
                    Log.e(TAG, "connectToDevice: CONNECTED");
                    mCurrentState = Constants.STATE_CONNECTED;
                    bluetooth.cancelDiscovery();
                    updateUserInterfaceTitle();
                }, throwable -> Log.e(TAG, "connectToDevice: FAILED TO CONNECT")));

        compositeDisposable.add(bluetooth.observeConnectionState()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.computation())
                .subscribe(event -> {
                    switch (event.getState()) {
                        case BluetoothAdapter.STATE_DISCONNECTED:
                        case BluetoothAdapter.STATE_DISCONNECTING:
                            mCurrentState = Constants.STATE_NONE;
                            Log.e(TAG, "BLUETOOTH: DISCONNECTED");
                            break;
                        case BluetoothAdapter.STATE_CONNECTING:
                            mCurrentState = Constants.STATE_CONNECTING;
                            Log.e(TAG, "BLUETOOTH: CONNECTING");
                            break;
                        case BluetoothAdapter.STATE_CONNECTED:
                            mCurrentState = Constants.STATE_CONNECTED;
                            Log.e(TAG, "BLUETOOTH: CONNECTED");
                            break;
                    }
                    updateUserInterfaceTitle();
                })
        );
    }

    public void disconnect() {
        mCurrentState = Constants.STATE_NONE;
        destroy();
    }

    public void startDiscovery(BluetoothDeviceCallback callback) {
        bluetooth.startDiscovery();
        compositeDisposable.add(bluetooth.observeDevices()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.computation())
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

    public void destroy() {
        if (bluetooth != null) {
            bluetooth.cancelDiscovery();
        }
        compositeDisposable.dispose();
    }

    public void write(byte[] message) {

    }



}
