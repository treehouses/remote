package io.treehouses.remote.Fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import io.treehouses.remote.Terminal;
import io.treehouses.remote.bases.BaseFragment;
import io.treehouses.remote.utils.Utils;

@SuppressWarnings("SpellCheckingInspection")
public class TunnelFragment extends BaseFragment {

    private static final String TAG = "BluetoothChatFragment";
    private static boolean isRead = false;
    private static TunnelFragment instance = null;
    private TextView mPingStatus;
    private Button pingStatusButton;
    private ArrayAdapter<String> mConversationArrayAdapter;
    private ListView mConversationView = null;
    private Button btn_status;

    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_tunnel_fragment, container, false);

        instance = this;

        ArrayList<String> listview = new ArrayList<String>();
        ListView terminallist = view.findViewById(R.id.list_command);
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
        Terminal.checkStatus(mChatService);
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
                Terminal.getView(view, isRead);
                return view;
            }
        };

        Terminal.copyToList(mConversationView, getContext());

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

    public static TunnelFragment getInstance() {
        return instance;
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

    public void mOffline() {
        Terminal.offline(mPingStatus, pingStatusButton);
    }

    public void mIdle() {
        Terminal.idle(mPingStatus, pingStatusButton);
    }

    public void mConnect() {
        Terminal.connect(mPingStatus, pingStatusButton);
    }

    private void configConditions(String readMessage) {
        if (readMessage.trim().contains("Error")) {
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
    }

    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case Constants.STATE_LISTEN:
                        case Constants.STATE_NONE:
                            mIdle();
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    Terminal.handlerCaseWrite(isRead, TAG, mConversationArrayAdapter, msg);
                    break;
                case Constants.MESSAGE_READ:
                    isRead = true;
                    String readMessage = (String) msg.obj;
                    Log.d(TAG, "readMessage = " + readMessage);

                   configConditions(readMessage);

                    //TODO: if message is json -> callback from RPi
                    if (isJson(readMessage)) {
                        //handleCallback(readMessage);
                    } else {
                       Terminal.isPingSuccesfull(readMessage);

                        //make it so text doesn't show on chat (need a better way to check multiple strings since mConversationArrayAdapter only takes messages line by line)
                        if (!readMessage.contains("1 packets") && !readMessage.contains("64 bytes") && !readMessage.contains("google.com") && !readMessage.contains("rtt") && !readMessage.trim().isEmpty()) {
                            mConversationArrayAdapter.add(readMessage);
                        }
                    }
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    Activity activity = getActivity();
                    Terminal.handlerCaseName(msg, activity);
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

}
