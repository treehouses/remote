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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by yubo on 7/11/17.
 */

/**
 * This fragment controls Bluetooth to communicate with other devices.
 */

public class BluetoothChatFragment extends android.support.v4.app.Fragment {

    private static final String TAG = "BluetoothChatFragment";

    // Layout Views
    private ListView mConversationView;
    private EditText mOutEditText;
    private Button mSendButton;
    private ProgressDialog mProgressDialog;
    private ProgressDialog hProgressDialog;
    private Button Tbutton;
    private Button Dbutton;
    private Button Vbutton;
    private Button Pbutton;
    private Button HNbutton;
    private Button CPbutton;
    private Button EFbutton;
    private String hnInput;
    private Boolean isValidInput;

    // Array adapter for the conversation thread
    private ArrayAdapter<String> mConversationArrayAdapter;

    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;

    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;

    // Member object for the chat services
    private BluetoothChatService mChatService = null;

    private static boolean isRead = false;

    private static boolean isCountdown = false;

    @Override
    public void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        getActivity().getActionBar().hide();
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mChatService = (BluetoothChatService) getArguments().getSerializable("mChatService");
        Log.d(TAG, "mChatService's state in ChatFragment: " + mChatService.getState());
        mChatService.setHandler(mHandler);

        //start pinging for wifi check
        final Handler h = new Handler();
        final int delay = 20000;
        h.postDelayed(new Runnable(){
            public void run(){
                String ping = "ping -c 1 google.com";
                sendPing(ping);
                h.postDelayed(this, delay);
            }
        }, delay);

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            FragmentActivity activity = getActivity();
            Toast.makeText(activity, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            activity.finish();
        }
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
        } else {setupChat();}
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(isCountdown){
            mHandler.removeCallbacks(watchDogTimeOut);
        }
        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
                mIdle();
            }
        }
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
        Tbutton = (Button) view.findViewById(R.id.TB);
        Dbutton = (Button)view.findViewById(R.id.DB);
        Vbutton = (Button)view.findViewById(R.id.VB);
        Pbutton = (Button)view.findViewById(R.id.PING);
        HNbutton = (Button)view.findViewById(R.id.HN);
        CPbutton = (Button) view.findViewById(R.id.CP);
        EFbutton = (Button)view.findViewById(R.id.EF);
    }

    /**
     * Set up the UI and background operations for chat.
     */
    private void setupChat() {
        Log.d(TAG, "setupChat()");

        // Initialize the array adapter for the conversation thread
        mConversationArrayAdapter = new ArrayAdapter<String>(getActivity(),R.layout.message){
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView tView = (TextView) view.findViewById(R.id.listItem);
                if(isRead){
                    tView.setTextColor(Color.BLUE);
                }else{
                    tView.setTextColor(Color.RED);
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
                    TextView textView = (TextView) view.findViewById(R.id.edit_text_out);
                    String message = textView.getText().toString();
                    sendMessage(message);
                }
            }
        });

        Tbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String t = "pirateship";
                sendMessage(t);
            }
        });

        Dbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String d = "docker ps";
                sendMessage(d);
            }
        });

        Vbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String v = "pirateship detectrpi";
                sendMessage(v);
            }
        });

        HNbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog(view);
            }

        });

        CPbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog(view);
            }
        });
        EFbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String e = "pirateship expandfs";
                sendMessage(e);
            }
        });

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");

        //get spinner
        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setTitle(R.string.progress_dialog_title);
        mProgressDialog.setMessage(getString(R.string.progress_dialog_message));
        mProgressDialog.setCancelable(false); // disable dismiss by tapping outside of the dialog

        //get spinner for hotspot
        hProgressDialog = new ProgressDialog(getActivity());
        hProgressDialog.setTitle(R.string.progress_dialog_title_hotspot);
        hProgressDialog.setMessage(getString(R.string.progress_dialog_message));
        hProgressDialog.setCancelable(false); // disable dismiss by tapping outside of the dialog

    }

    /**
     * This block is to create a dialog box for creating a new name or changing the password for the PI device
     * Sets the dialog button to be disabled if no text is in the EditText
     */
    private void showDialog(View view) {
        final EditText input = new EditText(getActivity());
        final AlertDialog alertDialog = getAlertDialog(input, view);


        alertDialog.getButton(alertDialog.BUTTON_POSITIVE).setClickable(false);
        alertDialog.getButton(alertDialog.BUTTON_POSITIVE).setEnabled(false);

        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() > 0) {
                    alertDialog.getButton(alertDialog.BUTTON_POSITIVE).setClickable(true);
                    alertDialog.getButton(alertDialog.BUTTON_POSITIVE).setEnabled(true);
                }else{
                    alertDialog.getButton(alertDialog.BUTTON_POSITIVE).setClickable(false);
                    alertDialog.getButton(alertDialog.BUTTON_POSITIVE).setEnabled(false);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private AlertDialog showAlertDialog(String title, String message, final String command, final EditText input){
        return new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage(message)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setView(input)
                .setPositiveButton("Change", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        hnInput = input.getText().toString();
                        String h = command + hnInput.toString();
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

    private AlertDialog getAlertDialog(final EditText input, View view) {
        if(view.equals(view.findViewById(R.id.HN))) {
            return showAlertDialog(
                    "Rename Hostname",
                    "Please enter new hostname",
                    "pirateship rename ", input);
        }else{
            return showAlertDialog(
                    "Change Password",
                    "Please enter new password",
                    "treehouses password ", input);
        }
    }

    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(getActivity(), R.string.not_connected, Toast.LENGTH_SHORT).show();
            mIdle();
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

    private final Runnable watchDogTimeOut = new Runnable() {
        @Override
        public void run() {
            isCountdown = false;
            //time out
            if(mProgressDialog.isShowing()){
                mProgressDialog.dismiss();
                Toast.makeText(getActivity(),"No response from RPi",Toast.LENGTH_LONG).show();
            }
            if(hProgressDialog.isShowing()){
                hProgressDialog.dismiss();
                Toast.makeText(getActivity(),"No response from RPi",Toast.LENGTH_LONG).show();
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
                case Constants.MESSAGE_WRITE:
                    isRead = false;
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    if(!writeMessage.contains("google.com")) {
                        Log.d(TAG, "writeMessage = " + writeMessage);
                        mConversationArrayAdapter.add("Command:  " + writeMessage);
                    }
                    break;
                case Constants.MESSAGE_READ:
                    isRead = true;
//                   byte[] readBuf = (byte[]) msg.obj;
//                   construct a string from the valid bytes in the buffer
//                   String readMessage = new String(readBuf, 0, msg.arg1);
//                   String readMessage = new String(readBuf);
                    String readMessage = (String)msg.obj;
                    Log.d(TAG, "readMessage = " + readMessage);

                    //TODO: if message is json -> callback from RPi
                    if(isCountdown){
                        mHandler.removeCallbacks(watchDogTimeOut);
                        isCountdown = false;
                    }
                    if(mProgressDialog.isShowing()){
                        mProgressDialog.dismiss();
                        Toast.makeText(activity, R.string.config_alreadyConfig, Toast.LENGTH_SHORT).show();
                    }
                    if(hProgressDialog.isShowing()){
                        hProgressDialog.dismiss();
                        Toast.makeText(activity, R.string.config_alreadyConfig_hotspot, Toast.LENGTH_SHORT).show();
                    }
                    //remove the space at the very end of the readMessage -> eliminate space between items
                    readMessage = readMessage.substring(0,readMessage.length()-1);
                    //mConversationArrayAdapter.add(mConnectedDeviceName + ":  " + readMessage);

                    //check if ping was successful
                    if(readMessage.contains("1 packets")){
                        mConnect();
                    }
                    if(readMessage.contains("Unreachable") || readMessage.contains("failure")){
                        mOffline();
                    }
                    // Make it so text doesn't show on chat (need a better way to check multiple
                    // strings since mConversationArrayAdapter only takes messages line by line)
                    if (!readMessage.contains("1 packets")
                            && !readMessage.contains("64 bytes")
                            && !readMessage.contains("google.com")
                            && !readMessage.contains("rtt")
                            && !readMessage.trim().isEmpty()){
                        mConversationArrayAdapter.add(readMessage);
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
        }
    }

    public void mOffline(){
        Pbutton.setBackgroundResource((R.drawable.circle));
        GradientDrawable bgShape = (GradientDrawable)Pbutton.getBackground();
        bgShape.setColor(Color.RED);
    }

    public void mIdle(){
        Pbutton.setBackgroundResource((R.drawable.circle));
        GradientDrawable bgShape = (GradientDrawable)Pbutton.getBackground();
        bgShape.setColor(Color.GRAY);
    }

    public void mConnect(){
        Pbutton.setBackgroundResource((R.drawable.circle));
        GradientDrawable bgShape = (GradientDrawable)Pbutton.getBackground();
        bgShape.setColor(Color.GREEN);
    }
}

