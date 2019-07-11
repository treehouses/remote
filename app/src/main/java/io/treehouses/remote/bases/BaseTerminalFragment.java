package io.treehouses.remote.bases;

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

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;

import io.treehouses.remote.Constants;
import io.treehouses.remote.MainApplication;
import io.treehouses.remote.Network.BluetoothChatService;
import io.treehouses.remote.R;
import io.treehouses.remote.utils.Utils;

public class BaseTerminalFragment extends BaseFragment{

    public String handlerCaseWrite(String TAG, ArrayAdapter<String> mConversationArrayAdapter, Message msg) {

        byte[] writeBuf = (byte[]) msg.obj;
        // construct a string from the buffer
        String writeMessage = new String(writeBuf);
        if (!writeMessage.contains("google.com")) {
            Log.d(TAG, "writeMessage = " + writeMessage);
            mConversationArrayAdapter.add("\nCommand:  " + writeMessage);
        }
        return writeMessage;
    }

    public void handlerCaseName(Message msg, Activity activity ) {
        // save the connected device's name
        String mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
        if (null != activity) {
            Toast.makeText(activity, "Connected to "
                    + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
        }
    }

    public void handlerCaseToast(Message msg) {
        if (null != getActivity()) {
            Toast.makeText(getActivity(), msg.getData().getString(Constants.TOAST), Toast.LENGTH_SHORT).show();
        }
    }

    public View getViews(View view, Boolean isRead) {
        TextView consoleView = view.findViewById(R.id.listItem);
        if (isRead) {
            consoleView.setTextColor(Color.BLUE);
        } else {
            consoleView.setTextColor(Color.RED);
        }
        return view;
    }

    private void bgResource(Button pingStatusButton, int color) {
        pingStatusButton.setBackgroundResource((R.drawable.circle));
        GradientDrawable bgShape = (GradientDrawable) pingStatusButton.getBackground();
        bgShape.setColor(color);
    }

    protected void offline(TextView mPingStatus, Button pingStatusButton) {
        mPingStatus.setText(R.string.bStatusOffline);
        bgResource(pingStatusButton, Color.RED);
    }

    public void idle(TextView mPingStatus, Button pingStatusButton) {
        mPingStatus.setText(R.string.bStatusIdle);
        bgResource(pingStatusButton, Color.YELLOW);
    }

    public void connect(TextView mPingStatus, Button pingStatusButton) {
        mPingStatus.setText(R.string.bStatusConnected);
        bgResource(pingStatusButton, Color.GREEN);
    }



    protected void copyToList(final ListView mConversationView, final Context context) {
        mConversationView.setOnItemClickListener((parent, view, position, id) -> {
            String clickedData = (String) mConversationView.getItemAtPosition(position);
            Utils.copyToClipboard(context, clickedData);
        });
    }

    private void checkStatus(BluetoothChatService mChatService, TextView mPingStatus, Button pingStatusButton) {
        if (mChatService.getState() == Constants.STATE_CONNECTED) {
            connect(mPingStatus, pingStatusButton);
        } else if (mChatService.getState() == Constants.STATE_NONE) {
            offline(mPingStatus, pingStatusButton);
        } else {
            idle(mPingStatus, pingStatusButton);
        }
    }

    protected void buttonOnClick(Button button, final BluetoothChatService mChatService, final TextView mPingStatus, final Button pingStatusButton) {
        button.setOnClickListener(v -> {
            Log.e("CHECK STATUS", "" + mChatService.getState());
            checkStatus(mChatService, mPingStatus, pingStatusButton);
        });
    }

    protected void filterMessages(String readMessage, ArrayAdapter mConversationArrayAdapter, ArrayList list) {
        //make it so text doesn't show on chat (need a better way to check multiple strings since mConversationArrayAdapter only takes messages line by line)
        if (!readMessage.contains("1 packets") && !readMessage.contains("64 bytes") && !readMessage.contains("google.com") && !readMessage.contains("rtt") && !readMessage.trim().isEmpty()) {
            list.add(readMessage);
            mConversationArrayAdapter.notifyDataSetChanged();
        }
    }

    protected void isPingSuccesfull(String readMessage, TextView mPingStatus, Button pingStatusButton) {
        readMessage = readMessage.trim();

        //check if ping was successful
        if (readMessage.contains("1 packets")) {
            connect(mPingStatus, pingStatusButton);
        }
        if (readMessage.contains("Unreachable") || readMessage.contains("failure")) {
            offline(mPingStatus, pingStatusButton);
        }
    }

    protected void handlerCaseRead(String readMessage, TextView mPingStatus, Button pingStatusButton) {

        Log.d("TAG", "readMessage = " + readMessage);

        //TODO: if message is json -> callback from RPi
        if (!isJson(readMessage)) {
            isPingSuccesfull(readMessage, mPingStatus, pingStatusButton);
        }
    }

    private boolean isJson(String readMessage) {
        try {
            new JSONObject(readMessage);
        } catch (JSONException ex) {
            return false;
        }
        return true;
    }
}
