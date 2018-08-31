package io.treehouses.remote.FragmentsOld;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import io.treehouses.remote.MiscOld.Constants;
import io.treehouses.remote.NetworkOld.BluetoothChatService;
import io.treehouses.remote.NetworkOld.DeviceListActivity;
import io.treehouses.remote.R;

/**
 * Created by Lalitha S Oruganty on 3/14/2018.
 */

public class TreehousesFragment extends Fragment implements View.OnClickListener{

    private static final String TAG = "TreehousesFragment" ;
    private FragmentActivity activity = getActivity();

    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothChatService mChatService = null;
    private StringBuffer mOutStringBuffer;
    private ArrayAdapter<String> outputArrayAdapter;
    private ListView consoleOutput;
    private Button detectRpiVersion;
    private EditText mOutEditText;
    private ProgressDialog mProgressDialog;
    static String currentStatus = "not connected";
    private static boolean isRead = false;
    private String mConnectedDeviceName = null;
    private static boolean isCountdown = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //activity.getActionBar().setDisplayShowHomeEnabled(true);
        //activity.getActionBar().setLogo(R.mipmap.ic_launcher);
        //activity.getActionBar().setDisplayUseLogoEnabled(true);
        //fragmentActivity = getActivity();

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            //FragmentActivity activity = getActivity();
            Toast.makeText(activity, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            activity.finish();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, Constants.REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else if (mChatService == null) {
            setupChat();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.treehouses_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        detectRpiVersion = (Button) view.findViewById(R.id.btn_detect_rpi_version);
        consoleOutput = (ListView) view.findViewById(R.id.lv_console_output);
        detectRpiVersion.setOnClickListener(this);
    }

    @Override
    public void onClick(View v){
        switch (v.getId()) {
            case R.id.btn_detect_rpi_version:
                sendMessage("treehouses detectrpi");
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constants.REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case Constants.REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case Constants.REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupChat();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(getActivity(), R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show();
                    //getApplication().finish();
                }
            case Constants.REQUEST_DIALOG_FRAGMENT:
                if(resultCode == Activity.RESULT_OK){

                    //check status
                    if(mChatService.getState() != Constants.STATE_CONNECTED){
                        Toast.makeText(getActivity(), R.string.not_connected,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    //show the progress bar, disable user interaction
                    mProgressDialog.show();
                    //TODO: start watchdog
                    isCountdown = true;
                    mHandler.postDelayed(watchDogTimeOut,30000);
                    Log.d(TAG, "watchDog start");

                    //get SSID & PWD from user input
                    String SSID = data.getStringExtra("SSID") == null? "":data.getStringExtra("SSID");
                    String PWD = data.getStringExtra("PWD") == null? "":data.getStringExtra("PWD");

                    Log.d(TAG, "back from dialog: ok, SSID = " + SSID + ", PWD = " + PWD);

                    //TODO: 1. check Valid input  2. get the SSID and password from data object and send it to RPi through sendMessage() method
//                    Toast.makeText(getActivity(), R.string.config_success,
//                            Toast.LENGTH_SHORT).show();

                    sendMessage(SSID,PWD);
                    //TODO:1. lock the app when configuring. 2. listen to configuration result and do the logic

                }else{
                    Log.d(TAG, "back from dialog, fail");
                }
        }
    }

    private void setupChat() {
        Log.d(TAG, "setupChat()");

        // Initialize the array adapter for the conversation thread
        outputArrayAdapter = new ArrayAdapter<String>(getActivity(),R.layout.message){
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView tView = (TextView) view.findViewById(R.id.listItem);
                if(isRead){
                    tView.setTextColor(Color.BLUE);
                }else{
                    tView.setTextColor(Color.RED);
                }
                return view;
            }
        };

        consoleOutput.setAdapter(outputArrayAdapter);

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(getActivity(), mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");

        //get spinner
        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setTitle(R.string.progress_dialog_title);
        mProgressDialog.setMessage(getString(R.string.progress_dialog_message));
        mProgressDialog.setCancelable(false); // disable dismiss by tapping outside of the dialog

    }

    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != Constants.STATE_CONNECTED) {
            Toast.makeText(getActivity(), R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
            mOutEditText.setText(mOutStringBuffer);
        }

    }

    private void sendMessage(String SSID, String PWD) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != Constants.STATE_CONNECTED) {
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

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
            mOutEditText.setText(mOutStringBuffer);
        }
    }

    private final CustomHandler mHandler = new CustomHandler(activity) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };

    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mChatService.connect(device, secure);
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

}
