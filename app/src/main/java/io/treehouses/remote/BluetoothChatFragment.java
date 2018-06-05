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

package io.treehouses.remote;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by yubo on 7/11/17.
 * This fragment controls Bluetooth to communicate with other devices.
 */

public class BluetoothChatFragment extends android.support.v4.app.Fragment {

    private static final String TAG = "BluetoothChatFragment";

    //current connection status
    static String currentStatus = "not connected";
    private static boolean connected = false;
    private static boolean attempt = false;

    // Layout Views
    private ListView mConversationView;
    private EditText mOutEditText;
    private Button mSendButton;
    private ProgressDialog wifiPD;
    private ProgressDialog hotspotPD;
    private Button treehousesButton;
    private Button dockerButton;
    private Button RPI_HW_Button;
    private Button hostnameButton;
    private Button changePasswordButton;
    private Button expandButton;

    /**
     * Name of the connected device
     */
    private String mConnectedDeviceName = null;

    /**
     * Array adapter for the conversation thread
     */
    private ArrayAdapter<String> mConversationArrayAdapter;

    /**
     * String buffer for outgoing messages
     */
    private StringBuffer mOutStringBuffer;

    /**
     * Local Bluetooth adapter
     */
    private BluetoothAdapter mBluetoothAdapter = null;

    /**
     * Member object for the chat services
     */
    private BluetoothChatService mChatService = null;
    private static boolean isRead = false;
    private static boolean isCountdown = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            FragmentActivity activity = getActivity();
            Toast.makeText(activity, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            activity.finish();
        }

