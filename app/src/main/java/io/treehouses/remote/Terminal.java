package io.treehouses.remote;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import io.treehouses.remote.Fragments.TunnelFragment;
import io.treehouses.remote.Network.BluetoothChatService;
import io.treehouses.remote.utils.Utils;

public class Terminal {

    public static void handlerCaseWrite(Boolean isRead, String TAG, ArrayAdapter<String> mConversationArrayAdapter, Message msg) {
        isRead = false;
        byte[] writeBuf = (byte[]) msg.obj;
        // construct a string from the buffer
        String writeMessage = new String(writeBuf);
        if (!writeMessage.contains("google.com")) {
            Log.d(TAG, "writeMessage = " + writeMessage);
            mConversationArrayAdapter.add("\nCommand:  " + writeMessage);
        }
    }

    public static void handlerCaseName(Message msg, Activity activity ) {
        // save the connected device's name
        String mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
        if (null != activity) {
            Toast.makeText(activity, "Connected to "
                    + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
        }
    }

    public static View getView(View view, Boolean isRead) {
        TextView consoleView = view.findViewById(R.id.listItem);
        if (isRead) {
            consoleView.setTextColor(Color.BLUE);
        } else {
            consoleView.setTextColor(Color.RED);
        }
        return view;
    }

    public static void bgResource(Button pingStatusButton, int color) {
        pingStatusButton.setBackgroundResource((R.drawable.circle));
        GradientDrawable bgShape = (GradientDrawable) pingStatusButton.getBackground();
        bgShape.setColor(color);
    }

    public static void offline(TextView mPingStatus, Button pingStatusButton) {
        mPingStatus.setText(R.string.bStatusOffline);
        bgResource(pingStatusButton, Color.RED);
    }

    public static void idle(TextView mPingStatus, Button pingStatusButton) {
        mPingStatus.setText(R.string.bStatusIdle);
        bgResource(pingStatusButton, Color.YELLOW);
    }

    public static void connect(TextView mPingStatus, Button pingStatusButton) {
        mPingStatus.setText(R.string.bStatusConnected);
        bgResource(pingStatusButton, Color.GREEN);
    }

    public static void isPingSuccesfull(String readMessage, TextView mPingStatus, Button pingStatusButton) {
        readMessage = readMessage.trim();

        //check if ping was successful
        if (readMessage.contains("1 packets")) {
            connect(mPingStatus, pingStatusButton);
        }
        if (readMessage.contains("Unreachable") || readMessage.contains("failure")) {
            offline(mPingStatus, pingStatusButton);
        }
    }

    public static void copyToList(final ListView mConversationView, final Context context) {
        mConversationView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String clickedData = (String) mConversationView.getItemAtPosition(position);
                Utils.copyToClipboard(context, clickedData);
            }
        });
    }

    public static void checkStatus(BluetoothChatService mChatService, TextView mPingStatus, Button pingStatusButton) {
        if (mChatService.getState() == Constants.STATE_CONNECTED) {
            connect(mPingStatus, pingStatusButton);
        } else if (mChatService.getState() == Constants.STATE_NONE) {
            offline(mPingStatus, pingStatusButton);
        } else {
            idle(mPingStatus, pingStatusButton);
        }
    }
}
