package io.treehouses.remote.Fragments;

import androidx.fragment.app.FragmentActivity;
import io.treehouses.remote.InitialActivity;
import io.treehouses.remote.MiscOld.Constants;
import io.treehouses.remote.Network.BluetoothChatService;
import io.treehouses.remote.R;

import android.app.Fragment;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view =  inflater.inflate(R.layout.activity_status_fragment, container, false);

//        ArrayList<String> list = new ArrayList<String>();
//        list.add("Bluetooth RPI Connection");
//        list.add("RPI Wifi Connection:");
//        list.add("Connected RPI Name:");
//        list.add("RPI Type:");
//        list.add("Upgrade Status:");
//
//        ArrayList<Drawable> list1 = new ArrayList<Drawable>();
//        list1.add(getResources().getDrawable(R.drawable.tick));
//        list1.add("RPI Wifi Connection:");
//        list1.add("Connected RPI Name:");
//        list1.add("RPI Type:");
//        list1.add("Upgrade Status:");
//
//        ListView listView = view.findViewById(R.id.listView);
////        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, list);
//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.cardlist, R.id.tvStatus, list, R.id.ivStatus, list1);
//        listView.setAdapter(adapter);

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

//        allButtons.add(btStatus);
//        allButtons.add(wifiStatus);
//        allButtons.add(btRPIName);
//        allButtons.add(rpiType);

        InitialActivity initialActivity = new InitialActivity();
        BluetoothChatService chatService = initialActivity.getChatService();

        mChatService = chatService;
        mChatService.updateHandler(mHandler);

        deviceName = initialActivity.getDeviceName();
        Log.e("STATUS","device name: "+deviceName);
        if(mChatService.getState() == Constants.STATE_CONNECTED){
            btStatus.setImageDrawable(getResources().getDrawable(R.drawable.tick));
        }
        checkStatusNow(view);
//        String ping = "ping -c 1 google.com";
//        byte[] pSend = ping.getBytes();
//        mChatService.write(pSend);





//        final Handler h = new Handler();
//        final int delay = 20000;
//        h.postDelayed(new Runnable(){
//            public void run(){
                String ping = "treehouses detectrpi";
                byte[] pSend1 = ping.getBytes();
                mChatService.write(pSend1);
////                h.postDelayed(this, delay);
//            }
//        }, delay);

//        final Handler h1 = new Handler();
//        final int delay1 = 20000;
//        h1.postDelayed(new Runnable(){
//            public void run(){
//                String ping1 = "treehouses internet";
//                byte[] pSend2 = ping1.getBytes();
//                mChatService.write(pSend2);
//                h1.postDelayed(this, delay1);
//            }
//        }, delay1);

//        Log.e("PROCESSING", ""+processing);
////        while(processing){System.out.print("-");}
////        Log.e("PROCESSING", ""+processing);
//        for(int i = 0; i < 1000; i++){System.out.print("-");}
//        Log.e("INCOMING MESSAGE", ""+processedMessage);


//        for(int i = 0; i < 1000; i++){System.out.print("-");}
//        String ping2 = "treehouses rebootneeded";
//        byte[] pSend3 = ping2.getBytes();
//        mChatService.write(pSend3);

//        initialActivity.sendMessage("treehouses detectrpi");


        return view;
    }

    private void checkStatusNow(View view){
//        if(mChatService.getState() == Constants.STATE_CONNECTED){
//            btStatus.setBackgroundResource((R.drawable.circle));
//            GradientDrawable bgShape = (GradientDrawable)btStatus.getBackground();
//            bgShape.setColor(Color.GREEN);
//            wifiStatus.setBackgroundResource((R.drawable.circle));
//            bgShape = (GradientDrawable)wifiStatus.getBackground();
//            bgShape.setColor(Color.GREEN);
//            btRPIName.setBackgroundResource((R.drawable.circle));
//            bgShape = (GradientDrawable)btRPIName.getBackground();
//            bgShape.setColor(Color.GREEN);
//            rpiType.setBackgroundResource((R.drawable.circle));
//            bgShape = (GradientDrawable)rpiType.getBackground();
//            bgShape.setColor(Color.GREEN);
//        }else{
//            btStatus.setBackgroundResource((R.drawable.circle));
//            GradientDrawable bgShape = (GradientDrawable)btStatus.getBackground();
//            bgShape.setColor(Color.RED);
//            wifiStatus.setBackgroundResource((R.drawable.circle));
//            bgShape = (GradientDrawable)wifiStatus.getBackground();
//            bgShape.setColor(Color.RED);
//            btRPIName.setBackgroundResource((R.drawable.circle));
//            bgShape = (GradientDrawable)btRPIName.getBackground();
//            bgShape.setColor(Color.RED);
//            rpiType.setBackgroundResource((R.drawable.circle));
//            bgShape = (GradientDrawable)rpiType.getBackground();
//            bgShape.setColor(Color.RED);
//        }
        Log.e("DEVICE",""+mConnectedDeviceName);
    }

    public void updateStatus(){
        int index = 0;
        tvStatus2.setText("Connected RPI Name: "+deviceName);
        btRPIName.setImageDrawable(getResources().getDrawable(R.drawable.tick));
        if(outs.size() == 1){
            tvStatus3.setText("RPI Type: "+outs.get(0));
            rpiType.setImageDrawable(getResources().getDrawable(R.drawable.tick));
            String ping1 = "treehouses internet";
            byte[] pSend2 = ping1.getBytes();
            mChatService.write(pSend2);
        }
        if(outs.size() == 2){
            tvStatus1.setText("RPI Wifi Connection: "+outs.get(1));
            if(outs.get(1).equals("true")){
                wifiStatusVal = true;
                wifiStatus.setImageDrawable(getResources().getDrawable(R.drawable.tick));
            }
            if(wifiStatusVal){
                String ping1 = "treehouses upgrade --check";
                byte[] pSend2 = ping1.getBytes();
                mChatService.write(pSend2);
            }else{
                tvUpgrade.setText("Upgrade Status: NO INTERNET");
            }
        }
        if(outs.size() == 3){
            if(outs.get(2).equals("false")){
                tvUpgrade.setText("Upgrade Status: Latest Version");
            }else{
                ivUpgrad.setImageDrawable(getResources().getDrawable(R.drawable.tick_png));
                tvUpgrade.setText("Upgrade Status: Required for Version: "+outs.get(2).substring(4));
            }

        }
//        for(String out : outs){
//
//        }
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
