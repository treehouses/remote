package io.treehouses.remote;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Lalitha S Oruganty on 3/13/2018.
 */

public class Dashboard extends Fragment {

    private static final String TAG = "BluetoothChatFragment";

    //current connection status
    static String currentStatus = "not connected";
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;
    public static final int REQUEST_DIALOG_FRAGMENT = 4;

    private ProgressDialog mProgressDialog;
    private ProgressDialog connectProgressDialog;
    private String mConnectedDeviceName = null;

    private static boolean isCountdown = false;
    private static boolean isRead = false;
    private BluetoothAdapter mBluetoothAdapter = null;
    private Button piButton;
    private Button dockerButton;
    private Button cmdButton;
    private BluetoothChatService mChatService = null;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return inflater.inflate(R.layout.hope_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setTitle(R.string.progress_dialog_title);
        mProgressDialog.setMessage(getString(R.string.progress_dialog_message));
        mProgressDialog.setCancelable(false); // disable dismiss by tapping outside of the dialog

        connectProgressDialog = new ProgressDialog(getActivity());
        connectProgressDialog.setTitle(R.string.connect_progress_dialog__title);
        connectProgressDialog.setMessage(getString(R.string.progress_dialog_message));
        connectProgressDialog.setCancelable(false); // disable dismiss by tapping outside of the dialog

        piButton = (Button) view.findViewById(R.id.pbutton);
        dockerButton = (Button) view.findViewById(R.id.docker_button);
        cmdButton = (Button)view.findViewById(R.id.cmdbutton);

        piButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), PirateshipActivity.class);
                //Bundle mBundle = new Bundle();
                intent.putExtra("mChatService", mChatService);
                //intent.putExtra("mBundle", mBundle);
                startActivity(intent);
            }
        });

        dockerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent dintent = new Intent(view.getContext(),DockerActivity.class);
                startActivity(dintent);
            }
        });

        cmdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getFragmentManager();
                android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                BluetoothChatFragment chatfrag = new BluetoothChatFragment();
                Bundle mBundle = new Bundle();
                mBundle.putSerializable("mChatService", mChatService);
                chatfrag.setArguments(mBundle);
                fragmentTransaction.replace(R.id.sample_layout,chatfrag);
                fragmentTransaction.commit();
            }
        });

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else if (mChatService == null) {
            mChatService = new BluetoothChatService(getActivity(), mHandler);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.wifi_configuration: {
                showNWifiDialog();
                //return true;
                break;
            }
            case R.id.insecure_connect_scan: {
                // Launch the DeviceListActivity to see devices and do scan
                Intent serverIntent = new Intent(getActivity(), DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
                //return true;
                break;
            }
            case R.id.discoverable: {
                // Ensure this device is discoverable by others
                ensureDiscoverable();
                //return true;
                break;
            }
            case R.id.hotspot_configuration: {
                showHotspotDialog();
                //return true;
                break;
            }
            default:
                return false;
        }
        return true;
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        if(isCountdown){
            mHandler.removeCallbacks(watchDogTimeOut);
        }
        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }
    }

    public void showNWifiDialog() {
        // Create an instance of the dialog fragment and show it
        DialogFragment dialogFrag = WifiDialogFragment.newInstance(123);
        dialogFrag.setTargetFragment(this, REQUEST_DIALOG_FRAGMENT);
        dialogFrag.show(getFragmentManager().beginTransaction(), "dialog");
    }

    public void showHotspotDialog(){
        //Reusing WifiDialogFragment code for Hotspot
        DialogFragment hDialogFragment = HotspotDialogFragment.newInstance(123);
        hDialogFragment.setTargetFragment(this, Constants.REQUEST_DIALOG_FRAGMENT_HOTSPOT);
        hDialogFragment.show(getFragmentManager().beginTransaction(),"hDialog");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                connectProgressDialog.show();
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    mChatService = new BluetoothChatService(getActivity(), mHandler);
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d("dashboard", "BT not enabled");
                    Toast.makeText(getActivity(), R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }
            case REQUEST_DIALOG_FRAGMENT:
                if (resultCode == Activity.RESULT_OK) {

                    //check status
                    if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
                        Toast.makeText(getActivity(), R.string.not_connected,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    //show the progress bar, disable user interaction
                       mProgressDialog.show();
                    //TODO: start watchdog
                      isCountdown = true;
                     // mHandler.postDelayed(watchDogTimeOut,30000);
                    Log.d(TAG, "watchDog start");

                    //get SSID & PWD from user input
                    String SSID = data.getStringExtra
                            ("SSID") == null ? "" : data.getStringExtra("SSID");
                    String PWD = data.getStringExtra
                            ("PWD") == null ? "" : data.getStringExtra("PWD");

                    Log.d(TAG, "back from dialog: ok, SSID = " + SSID + ", PWD = " + PWD);

                    /**
                     * TODO:
                     * 1. check Validinput
                     * 2. get the SSID and password from data object and send it to RPi through
                     * sendMessage() method
                     */
                    sendMessage(SSID, PWD);
                    /**
                     * TODO:
                     * 1. lock the app when configuring.
                     * 2. listen to configuration result and do the logic.
                     */
                } else {
                    Log.d(TAG, "back from dialog, fail");
                }
        }
    }

    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mChatService.connect(device, secure);
    }

    private void sendMessage(String SSID, String PWD) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(getActivity(), R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }
        // Check that there's actually something to send
        if (SSID.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            JSONObject mJson = new JSONObject();
            try {
                mJson.put("SSID",SSID);
                mJson.put("PWD",PWD);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            byte[] send = mJson.toString().getBytes();
            mChatService.write(send);
        }
    }
    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            FragmentActivity activity = getActivity();
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
                    break;
                case Constants.MESSAGE_READ:
                    isRead = true;
                    String readMessage = (String)msg.obj;
                    Log.d(TAG, "readMessage = " + readMessage);
                    //TODO: if message is json -> callback from RPi
                    if(isJson(readMessage)){
                        handleCallback(readMessage);
                    }else{
                        if(isCountdown){
                            mHandler.removeCallbacks(watchDogTimeOut);
                            isCountdown = false;
                        }
                        if(mProgressDialog.isShowing()){
                            mProgressDialog.dismiss();
                            Toast.makeText(activity, R.string.config_alreadyConfig, Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != activity) {
                        Toast.makeText(activity, "Connected to "
                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != activity) {
                        Toast.makeText(activity, msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.MESSAGE_DISPLAY_DONE:
                    connectProgressDialog.dismiss();
                    break;
            }
        }
    };

    private void setStatus(int resId) {
        FragmentActivity activity = getActivity();
        if (null == activity) {
            return;
        }
        final ActionBar actionBar = activity.getActionBar();
        if (null == actionBar) {
            return;
        }
        Log.d(TAG, "actionBar.setSubtitle(resId) = " + resId );
        currentStatus = getString(resId);
        actionBar.setSubtitle(resId);

    }

    /**
     * Updates the status on the action bar.
     *
     * @param subTitle status
     */
    private void setStatus(CharSequence subTitle) {
        FragmentActivity activity = getActivity();
        if (null == activity) {
            return;
        }
        final ActionBar actionBar = activity.getActionBar();
        if (null == actionBar) {
            return;
        }
        Log.d(TAG, "actionBar.setSubtitle(subTitle) = " + subTitle );
        currentStatus = subTitle.toString();
        actionBar.setSubtitle(subTitle);
    }

    private final Runnable watchDogTimeOut = new Runnable() {
        @Override
        public void run() {
            isCountdown = false;
            //time out
            if(mProgressDialog.isShowing()){
                mProgressDialog.dismiss();
                Toast.makeText(getActivity(),"No response from RPi",Toast.LENGTH_LONG).show();
            }
        }
    };

    public boolean isJson(String str) {
        try {
            new JSONObject(str);
        } catch (JSONException ex) {
            return false;
        }
        return true;
    }

    public void handleCallback(String str){
        String result;
        String ip;
        if(isCountdown){
            mHandler.removeCallbacks(watchDogTimeOut);
            isCountdown = false;
        }
        //enable user interaction
        mProgressDialog.dismiss();
        try{
            JSONObject mJSON = new JSONObject(str);
            result = mJSON.getString("result") == null? "" : mJSON.getString("result");
            ip = mJSON.getString("IP") == null? "" : mJSON.getString("IP");
            //Toast.makeText(getActivity(), "result: "+result+", IP: "+ip, Toast.LENGTH_LONG).show();

            if(!result.equals("SUCCESS")){
                Toast.makeText(getActivity(), R.string.config_fail,
                        Toast.LENGTH_LONG).show();
            }else{
//                Toast.makeText(getActivity(), R.string.config_success,
//                            Toast.LENGTH_SHORT).show();
                Toast.makeText(getActivity(),getString(R.string.config_success) + ip,Toast.LENGTH_LONG).show();
            }

        }catch (JSONException e){
            // error handling
            Toast.makeText(getActivity(), "SOMETHING WENT WRONG", Toast.LENGTH_LONG).show();
        }
    }
}