        //start looking for when bluetooth is paired, and then implement wifiReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
        getActivity().registerReceiver(wifiReceiver, filter);
    }

    @Override
    public void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, Constants.REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else if (mChatService == null) {
            setupChat();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isCountdown) {
            mHandler.removeCallbacks(watchDogTimeOut);
        }
        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == Constants.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }
        getActivity().unregisterReceiver(wifiReceiver);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bluetooth_chat, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mConversationView = (ListView) view.findViewById(R.id.in);
        mOutEditText = (EditText) view.findViewById(R.id.edit_text_out);
        mSendButton = (Button) view.findViewById(R.id.button_send);
        treehousesButton = (Button) view.findViewById(R.id.TB);
        dockerButton = (Button) view.findViewById(R.id.DB);
        RPI_HW_Button = (Button) view.findViewById(R.id.VB);
        hostnameButton = (Button) view.findViewById(R.id.HN);
        changePasswordButton = (Button) view.findViewById(R.id.CP);
        expandButton = (Button) view.findViewById(R.id.EF);
    }

    /**
     * Set up the UI and background operations for chat.
     */
    private void setupChat() {
        Log.d(TAG, "setupChat()");

        // Initialize the array adapter for the conversation thread
        mConversationArrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.message) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView consoleView = (TextView) view.findViewById(R.id.listItem);
                if (isRead) {
                    consoleView.setTextColor(Color.BLUE);
                } else {
                    consoleView.setTextColor(Color.RED);
                }
                return view;
            }
        };

        mConversationView.setAdapter(mConversationArrayAdapter);

        // Initialize the compose field with a listener for the return key
        mOutEditText.setOnEditorActionListener(mWriteListener);

        // Initialize the send button with a listener that for click events
        mSendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                View view = getView();
                if (null != view) {
                    TextView consoleInput = (TextView) view.findViewById(R.id.edit_text_out);
                    String message = consoleInput.getText().toString();
                    sendMessage(message);
                }
            }
        });

        treehousesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String t = "treehouses";
                sendMessage(t);
            }
        });

        dockerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String d = "docker ps";
                sendMessage(d);
            }
        });

        RPI_HW_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String v = "treehouses detectrpi";
                sendMessage(v);
            }
        });

        hostnameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog(view);
            }
        });

        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChPasswordDialog();
            }
        });
        expandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String e = "treehouses expandfs";
                sendMessage(e);
            }
        });

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(getActivity(), mHandler);
        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");

        // Create spinner for Wifi
        wifiPD = new ProgressDialog(getActivity());
        wifiPD.setTitle(R.string.progress_dialog_title);
        wifiPD.setMessage(getString(R.string.progress_dialog_message));
        wifiPD.setCancelable(false); // disable dismiss by tapping outside of the dialog

        // Create spinner for Hotspot
        hotspotPD = new ProgressDialog(getActivity());
        hotspotPD.setTitle(R.string.progress_dialog_title_hotspot);
        hotspotPD.setMessage(getString(R.string.progress_dialog_message));
        hotspotPD.setCancelable(false); // disable dismiss by tapping outside of the dialog
    }

    /**
     * This block is to create a dialog box for creating a new name or changing the password for the PI device
     * Sets the dialog button to be disabled if no text is in the EditText
     */
    private void showDialog(View view) {
        final EditText input = new EditText(getActivity());
        final AlertDialog alertDialog = showAlertDialog(
                "Rename Hostname",
                "Please enter new hostname",
                "treehouses rename ", input);

        alertDialog.getButton(alertDialog.BUTTON_POSITIVE).setClickable(false);
        alertDialog.getButton(alertDialog.BUTTON_POSITIVE).setEnabled(false);

        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    alertDialog.getButton(alertDialog.BUTTON_POSITIVE).setClickable(true);
                    alertDialog.getButton(alertDialog.BUTTON_POSITIVE).setEnabled(true);
                } else {
                    alertDialog.getButton(alertDialog.BUTTON_POSITIVE).setClickable(false);
                    alertDialog.getButton(alertDialog.BUTTON_POSITIVE).setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private AlertDialog showAlertDialog(String title, String message, final String command, final EditText input) {
        return new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage(message)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setView(input)
                .setPositiveButton("Change", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        String hostnameInput = input.getText().toString();
                        String h = command + hostnameInput.toString();
                        sendMessage(h);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .show();
    }

    /**
     * Makes this device discoverable for 300 seconds (5 minutes).
     */
    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != Constants.STATE_CONNECTED) {
            Toast.makeText(getActivity(), R.string.not_connected, Toast.LENGTH_SHORT).show();
            wConnect(R.color.grey);
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
            mOutEditText.setText(mOutStringBuffer);
        }
    }

    private void sendPing(String ping) {
        // Get the message bytes and tell the BluetoothChatService to write
        byte[] pSend = ping.getBytes();
        mChatService.write(pSend);
        mOutStringBuffer.setLength(0);
    }

    private void sendMessage(String SSID, String PWD) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != Constants.STATE_CONNECTED) {
            Toast.makeText(getActivity(), R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (SSID.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            JSONObject mJson = new JSONObject();
            try {
                mJson.put("SSID", SSID);
                mJson.put("PWD", PWD);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            byte[] send = mJson.toString().getBytes();
            mChatService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
            mOutEditText.setText(mOutStringBuffer);
        }
    }

    /**
     * The action listener for the EditText widget, to listen for the return key
     */
    private TextView.OnEditorActionListener mWriteListener
            = new TextView.OnEditorActionListener() {
        public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
            // If the action is a key-up event on the return key, send the message
            if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                String message = view.getText().toString();
                sendMessage(message);
            }
            return true;
        }
    };

    /**
     * Updates the status on the action bar.
     *
     * @param arg: can be either a string resource ID or subTitle status
     */

    private void setStatus(Object arg) {
        FragmentActivity activity = getActivity();
        if (null == activity) {
            return;
        }
        final ActionBar actionBar = activity.getActionBar();
        if (null == actionBar) {
            return;
        }
        if (arg instanceof Integer) {
            Log.d(TAG, "actionBar.setSubtitle(resId) = " + arg);
            currentStatus = getString((Integer) arg);
            actionBar.setSubtitle((Integer) arg);
        } else if (arg instanceof CharSequence) {
            Log.d(TAG, "actionBar.setSubtitle(subTitle) = " + arg);
            currentStatus = arg.toString();
            actionBar.setSubtitle((CharSequence) arg);
        }
    }

    private final Runnable watchDogTimeOut = new Runnable() {
        @Override
        public void run() {
            isCountdown = false;
            //time out
            if (wifiPD.isShowing()) {
                wifiPD.dismiss();
                Toast.makeText(getActivity(), "No response from RPi", Toast.LENGTH_LONG).show();
            }
            if (hotspotPD.isShowing()) {
                hotspotPD.dismiss();
                Toast.makeText(getActivity(), "No response from RPi", Toast.LENGTH_LONG).show();
            }
        }
    };

    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            FragmentActivity activity = getActivity();
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case Constants.STATE_CONNECTED:
                            setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                            mConversationArrayAdapter.clear();
                            break;
                        case Constants.STATE_CONNECTING:
                            setStatus(R.string.title_connecting);
                            break;
                        case Constants.STATE_LISTEN:
                        case Constants.STATE_NONE:
                            setStatus(R.string.title_not_connected);
                            //if not connected to rpi, don't show data
                            wConnect(R.color.grey);
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    isRead = false;
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    if (!writeMessage.contains("google.com") && !writeMessage.contains("treehouses checksignal")) {
                        Log.d(TAG, "writeMessage = " + writeMessage);
                        mConversationArrayAdapter.add("Command:  " + writeMessage);
                    }
                    break;
                case Constants.MESSAGE_READ:
                    isRead = true;
