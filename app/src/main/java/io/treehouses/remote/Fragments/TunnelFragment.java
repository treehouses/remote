package io.treehouses.remote.Fragments;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.treehouses.remote.Constants;
import io.treehouses.remote.Network.BluetoothChatService;
import io.treehouses.remote.R;
import io.treehouses.remote.bases.BaseFragment;
import io.treehouses.remote.utils.Utils;

@SuppressWarnings("SpellCheckingInspection")
public class TunnelFragment extends BaseFragment {

    private static final String TAG = "BluetoothChatFragment";
    private static boolean isRead = false;
    private static boolean isCountdown = false;
    private TextView mPingStatus;
    private Button pingStatusButton;
    private ArrayAdapter<String> mConversationArrayAdapter;
    private ListView mConversationView = null;
    private Context context;
    private Button btn_status;
    private ListView terminallist;

    View view;

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
                        mConversationArrayAdapter.add("\nCommand:  " + writeMessage);
                    }
                    break;
                case Constants.MESSAGE_READ:
                    isRead = true;
                    String readMessage = (String) msg.obj;
                    Log.d(TAG, "readMessage = " + readMessage);

                    if (readMessage.contains("Error")) {
                        try {
                            listener.sendMessage("treehouses tor start");
                            Thread.sleep(300);
                            listener.sendMessage("treehouses tor add 80");
                            Thread.sleep(300);
                            listener.sendMessage("treehouses tor add 22");
                            Thread.sleep(300);
                            listener.sendMessage("treehouses tor add 2200");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
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
                    String mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_tunnel_fragment, container, false);
        ArrayList<String> listview = new ArrayList<String>();
        terminallist = view.findViewById(R.id.list_command);
        terminallist.setDivider(null);
        terminallist.setDividerHeight(0);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.tunnel_commands_list, R.id.command_textView, listview);
        terminallist.setAdapter(adapter);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mConversationView = view.findViewById(R.id.list_command);
        btn_status = view.findViewById(R.id.btn_status);
        mPingStatus = view.findViewById(R.id.pingStatus);
        pingStatusButton = view.findViewById(R.id.PING);
        Button btn_start = view.findViewById(R.id.btn_start_config);
        Button btn_execute_start = view.findViewById(R.id.btn_execute_start);
        Button btn_execute_stop = view.findViewById(R.id.btn_execute_stop);
        Button btn_execute_destroy = view.findViewById(R.id.btn_execute_destroy);
        Button btn_execute_address = view.findViewById(R.id.btn_execute_address);
        sendMessage(btn_start, btn_execute_start, btn_execute_stop, btn_execute_destroy, btn_execute_address);
    }

    @Override
    public void onStart() {
        super.onStart();
        onLoad(mHandler);
    }

    @Override
    public void checkStatusNow() {
        if (mChatService.getState() == Constants.STATE_CONNECTED) {
            mConnect();
        } else if (mChatService.getState() == Constants.STATE_NONE) {
            mOffline();
        } else {
            mIdle();
        }
    }

    @Override
    public void setupChat() {
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
                return view;
            }
        };

        mConversationView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String clickedData = (String) mConversationView.getItemAtPosition(position);
                context = getContext();
                Utils.copyToClipboard(context, clickedData);
            }
        });

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
        StringBuffer mOutStringBuffer = new StringBuffer();
    }

    public void sendMessage(Button btn_start, Button btn_execute_start, Button btn_execute_stop, Button btn_execute_destroy, Button btn_execute_address) {

        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.sendMessage("treehouses tor");
                Log.e("log", "after message was sent");
            }
        });

        btn_execute_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.sendMessage("treehouses tor");
            }
        });

        btn_execute_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.sendMessage("treehouses tor start");
            }
        });

        btn_execute_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.sendMessage("treehouses tor stop");
            }
        });

        btn_execute_destroy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.sendMessage("treehouses tor destroy");
            }
        });
    }

    private boolean isJson(String str) {
        try {
            new JSONObject(str);
        } catch (JSONException ex) {
            return false;
        }
        return true;
    }

    private void mOffline() {
        mPingStatus.setText(R.string.bStatusOffline);
        pingStatusButton.setBackgroundResource((R.drawable.circle));
        GradientDrawable bgShape = (GradientDrawable) pingStatusButton.getBackground();
        bgShape.setColor(Color.RED);
    }

    private void mIdle() {
        mPingStatus.setText(R.string.bStatusIdle);
        pingStatusButton.setBackgroundResource((R.drawable.circle));
        GradientDrawable bgShape = (GradientDrawable) pingStatusButton.getBackground();
        bgShape.setColor(Color.YELLOW);
    }

    private void mConnect() {
        mPingStatus.setText(R.string.bStatusConnected);
        pingStatusButton.setBackgroundResource((R.drawable.circle));
        GradientDrawable bgShape = (GradientDrawable) pingStatusButton.getBackground();
        bgShape.setColor(Color.GREEN);
    }
}
