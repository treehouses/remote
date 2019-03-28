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

import java.util.ArrayList;
import java.util.List;

import io.treehouses.remote.Network.BluetoothChatService;

/**
 * Defines several constants used between {@link BluetoothChatService} and the UI.
 */
public class Constants {

    // Intent request code (use in BluetoothChatFragment)
    public static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    public static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    public static final int REQUEST_ENABLE_BT = 3;
    public static final int REQUEST_DIALOG_FRAGMENT = 4;
    public static final int REQUEST_DIALOG_FRAGMENT_HOTSPOT = 5;
    public static final int REQUEST_DIALOG_FRAGMENT_CHPASS = 6;

    // Constants that indicate the current connection state (use in BluetoothChatService)
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    public static final List<String> terminalList = new ArrayList();
    static {
        terminalList.add("Change Pi Password");
        terminalList.add("Treehouses");
        terminalList.add("Treehouses Detectrpi");
        terminalList.add("Docker ps");
        terminalList.add("Rename Hostname");
        terminalList.add("Expand File System");
    }
}

