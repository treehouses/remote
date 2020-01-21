package io.treehouses.remote.Fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;

import io.treehouses.remote.Constants;
import io.treehouses.remote.MainApplication;
import io.treehouses.remote.Network.BluetoothChatService;
import io.treehouses.remote.R;
import io.treehouses.remote.bases.BaseTerminalFragment;

@SuppressWarnings("SpellCheckingInspection")
public class TunnelFragment extends BaseTerminalFragment {

    private static final String TAG = "BluetoothChatFragment";
    private static boolean isRead = false;
    private TextView mPingStatus;
    private Button pingStatusButton;
    private ArrayAdapter<String> mConversationArrayAdapter;
    private ListView mConversationView = null;
    private Switch aSwitch;

    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_tunnel_fragment, container, false);
        ArrayList<String> listview = new ArrayList<String>();
        ListView terminallist = view.findViewById(R.id.list_command);
        terminallist.setDivider(null);
        terminallist.setDividerHeight(0);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.tunnel_commands_list, R.id.command_textView, listview);
        terminallist.setAdapter(adapter);

        aSwitch = view.findViewById(R.id.switchNotification);

        onSwitchChecked();

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mConversationView = view.findViewById(R.id.list_command);
        mPingStatus = view.findViewById(R.id.pingStatus);
        pingStatusButton = view.findViewById(R.id.PING);

        Button btn_start = view.findViewById(R.id.btn_start_config);
        Button btn_execute_start = view.findViewById(R.id.btn_execute_start);
        Button btn_execute_stop = view.findViewById(R.id.btn_execute_stop);
        Button btn_execute_destroy = view.findViewById(R.id.btn_execute_destroy);
        Button btn_execute_address = view.findViewById(R.id.btn_execute_address);

        btn_start.setOnClickListener(v -> listener.sendMessage("treehouses tor"));
        btn_execute_address.setOnClickListener(v -> {
            listener.sendMessage("treehouses tor");
        });
        btn_execute_start.setOnClickListener(v -> listener.sendMessage("treehouses tor start"));
        btn_execute_stop.setOnClickListener(v -> listener.sendMessage("treehouses tor stop"));
        btn_execute_destroy.setOnClickListener(v -> listener.sendMessage("treehouses tor destroy"));
    }

    @Override
    public void onStart() {
        super.onStart();
        onLoad(mHandler);
    }

    @Override
    public void setupChat() {
        Log.e("tag", "LOG setupChat()");
        LayoutInflater inflater = getLayoutInflater();
        // Initialize the array adapter for the conversation thread
        getViewFunction();

        copyToList(mConversationView, getContext());

        mConversationView.setAdapter(mConversationArrayAdapter);

        // Initialize the BluetoothChatService to perform bluetooth connections
        if (mChatService.getState() == Constants.STATE_NONE) {
            mChatService = new BluetoothChatService(mHandler);
        }
        // Initialize the buffer for outgoing messages
        new StringBuilder();
    }

    private void getViewFunction() {
        mConversationArrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.message, MainApplication.getTunnelList()) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                view = getViews(view, isRead);
                return view;
            }
        };
    }

    private void onSwitchChecked() {
        aSwitch.setOnClickListener(v -> {
            if (aSwitch.isChecked()) {
                listener.sendMessage("treehouses tor notice on");
            } else {
                listener.sendMessage("treehouses tor notice off");
            }
        });
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
                    if (msg.arg1 == Constants.STATE_LISTEN || msg.arg1 == Constants.STATE_NONE) { idle(mPingStatus, pingStatusButton); }
                    break;
                case Constants.MESSAGE_WRITE:
                    isRead = false;
                    handlerCaseWrite(TAG, mConversationArrayAdapter, msg);
                    break;
                case Constants.MESSAGE_READ:
                    String readMessage = (String) msg.obj;
                    isRead = true;
                    configConditions(readMessage);
                    handlerCaseRead(readMessage, mPingStatus, pingStatusButton);
                    filterMessages(readMessage, mConversationArrayAdapter, MainApplication.getTunnelList());
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    Activity activity = getActivity();
                    handlerCaseName(msg, activity);
                    break;
                case Constants.MESSAGE_TOAST:
                    handlerCaseToast(msg);
                    break;
            }
        }
    };

    @Override
    public void onResume(){
        Log.e("CHECK STATUS", "" + mChatService.getState());
        checkStatus(mChatService, mPingStatus, pingStatusButton);
        super.onResume();
    }
}
