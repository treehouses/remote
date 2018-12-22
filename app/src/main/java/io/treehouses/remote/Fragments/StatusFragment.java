package io.treehouses.remote.Fragments;

import androidx.fragment.app.FragmentActivity;
import io.treehouses.remote.InitialActivity;
import io.treehouses.remote.MiscOld.Constants;
import io.treehouses.remote.Network.BluetoothChatService;
import io.treehouses.remote.R;

import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;

public class StatusFragment extends androidx.fragment.app.Fragment {

    public StatusFragment(){}

    View view;

    String readMessage = "";

    private static final String TAG = "BluetoothChatFragment";

    //current connection status
    static String currentStatus = "not connected";

    private BluetoothChatService mChatService = null;
    /**
     * Name of the connected device
     */
    private String mConnectedDeviceName = null;

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
    private static boolean isCountdown = false;

    Button btStatus, wifiStatus, btRPIName, rpiType;

    ArrayList<Button> allButtons;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view =  inflater.inflate(R.layout.activity_status_fragment, container, false);

        btStatus = (Button) view.findViewById(R.id.btStatus);
        wifiStatus = (Button) view.findViewById(R.id.wifiStatus);
        btRPIName = (Button) view.findViewById(R.id.rpiName);
        rpiType = (Button) view.findViewById(R.id.rpiType);

//        allButtons.add(btStatus);
//        allButtons.add(wifiStatus);
//        allButtons.add(btRPIName);
//        allButtons.add(rpiType);

        InitialActivity initialActivity = new InitialActivity();
        BluetoothChatService chatService = initialActivity.getChatService();

        mChatService = chatService;
        mChatService.updateHandler(mHandler);


        checkStatusNow(view);
        String ping = "ping -c 1 google.com";
//        byte[] pSend = ping.getBytes();
//        mChatService.write(pSend);



        ping = "treehouses detectrpi";
        byte[] pSend1 = ping.getBytes();
        mChatService.write(pSend1);
//        initialActivity.sendMessage("treehouses detectrpi");


        return view;
    }

    private void checkStatusNow(View view){
        if(mChatService.getState() == Constants.STATE_CONNECTED){
            btStatus.setBackgroundResource((R.drawable.circle));
            GradientDrawable bgShape = (GradientDrawable)btStatus.getBackground();
            bgShape.setColor(Color.GREEN);
            wifiStatus.setBackgroundResource((R.drawable.circle));
            bgShape = (GradientDrawable)wifiStatus.getBackground();
            bgShape.setColor(Color.GREEN);
            btRPIName.setBackgroundResource((R.drawable.circle));
            bgShape = (GradientDrawable)btRPIName.getBackground();
            bgShape.setColor(Color.GREEN);
            rpiType.setBackgroundResource((R.drawable.circle));
            bgShape = (GradientDrawable)rpiType.getBackground();
            bgShape.setColor(Color.GREEN);
        }else{
            btStatus.setBackgroundResource((R.drawable.circle));
            GradientDrawable bgShape = (GradientDrawable)btStatus.getBackground();
            bgShape.setColor(Color.RED);
            wifiStatus.setBackgroundResource((R.drawable.circle));
            bgShape = (GradientDrawable)wifiStatus.getBackground();
            bgShape.setColor(Color.RED);
            btRPIName.setBackgroundResource((R.drawable.circle));
            bgShape = (GradientDrawable)btRPIName.getBackground();
            bgShape.setColor(Color.RED);
            rpiType.setBackgroundResource((R.drawable.circle));
            bgShape = (GradientDrawable)rpiType.getBackground();
            bgShape.setColor(Color.RED);
        }
        Log.e("DEVICE",""+mConnectedDeviceName);
    }

//    /**
//     * The Handler that gets information back from the BluetoothChatService
//     */
    public final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            FragmentActivity activity = getActivity();
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    checkStatusNow(view);
                    break;
                case Constants.MESSAGE_READ:
//                    isRead = true;
//                    byte[] readBuf = (byte[]) msg.obj;
//                     construct a string from the valid bytes in the buffer
//                    String readMessage = new String(readBuf, 0, msg.arg1);
//                    String readMessage = new String(readBuf);
                    readMessage = (String)msg.obj;
                    Log.d(TAG, "readMessage = " + readMessage);
                    //TODO: if message is json -> callback from RPi
//                    if(isJson(readMessage)){
//                        //handleCallback(readMessage);
//                    }else{
//                        if(isCountdown){
//                            //mHandler.removeCallbacks(watchDogTimeOut);
//                            isCountdown = false;
//                        }
//                        //remove the space at the very end of the readMessage -> eliminate space between items
//                        readMessage = readMessage.substring(0,readMessage.length()-1);
//                        //mConversationArrayAdapter.add(mConnectedDeviceName + ":  " + readMessage);
//
//                        //check if ping was successful
//                        if(readMessage.contains("1 packets")){
//                            mConnect();
//                        }
//                        if(readMessage.contains("Unreachable") || readMessage.contains("failure")){
//                            mOffline();
//                        }
//                        //make it so text doesn't show on chat (need a better way to check multiple strings since mConversationArrayAdapter only takes messages line by line)
//                        if (!readMessage.contains("1 packets") && !readMessage.contains("64 bytes") && !readMessage.contains("google.com") &&
//                                !readMessage.contains("rtt") && !readMessage.trim().isEmpty()){
//                            mConversationArrayAdapter.add(readMessage);
//                        }
//                    }
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
//                    if (null != activity) {
//                        Toast.makeText(activity, "Connected to "
//                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
//                    }
                    break;
            }
        }
    };
}
