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

package io.treehouses.remote.MiscOld;

/**
 * Created by yubo on 7/11/17.
 */

import io.treehouses.remote.NetworkOld.BluetoothChatService;

/**
 * Defines several constants used between {@link BluetoothChatService} and the UI.
 */
public interface Constants {

    // Intent request code (use in BluetoothChatFragment)
    int REQUEST_CONNECT_DEVICE_SECURE = 1;
    int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    int REQUEST_ENABLE_BT = 3;
    int REQUEST_DIALOG_FRAGMENT = 4;
    int REQUEST_DIALOG_FRAGMENT_HOTSPOT = 5;
    int REQUEST_DIALOG_FRAGMENT_CHPASS = 6;

    // Constants that indicate the current connection state (use in BluetoothChatService)
    int STATE_NONE = 0;       // we're doing nothing
    int STATE_LISTEN = 1;     // now listening for incoming connections
    int STATE_CONNECTING = 2; // now initiating an outgoing connection
    int STATE_CONNECTED = 3;  // now connected to a remote device

    // Message types sent from the BluetoothChatService Handler
    int MESSAGE_STATE_CHANGE = 1;
    int MESSAGE_READ = 2;
    int MESSAGE_WRITE = 3;
    int MESSAGE_DEVICE_NAME = 4;
    int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    String DEVICE_NAME = "device_name";
    String TOAST = "toast";
    
}
