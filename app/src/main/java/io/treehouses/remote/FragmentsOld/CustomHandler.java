package io.treehouses.remote.FragmentsOld;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Application;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

import io.treehouses.remote.MiscOld.Constants;
import io.treehouses.remote.NetworkOld.BluetoothChatService;
import io.treehouses.remote.R;

/**
 * Created by Nick on 4/9/2018.
 */

public class CustomHandler extends Handler {

    private static final String TAG = "CustomHandler";
    private static ProgressDialog mProgressDialog;
    private static String mConnectedDeviceName = null;
    private FragmentActivity fragmentActivity;
    private Application application;
    private WeakReference<Context> mContext;

    private boolean isCountdown = false;
    private boolean isRead = false;
    private ArrayAdapter<String> mConversationArrayAdapter;
    private StringBuffer mOutStringBuffer;
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothChatService mChatService = null;
    static String currentStatus = "not connected";
    private Object currentActivity;

    public CustomHandler() {

    }

    public CustomHandler(FragmentActivity fragmentActivity) {
        this.currentActivity = fragmentActivity;
    }

    public CustomHandler(Application activity, Context context) {
        this.currentActivity = activity;
        mContext = new WeakReference<Context>(context);
    }

    private final Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
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
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    isRead = false;
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    Log.d(TAG, "writeMessage = " + writeMessage);
                    mConversationArrayAdapter.add("Command:  " + writeMessage);

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
                        handleCallback(readMessage, (Activity) currentActivity);
                    } else {
                        if (isCountdown) {
                            mHandler.removeCallbacks(watchDogTimeOut);
                            isCountdown = false;
                        }
                        if (mProgressDialog.isShowing()) {
                            mProgressDialog.dismiss();
                            Toast.makeText((Context) currentActivity, R.string.config_alreadyConfig, Toast.LENGTH_SHORT).show();
                        }
                        //remove the space at the very end of the readMessage -> eliminate space between items
                        readMessage = readMessage.substring(0, readMessage.length() - 1);
                        //mConversationArrayAdapter.add(mConnectedDeviceName + ":  " + readMessage);
                        mConversationArrayAdapter.add(readMessage);
                    }

                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != currentActivity) {
                        Toast.makeText((Context) currentActivity, "Connected to "
                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != currentActivity) {
                        Toast.makeText((Context) currentActivity, msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

    private String getString(int title_connected_to, String mConnectedDeviceName) {
        return R.string.title_connected_to + " " + mConnectedDeviceName;
    }

    public void handleCallback(String str, Activity activity) {
        String result;
        String ip;
        if (isCountdown) {
            mHandler.removeCallbacks(watchDogTimeOut);
            isCountdown = false;
        }
        //enable user interaction
        mProgressDialog.dismiss();
        try {
            JSONObject mJSON = new JSONObject(str);
            result = mJSON.getString("result") == null ? "" : mJSON.getString("result");
            ip = mJSON.getString("IP") == null ? "" : mJSON.getString("IP");
            Toast.makeText((Context) currentActivity, "result: " + result + ", IP: " + ip, Toast.LENGTH_LONG).show();

            if (!result.equals("SUCCESS")) {
                Toast.makeText(activity, R.string.config_fail,
                        Toast.LENGTH_LONG).show();
            } else {
//                Toast.makeText(getActivity(), R.string.config_success,
//                            Toast.LENGTH_SHORT).show();
                Toast.makeText(activity, getString(R.string.config_success) + ip, Toast.LENGTH_LONG).show();
            }

        } catch (JSONException e) {
            // error handling
            Toast.makeText(activity, "SOMETHING WENT WRONG", Toast.LENGTH_LONG).show();
        }

    }

    private String getString(int config_success) {
        return String.valueOf(R.string.config_success);
    }

    private final Runnable watchDogTimeOut = new Runnable() {
        @Override
        public void run() {
            isCountdown = false;
            //time out
            if (mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
                Toast.makeText(fragmentActivity, "No response from RPi", Toast.LENGTH_LONG).show();
            }
        }
    };

    private void setStatus(Object arg) {
        Activity activity = null;
        if (determineActivity(application, fragmentActivity).equals("Application")) {
            activity = (Activity) currentActivity;
        } else if(determineActivity(application, fragmentActivity).equals("Fragment")){
            activity = (FragmentActivity) currentActivity;
        }
        if (null == activity) {
            return;
        }
        final ActionBar actionBar = activity.getActionBar();
        if (null == actionBar) {
            return;
        }
        if(arg instanceof Integer){
            Log.d(TAG, "actionBar.setSubtitle(resId) = " + arg);
            currentStatus = getString((Integer) arg);
            actionBar.setSubtitle((Integer) arg);
        } else if(arg instanceof CharSequence){
            Log.d(TAG, "actionBar.setSubtitle(subTitle) = " + arg);
            currentStatus = arg.toString();
            actionBar.setSubtitle((CharSequence) arg);
        }
    }

    public boolean isJson(String str) {
        try {
            new JSONObject(str);
        } catch (JSONException ex) {
            return false;
        }
        return true;
    }

    public String determineActivity(Application activity, FragmentActivity fragmentActivity) {
        if (activity != null) {
            return "Application";
        } else if (fragmentActivity != null) {
            return "Fragment";
        } else {
            return null;
        }
    }
}
