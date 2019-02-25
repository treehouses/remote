package io.treehouses.remote.Fragments;

import androidx.fragment.app.FragmentActivity;
import io.treehouses.remote.InitialActivity;
import io.treehouses.remote.MiscOld.Constants;
import io.treehouses.remote.Network.BluetoothChatService;
import io.treehouses.remote.R;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class StatusFragment extends androidx.fragment.app.Fragment {

    public StatusFragment(){}

    View view;

    String readMessage = "";

    static boolean processing = false;
    static String processedMessage = "";

    private static final String TAG = "StatusFragment";

    //current connection status
    static String currentStatus = "not connected";

    private BluetoothChatService mChatService = null;
    /**
     * Name of the connected device
     */
    private String mConnectedDeviceName = null;
    private String deviceName = "";

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

    ImageView  wifiStatus, btRPIName, rpiType;

    ImageView btStatus, ivUpgrad;

    TextView tvStatus, tvStatus1, tvStatus2, tvStatus3, tvUpgrade;

    ArrayList<Button> allButtons;

    List<String> outs = new ArrayList<String>();

    Boolean wifiStatusVal = false;

    Button upgrade;

    ProgressDialog pd;

    Boolean updateRightNow = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view =  inflater.inflate(R.layout.activity_status_fragment, container, false);

        initializeUIElements(view);

        InitialActivity initialActivity = new InitialActivity();
        BluetoothChatService chatService = initialActivity.getChatService();

        mChatService = chatService;
        mChatService.updateHandler(mHandler);

        deviceName = mChatService.getConnectedDeviceName();
        Log.e("STATUS","device name: "+deviceName);
        if(mChatService.getState() == Constants.STATE_CONNECTED){
            btStatus.setImageDrawable(getResources().getDrawable(R.drawable.tick));
        }
        checkStatusNow(view);

        String ping = "treehouses detectrpi";
        byte[] pSend1 = ping.getBytes();
        mChatService.write(pSend1);

        return view;
    }

    public void initializeUIElements(View view){
        btStatus = view.findViewById(R.id.btStatus);
        wifiStatus = view.findViewById(R.id.wifiStatus);
        btRPIName = view.findViewById(R.id.rpiName);
        rpiType = view.findViewById(R.id.rpiType);
        ivUpgrad = view.findViewById(R.id.upgradeCheck);


        tvStatus = view.findViewById(R.id.tvStatus);
        tvStatus1 = view.findViewById(R.id.tvStatus1);
        tvStatus2 = view.findViewById(R.id.tvStatus2);
        tvStatus3 = view.findViewById(R.id.tvStatus3);
        tvUpgrade = view.findViewById(R.id.tvUpgradeCheck);
        upgrade = view.findViewById(R.id.upgrade);
        upgrade.setVisibility(View.GONE);

        upgrade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writeToRPI("treehouses upgrade");
                updateRightNow = true;
                pd = ProgressDialog.show(getActivity(), "Updating...", "Please wait a few seconds...");
            }
        });
    }

    private void checkStatusNow(View view){
        Log.e("DEVICE",""+mConnectedDeviceName);
    }

    public void updateStatus(){
        setRPIDeviceName();
        if(outs.size() == 1){
            setRPIType();
        }
        if(outs.size() == 2){
            checkWifiStatus();
        }
        if(outs.size() == 3){
            checkUpgradeStatus();
        }
        if(outs.size() == 4){
            outs.remove(2);
            outs.remove(2);
            checkWifiStatus();
        }
    }

    void writeToRPI(String ping){
        byte[] pSend = ping.getBytes();
        mChatService.write(pSend);
    }

    void setRPIDeviceName(){
        tvStatus2.setText("Connected RPI Name: "+deviceName);
        btRPIName.setImageDrawable(getResources().getDrawable(R.drawable.tick));
    }

    void setRPIType(){
        tvStatus3.setText("RPI Type: "+outs.get(0));
        rpiType.setImageDrawable(getResources().getDrawable(R.drawable.tick));
        writeToRPI("treehouses internet");
    }

    void checkWifiStatus(){
        tvStatus1.setText("RPI Wifi Connection: "+outs.get(1));
        Log.e("StatusFragment","**"+outs.get(1)+"**"+outs.get(1).equals("true "));
        if(outs.get(1).equals("true ")){
            Log.e("StatusFragment","TRUE");
            wifiStatusVal = true;
            wifiStatus.setImageDrawable(getResources().getDrawable(R.drawable.tick));
        }
        if(wifiStatusVal){
            writeToRPI("treehouses upgrade --check");
        }else{
            tvUpgrade.setText("Upgrade Status: NO INTERNET");
            upgrade.setVisibility(View.GONE);
        }
    }

    void checkUpgradeStatus(){
        if(updateRightNow){
            updateRightNow = false;
            pd.dismiss();
        }
        if(outs.get(2).equals("false ")){
            ivUpgrad.setImageDrawable(getResources().getDrawable(R.drawable.tick));
            tvUpgrade.setText("Upgrade Status: Latest Version");
            upgrade.setVisibility(View.GONE);
        }else{
            ivUpgrad.setImageDrawable(getResources().getDrawable(R.drawable.tick_png));
            tvUpgrade.setText("Upgrade Status: Required for Version: "+outs.get(2).substring(4));
            upgrade.setVisibility(View.VISIBLE);
        }
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
                case Constants.MESSAGE_WRITE:
                    Log.e("StatusFragment", "WRITE");
//                    isRead = false;
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
//                    if(!writeMessage.contains("google.com")) {
//                        Log.d(TAG, "writeMessage = " + writeMessage);
//                        mConversationArrayAdapter.add("Command:  " + writeMessage);
//                    }
                    Log.d(TAG, "writeMessage = " + writeMessage);
                    processing = true;
                    break;
                case Constants.MESSAGE_READ:
                    Log.e("StatusFragment", "READ");
//                    isRead = true;
//                    byte[] readBuf = (byte[]) msg.obj;
//                     construct a string from the valid bytes in the buffer
//                    String readMessage = new String(readBuf, 0, msg.arg1);
//                    String readMessage = new String(readBuf);
                    readMessage = (String)msg.obj;
                    Log.d(TAG, "readMessage = " + readMessage);
//                    processedMessage = readMessage;
                    outs.add(readMessage);
//                    for(String out : outs){
//                        Log.e("OUT", out);
//                    }
                    updateStatus();
                    processing = false;
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
