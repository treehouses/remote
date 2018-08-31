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
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import io.treehouses.remote.NetworkOld.BluetoothChatFragment;
import io.treehouses.remote.NetworkOld.BluetoothChatService;
import io.treehouses.remote.NetworkOld.DeviceListActivity;
import io.treehouses.remote.NetworkOld.HotspotDialogFragment;
import io.treehouses.remote.NetworkOld.WifiDialogFragment;
import io.treehouses.remote.R;

/**
 * Created by Lalitha S Oruganty on 3/13/2018.
 */

public class Dashboard extends Fragment implements View.OnClickListener{

    private static final String TAG = "BluetoothChatFragment";
    private static final String BACK_STACK_ROOT_TAG = "root_fragment";

    // current connection status
    static String currentStatus = "not connected";

    private ListView mConversationView;
    private EditText mOutEditText;
    private Button mSendButton;
    private ProgressDialog mProgressDialog;
    private String mConnectedDeviceName = null;

    private static boolean isCountdown = false;
    private static boolean isRead = false;
    private ArrayAdapter<String> mConversationArrayAdapter;
    private StringBuffer mOutStringBuffer;
    private BluetoothAdapter mBluetoothAdapter = null;
    private Button piButton;
    private Button dockerButton;
    private ListView lview;
    private Button cmdButton;
    private BluetoothChatService mChatService = null;
    private FragmentActivity mFragmentActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mFragmentActivity = getActivity();
        return inflater.inflate(R.layout.hope_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        piButton = (Button) view.findViewById(R.id.btn_treehouses_commands);
        dockerButton = (Button) view.findViewById(R.id.btn_docker_commands);
        cmdButton = (Button)view.findViewById(R.id.btn_cmd_commands);

        piButton.setOnClickListener(this);
        dockerButton.setOnClickListener(this);
        cmdButton.setOnClickListener(this);

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, Constants.REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else if (mChatService == null) {
            setupChat();
        }
    }

    @Override
    public void onClick(View v){
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        switch (v.getId()) {
            case R.id.btn_treehouses_commands:
                fragmentManager.popBackStack(BACK_STACK_ROOT_TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                TreehousesFragment treehousesFragment = new TreehousesFragment();
                fragmentTransaction.replace(R.id.sample_layout, treehousesFragment);
                fragmentTransaction.addToBackStack(BACK_STACK_ROOT_TAG);
                fragmentTransaction.commit();
                break;
            case R.id.btn_docker_commands:
                fragmentManager.popBackStack(BACK_STACK_ROOT_TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                DockerFragment dockerFragment = new DockerFragment();
                fragmentTransaction.replace(R.id.sample_layout, dockerFragment);
                fragmentTransaction.addToBackStack(BACK_STACK_ROOT_TAG);
                fragmentTransaction.commit();
                break;
            case R.id.btn_cmd_commands:
                fragmentManager.popBackStack(BACK_STACK_ROOT_TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                BluetoothChatFragment chatFragment = new BluetoothChatFragment();
                fragmentTransaction.replace(R.id.sample_layout, chatFragment);
                fragmentTransaction.addToBackStack(BACK_STACK_ROOT_TAG);
                fragmentTransaction.commit();
                break;
        }
    }

    // This is unused right? Can we remove this to make this file shorter???
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
       // inflater.inflate(R.menu.bluetooth_chat, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.wifi_configuration: {
                showWifiDialog();
                //return true;
                break;
            }
            case R.id.insecure_connect_scan: {
                // Launch the DeviceListActivity to see devices and do scan
                Intent serverIntent = new Intent(getActivity(), DeviceListActivity.class);
                startActivityForResult(serverIntent, Constants.REQUEST_CONNECT_DEVICE_INSECURE);
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
            if (mChatService.getState() == Constants.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }
    }

    public void showWifiDialog() {
        // Create an instance of the dialog fragment and show it
        DialogFragment dialogFrag = WifiDialogFragment.newInstance(123);
        dialogFrag.setTargetFragment(this, Constants.REQUEST_DIALOG_FRAGMENT);
        dialogFrag.show(getFragmentManager().beginTransaction(), "wifiDialog");
    }

    public void showHotspotDialog(){
        // Create an instance of the dialog fragment and show it
        DialogFragment dialogFrag = HotspotDialogFragment.newInstance(123);
        dialogFrag.setTargetFragment(this, Constants.REQUEST_DIALOG_FRAGMENT_HOTSPOT);
        dialogFrag.show(getFragmentManager().beginTransaction(),"hotspotDialog");
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
                    Log.d("dashboard", "BT not enabled");
                    Toast.makeText(getActivity(), R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }
            case Constants.REQUEST_DIALOG_FRAGMENT:
                if (resultCode == Activity.RESULT_OK) {

                    //check status
                    if (mChatService.getState() != Constants.STATE_CONNECTED) {
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
                    String SSID = data.getStringExtra("SSID") == null ? "" : data.getStringExtra("SSID");
                    String PWD = data.getStringExtra("PWD") == null ? "" : data.getStringExtra("PWD");

                    Log.d(TAG, "back from dialog: ok, SSID = " + SSID + ", PWD = " + PWD);

                    //TODO: 1. check Valid input  2. get the SSID and password from data object and send it to RPi through sendMessage() method
//                    Toast.makeText(getActivity(), R.string.config_success,
//                            Toast.LENGTH_SHORT).show();

                    sendMessage(SSID, PWD);
                    //TODO:1. lock the app when configuring. 2. listen to configuration result and do the logic

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

    private void setupChat() {
        Log.d(TAG, "setupChat()");

        // Initialize the array adapter for the conversation thread
        mConversationArrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.message) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView consoleView = (TextView) view.findViewById(R.id.listItem);
                if (isRead) {
                    consoleView.setTextColor(Color.BLUE);
                } else {
                    consoleView.setTextColor(Color.RED);
                }
                return view;
            }
        };
        mChatService = new BluetoothChatService(getActivity(), mHandler);
    }

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

    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    private final CustomHandler mHandler = new CustomHandler(mFragmentActivity){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };

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
