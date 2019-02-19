package io.treehouses.remote.Fragments;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

import io.treehouses.remote.InitialActivity;
import io.treehouses.remote.MiscOld.Constants;
import io.treehouses.remote.Network.BluetoothChatService;
import io.treehouses.remote.R;

public class TunnelFragment extends androidx.fragment.app.Fragment {

    private static final String TAG = "BluetoothChatFragment";
    private static boolean isRead = false;
    private static boolean isCountdown = false;
    View view;
    View terminal;
    Context context;
    Button btn_start;
    Button btn_execute_start;
    Button btn_execute_stop;
    Button btn_execute_destroy;
    String output;
    InitialActivity initialActivity;
    TerminalFragment terminalFragment;
    ListView list;
    Button btn_status;
    TextView status;
    Boolean message = false;
    String[] split = {};
    String message_output;
    Boolean _output = false;
    int printedLineCount = 0;
    ArrayList<String> message_array_list = new ArrayList<String>(Arrays.asList(split));
    ArrayList<String> message_array_listMaster = new ArrayList<String>(Arrays.asList(split));
    int i;
    private StringBuffer mOutStringBuffer;
    private String mConnectedDeviceName = null;
    private TextView mPingStatus;
    private Button pingStatusButton;
    private BluetoothAdapter mBluetoothAdapter = null;
    private ArrayAdapter<String> mConversationArrayAdapter;
    public final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case Constants.STATE_LISTEN:
                        case Constants.STATE_NONE:
//                            setStatus(R.string.title_not_connected);
                            mIdle();
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    isRead = false;
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    if (!writeMessage.contains("google.com")) {
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
                        //handleCallback(readMessage);
                    } else {
                        if (isCountdown) {
                            //mHandler.removeCallbacks(watchDogTimeOut);
                            isCountdown = false;
                        }
                        //remove the space at the very end of the readMessage -> eliminate space between items
                        readMessage = readMessage.substring(0, readMessage.length() - 1);
                        //mConversationArrayAdapter.add(mConnectedDeviceName + ":  " + readMessage);

                        //check if ping was successful
                        if (readMessage.contains("1 packets")) {
                            mConnect();
                        }
                        if (readMessage.contains("Unreachable") || readMessage.contains("failure")) {
                            mOffline();
                        }
                        //make it so text doesn't show on chat (need a better way to check multiple strings since mConversationArrayAdapter only takes messages line by line)
                        if (!readMessage.contains("1 packets") && !readMessage.contains("64 bytes") && !readMessage.contains("google.com") &&
                                !readMessage.contains("rtt") && !readMessage.trim().isEmpty()) {
                            mConversationArrayAdapter.add(readMessage);
                        }
                    }
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != getActivity()) {
                        Toast.makeText(getActivity(), "Connected to "
                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != getActivity()) {
                        Toast.makeText(getActivity(), msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };
    private BluetoothChatService mChatService = null;
    private ListView mConversationView = null;

    public TunnelFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_tunnel_fragment, container, false);

        initialActivity = new InitialActivity();

        ArrayList<String> listview = new ArrayList<String>();

        list = view.findViewById(R.id.list_command);
        list.setDivider(null);
        list.setDividerHeight(0);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.tunnel_commands_list, R.id.command_textView, listview);
        list.setAdapter(adapter);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            Toast.makeText(getActivity(), "Bluetooth is not available", Toast.LENGTH_LONG).show();
            getActivity().finish();
        }

