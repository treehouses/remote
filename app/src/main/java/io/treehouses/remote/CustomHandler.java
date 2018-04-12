package io.treehouses.remote;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Application;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

/**
 * Created by Nick on 4/9/2018.
 */

public class CustomHandler extends Handler {

    private static final String TAG = "CustomHandler";
    private static ProgressDialog mProgressDialog;
    private static ProgressDialog hProgressDialog;
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

    private String networkStatus;

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
                        case BluetoothChatService.STATE_CONNECTED:
                            setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                            try {
                                // Sleep for 0.5 sec to make sure thread is ready
                                Thread.sleep(500);
                                String[] firstRun = {
                                        "cd boot\n",
                                        "cat version.txt\n",
                                        "pirateship detectrpi\n",
                                        "cd ..\n"};
                                for (int i = 0; i <= 3; i++) {
                                    mChatService.write(firstRun[i].getBytes());
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            mConversationArrayAdapter.clear();
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            setStatus(R.string.title_connecting);
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
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
                        Toast.makeText(fragmentActivity, R.string.config_alreadyConfig, Toast.LENGTH_SHORT).show();
                    }
                    if(hProgressDialog.isShowing()){
                        hProgressDialog.dismiss();
                        Toast.makeText(fragmentActivity, R.string.config_alreadyConfig_hotspot, Toast.LENGTH_SHORT).show();
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

    public void mOffline(){
        BluetoothChatFragment.Pbutton.setBackgroundResource((R.drawable.circle));
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