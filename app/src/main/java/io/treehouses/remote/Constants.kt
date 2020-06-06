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
package io.treehouses.remote

import java.util.*

/**
 * Created by yubo on 7/11/17.
 */
/**
 * Defines several constants used between [BluetoothChatService] and the UI.
 */
object Constants {
    const val PARSE_URL = "http://treehouses.media.mit.edu:1337/parse/"
    const val PARSE_APPLICATION_ID = "treehouses"
    var value = ""
    private val groups = ArrayList<String>()

    // Intent request code (use in BluetoothChatFragment)
    const val REQUEST_CONNECT_DEVICE_SECURE = 1
    const val REQUEST_CONNECT_DEVICE_INSECURE = 2
    const val REQUEST_ENABLE_BT = 3
    const val REQUEST_DIALOG_FRAGMENT = 4
    const val REQUEST_DIALOG_FRAGMENT_HOTSPOT = 5
    const val REQUEST_DIALOG_FRAGMENT_CHPASS = 6
    const val REQUEST_DIALOG_FRAGMENT_ADD_COMMAND = 7
    const val REQUEST_DIALOG_WIFI = 8
    const val NETWORK_BOTTOM_SHEET = 9

    // Constants that indicate the current connection state (use in BluetoothChatService)
    const val STATE_NONE = 0 // we're doing nothing
    const val STATE_LISTEN = 1 // now listening for incoming connections
    const val STATE_CONNECTING = 2 // now initiating an outgoing connection
    const val STATE_CONNECTED = 3 // now connected to a remote device

    // Message types sent from the BluetoothChatService Handler
    const val MESSAGE_STATE_CHANGE = 1
    const val MESSAGE_READ = 2
    const val MESSAGE_WRITE = 3
    const val MESSAGE_DEVICE_NAME = 4
    const val MESSAGE_TOAST = 5

    // Key names received from the BluetoothChatService Handler
    const val DEVICE_NAME = "device_name"
    const val TOAST = "toast"

    //JSON String bundle
    const val JSON_STRING = "jsonString"
}