//        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                String msg = "treehouses tor";
//                initialActivity.sendMessage(msg);
//            }
//        });
        sendMessage();

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mConversationView = view.findViewById(R.id.list_command);
        btn_status = view.findViewById(R.id.btn_status);
        mPingStatus = view.findViewById(R.id.pingStatus);
        pingStatusButton = view.findViewById(R.id.PING);
        btn_execute_start = view.findViewById(R.id.btn_execute_start);
        btn_execute_stop = view.findViewById(R.id.btn_execute_stop);
        btn_execute_destroy = view.findViewById(R.id.btn_execute_destroy);
    }

    @Override
    public void onStart() {
        super.onStart();

//        if((new RPIDialogFragment()).equals(null)){
//            Log.e("TERMINAL", "NULL");
//        }
        initialActivity = new InitialActivity();
//        RPIDialogFragment initialActivity = new RPIDialogFragment();
//        BluetoothDevice device = initialActivity.getMainDevice();
        mChatService = initialActivity.getChatService();

//        if(mChatService == null){
//            showRPIDialog();
//        }else{
        mChatService.updateHandler(mHandler);
        Log.e("TERMINAL mChatService", "" + mChatService.getState());
        checkStatusNow();
//        }
//        Log.e("DEVICE ", ""+device.getName());
//         If BT is not on, request that it be enabled.
//         setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, Constants.REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else {
            setupChat();
//            mChatService.connect(device,true);
        }
    }

    private void checkStatusNow() {
        if (mChatService.getState() == Constants.STATE_CONNECTED) {
            mConnect();

        } else if (mChatService.getState() == Constants.STATE_NONE) {
            mOffline();
        } else {
            mIdle();
        }
    }

    public void setupChat(String... msg) {
        Log.e("tag", "LOG setupChat()");
        LayoutInflater inflater = getLayoutInflater();
        // Initialize the array adapter for the conversation thread
        mConversationArrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.message) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView consoleView = view.findViewById(R.id.listItem);
                if (isRead) {
                    consoleView.setTextColor(Color.BLUE);
                } else {
                    consoleView.setTextColor(Color.RED);
                }

                if (message) {
                    message_output = consoleView.getText().toString();

                    //message_output += "_+_";
                    //message_array_list.add(message_output);
//                    message = false;
                    // Log.e("tag", "LOG Message  " + message_output);

                }
                String msg = "Success: the tor service has been started";
                return view;
            }

        };

        mConversationArrayAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                for (int x = printedLineCount; x < mConversationArrayAdapter.getCount(); x++) {
                    message_array_listMaster.add(mConversationArrayAdapter.getItem(x));
                    printedLineCount++;
                }
            }
        });

        mConversationView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                for (int j = 0; j < message_array_listMaster.size(); j++) {
                    String array_elements = message_array_listMaster.get(j).toString().trim();
                    if (array_elements.contains("Command") || array_elements.contains("Command:") || array_elements.contains(" Command:")) {
                        message_array_listMaster.remove(j);
                    } else {
                        //Log.e("tag", "LOG leo Out " + message_array_listMaster.get(j));
                    }
                }
                Log.e("tag", "LOG leo  Items " + message_array_listMaster);
            }
        });

        mConversationArrayAdapter.notifyDataSetChanged();
        mConversationView.setAdapter(mConversationArrayAdapter);

        btn_status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("CHECK STATUS", "" + mChatService.getState());
                checkStatusNow();
            }
        });

        // Initialize the BluetoothChatService to perform bluetooth connections
        if (mChatService.getState() == Constants.STATE_NONE) {
            mChatService = new BluetoothChatService(mHandler);
        }
        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer();
    }

    public void sendMessage() {
        btn_start = view.findViewById(R.id.btn_start_config);
        btn_execute_start = view.findViewById(R.id.btn_execute_start);
        btn_execute_stop = view.findViewById(R.id.btn_execute_stop);
        btn_execute_destroy = view.findViewById(R.id.btn_execute_destroy);

        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initialActivity.sendMessage("treehouses tor");
                Log.e("log", "after message was sent");
                message = true;
                _output = true;


//                initialActivity.sendMessage("treehouses tor");
//                initialActivity.sendMessage("treehouses tor add 80");
//                initialActivity.sendMessage("treehouses tor add 22");
//                initialActivity.sendMessage("treehouses tor add 2200");

                //Toast.makeText(getContext(), output, Toast.LENGTH_LONG).show();
            }
        });

        btn_execute_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initialActivity.sendMessage("treehouses tor start");
            }
        });

        btn_execute_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initialActivity.sendMessage("treehouses tor stop");
            }
        });

        btn_execute_destroy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initialActivity.sendMessage("treehouses tor destroy");
            }
        });
    }

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
                break;
            case Constants.REQUEST_DIALOG_FRAGMENT_CHPASS:
                if (resultCode == Activity.RESULT_OK) {

                    //get password change request
                    String chPWD = data.getStringExtra("password") == null ? "" : data.getStringExtra("password");

                    //store password and command
                    String password = "treehouses password " + chPWD;

                    Log.d(TAG, "back from change password");

                    //send password to command line interface
                    initialActivity.sendMessage(password);

                } else {
                    Log.d(TAG, "back from change password, fail");
                }
                break;
        }
    }

    public void showChPasswordDialog() {
        // Create an instance of the dialog fragment and show it
        androidx.fragment.app.DialogFragment dialogFrag = ChPasswordDialogFragment.newInstance(123);
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

    public void mOffline() {
        mPingStatus.setText(R.string.bStatusOffline);
        pingStatusButton.setBackgroundResource((R.drawable.circle));
        GradientDrawable bgShape = (GradientDrawable) pingStatusButton.getBackground();
        bgShape.setColor(Color.RED);
    }

    public void mIdle() {
        mPingStatus.setText(R.string.bStatusIdle);
        pingStatusButton.setBackgroundResource((R.drawable.circle));
        GradientDrawable bgShape = (GradientDrawable) pingStatusButton.getBackground();
        bgShape.setColor(Color.YELLOW);
    }

    public void mConnect() {
        mPingStatus.setText(R.string.bStatusConnected);
        pingStatusButton.setBackgroundResource((R.drawable.circle));
        GradientDrawable bgShape = (GradientDrawable) pingStatusButton.getBackground();
        bgShape.setColor(Color.GREEN);
    }


}