//                    byte[] readBuf = (byte[]) msg.obj;
//                     construct a string from the valid bytes in the buffer
//                    String readMessage = new String(readBuf, 0, msg.arg1);
//                    String readMessage = new String(readBuf);
                    String readMessage = (String) msg.obj;
                    Log.d(TAG, "readMessage = " + readMessage);
                    //TODO: if message is json -> callback from RPi
                    if (isJson(readMessage)) {
                        handleCallback(readMessage);
                    } else {
                        if (isCountdown) {
                            mHandler.removeCallbacks(watchDogTimeOut);
                            isCountdown = false;
                        }
                        if (wifiPD.isShowing()) {
                            wifiPD.dismiss();
                            Toast.makeText(activity, R.string.config_alreadyConfig, Toast.LENGTH_SHORT).show();
                        }
                        if (hotspotPD.isShowing()) {
                            hotspotPD.dismiss();
                            Toast.makeText(activity, R.string.config_alreadyConfig_hotspot, Toast.LENGTH_SHORT).show();
                        }
                        //remove the space at the very end of the readMessage -> eliminate space between items
                        readMessage = readMessage.substring(0, readMessage.length() - 1);
                        //mConversationArrayAdapter.add(mConnectedDeviceName + ":  " + readMessage);

                        //check if ping was successful
                        if (readMessage.contains("1 packets") && !attempt) {
                            wConnect(R.color.green); //for ethernet users
                            attempt = true;
                            connected = true;
                        }

                        if ((readMessage.contains("Unreachable") || readMessage.contains("failure")) && !attempt) {
                            wConnect(R.color.red);
                            attempt = true;
                            connected = false;
                        }

                        //check the signal
                        if (readMessage.contains("Signal level=") && connected) {
                            String last = readMessage;
                            last = last.replaceAll("[^\\d-]", "");
                            int level = Integer.parseInt(last);

                            if (level >= -50) {
                                wConnect(R.color.green); //good connection
                            }
                            if ((level < -50) && (level > -100)) {
                                wConnect(R.color.yellow); //decent connection
                            }
                            if (level <= -100) {
                                wConnect(R.color.purple); //bad connection
                            }
                        }

                        //make it so text doesn't show on chat (need a better way to check multiple strings since bluetooth server only sends messages line by line)
                        if (!readMessage.matches("^.*?(1 packets|64 bytes|google.com|rtt|not connected to|Signal level|you are not on a wireless connection).*$") && !readMessage.trim().isEmpty()) {
                            mConversationArrayAdapter.add(readMessage);
                        }
                        break;
                    }
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != activity) {
                        Toast.makeText(activity, "Connected to "
                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != activity) {
                        Toast.makeText(activity, msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constants.REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case Constants.REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case Constants.REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupChat();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(getActivity(), R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }
                break;
            case Constants.REQUEST_DIALOG_FRAGMENT:
                if (resultCode == Activity.RESULT_OK) {

                    //check status
                    if (mChatService.getState() != Constants.STATE_CONNECTED) {
                        Toast.makeText(getActivity(), R.string.not_connected,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    //show the progress bar, disable user interaction
                    wifiPD.show();
                    //TODO: start watchdog
                    isCountdown = true;
                    mHandler.postDelayed(watchDogTimeOut, 30000);
                    Log.d(TAG, "watchDog start");

                    //get SSID & PWD from user input
                    String SSID = data.getStringExtra("SSID") == null ? "" : data.getStringExtra("SSID");
                    String PWD = data.getStringExtra("PWD") == null ? "" : data.getStringExtra("PWD");

                    String wifi = "treehouses wifi " + SSID + " " + PWD;

                    Log.d(TAG, "back from dialog: ok, SSID = " + SSID + ", PWD = " + PWD);

                    //TODO: 1. check Valid input  2. get the SSID and password from data object and send it to RPi through sendMessage() method
                    //Toast.makeText(getActivity(), R.string.config_success,
                    //Toast.LENGTH_SHORT).show();

                    sendMessage(wifi);
                    //TODO:1. lock the app when configuring. 2. listen to configuration result and do the logic

                } else {
                    Log.d(TAG, "back from dialog, fail");
                }
                break;
            case Constants.REQUEST_DIALOG_FRAGMENT_HOTSPOT:
                if (resultCode == Activity.RESULT_OK) {

                    //check status
                    if (mChatService.getState() != Constants.STATE_CONNECTED) {
                        Toast.makeText(getActivity(), R.string.not_connected,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    //show the progress bar, disable user interaction
                    hotspotPD.show();
                    //TODO: start watchdog
                    isCountdown = true;
                    mHandler.postDelayed(watchDogTimeOut, 30000);
                    Log.d(TAG, "watchDog start");

                    //get SSID & PWD from user input
                    String HSSID = data.getStringExtra("HSSID") == null ? "" : data.getStringExtra("HSSID");
                    String HPWD = data.getStringExtra("HPWD") == null ? "" : data.getStringExtra("HPWD");

                    String hotSpot = "treehouses hotspot " + HSSID + " " + HPWD + "";

                    Log.d(TAG, "back from dialog_hotspot: ok, SSID = " + HSSID + ", PWD = " + HPWD);

                    //TODO: 1. check Valid input  2. get the SSID and password from data object and send it to RPi through sendMessage() method
//                    Toast.makeText(getActivity(), R.string.config_success,
//                            Toast.LENGTH_SHORT).show();

                    sendMessage(hotSpot);
                    //TODO:1. lock the app when configuring. 2. listen to configuration result and do the logic

                } else {
                    Log.d(TAG, "back from dialog_hotspot, fail");
                }
                break;

            case Constants.REQUEST_DIALOG_FRAGMENT_CHPASS:
                if (resultCode == Activity.RESULT_OK) {

                    //get password change request
                    String chPWD = data.getStringExtra("password") == null ? "" : data.getStringExtra("password");

                    //store password and command
                    String password = "treehouses password " + chPWD;

                    Log.d(TAG, "back from change password");

                    //send password to command line interface
                    sendMessage(password);

                } else {
                    Log.d(TAG, "back from change password, fail");
                }
                break;
        }
    }

    /**
     * Establish connection with other device
     *
     * @param data   An {@link Intent} with {@link DeviceListActivity#EXTRA_DEVICE_ADDRESS} extra.
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mChatService.connect(device, secure);
    }

    // Is this unused? Can we remove this????
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //inflater.inflate(R.menu.bluetooth_chat, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.wifi_configuration: {
                showWifiDialog();
                //return true;
                break;
            }
            case R.id.insecure_connect_scan: {
                // Launch the DeviceListActivity to see devices and do scan
                Intent serverIntent = new Intent(getActivity(), DeviceListActivity.class);
                startActivityForResult(serverIntent, Constants.REQUEST_CONNECT_DEVICE_INSECURE);
                //return true;
                break;
            }
            case R.id.discoverable: {
                // Ensure this device is discoverable by others
                ensureDiscoverable();
                //return true;
                break;
            }
            case R.id.hotspot_configuration: {
                showHotspotDialog();
                //return true;
                break;
            }
            default:
                return false;
        }
        return true;
    }

    public void showWifiDialog() {
        // Create an instance of the dialog fragment and show it
        DialogFragment dialogFrag = WifiDialogFragment.newInstance(123);
        dialogFrag.setTargetFragment(this, Constants.REQUEST_DIALOG_FRAGMENT);
        dialogFrag.show(getFragmentManager().beginTransaction(), "wifiDialog");
    }

    public void showHotspotDialog() {
        // Create an instance of the dialog fragment and show it
        DialogFragment dialogFrag = HotspotDialogFragment.newInstance(123);
        dialogFrag.setTargetFragment(this, Constants.REQUEST_DIALOG_FRAGMENT_HOTSPOT);
        dialogFrag.show(getFragmentManager().beginTransaction(), "hotspotDialog");
    }

    public void showChPasswordDialog() {
        // Create an instance of the dialog fragment and show it
        DialogFragment dialogFrag = ChPasswordDialogFragment.newInstance(123);
        dialogFrag.setTargetFragment(this, Constants.REQUEST_DIALOG_FRAGMENT_CHPASS);
        dialogFrag.show(getFragmentManager().beginTransaction(), "ChangePassDialog");
    }

    public boolean isJson(String str) {
        try {
            new JSONObject(str);
        } catch (JSONException ex) {
            return false;
        }
        return true;
    }

    public void handleCallback(String str) {
        String result;
        String ip;
        if (isCountdown) {
            mHandler.removeCallbacks(watchDogTimeOut);
            isCountdown = false;
        }

        //enable user interaction
        wifiPD.dismiss();
        try {
            JSONObject mJSON = new JSONObject(str);
            result = mJSON.getString("result") == null ? "" : mJSON.getString("result");
            ip = mJSON.getString("IP") == null ? "" : mJSON.getString("IP");
            //Toast.makeText(getActivity(), "result: "+result+", IP: "+ip, Toast.LENGTH_LONG).show();

            if (!result.equals("SUCCESS")) {
                Toast.makeText(getActivity(), R.string.config_fail,
                        Toast.LENGTH_LONG).show();
            } else {
//                Toast.makeText(getActivity(), R.string.config_success,
//                            Toast.LENGTH_SHORT).show();
                Toast.makeText(getActivity(), getString(R.string.config_success) + ip, Toast.LENGTH_LONG).show();
            }

        } catch (JSONException e) {
            // error handling
            Toast.makeText(getActivity(), "SOMETHING WENT WRONG", Toast.LENGTH_LONG).show();
        }
    }

    //showing wifi signal
    private void wConnect(int color) {
        getActivity().invalidateOptionsMenu();
        Drawable signal = getResources().getDrawable(R.drawable.wifi_configuration);
        signal.setColorFilter(getResources().getColor(color), PorterDuff.Mode.SRC_ATOP);
        getActivity().getActionBar().setIcon(signal);
    }

    private final BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        public void onReceive(final Context context, final Intent intent) {
            final String mIntentAction = intent.getAction();
            final Handler handlerPing = new Handler();
            final int startTest = 22000;
            final int startWifi = 27000;
            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(mIntentAction)) {
                handlerPing.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        String ping = "ping -c 1 google.com";
                        sendPing(ping);
                        handlerPing.postDelayed(this, startTest);
                    }
                }, startTest);

                handlerPing.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        String signal = "treehouses checksignal";
                        sendPing(signal);
                        handlerPing.postDelayed(this, startWifi);
                    }
                }, startWifi);
            }
        }
    };
}




