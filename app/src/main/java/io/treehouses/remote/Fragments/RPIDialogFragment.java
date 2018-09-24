package io.treehouses.remote.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import io.treehouses.remote.FragmentsOld.CustomHandler;
import io.treehouses.remote.MiscOld.Constants;
import io.treehouses.remote.Network.BluetoothChatService;
import io.treehouses.remote.Network.DeviceListActivity;
import io.treehouses.remote.R;
import me.aflak.bluetooth.Bluetooth;

public class RPIDialogFragment extends DialogFragment{

    private BluetoothChatService mChatService = null;

    private static final String TAG = "RaspberryDialogFragment";
    private static boolean isRead = false;


    private ArrayAdapter<String> mConversationArrayAdapter;
    ListView listView;
    BluetoothAdapter mBluetoothAdapter;
    Bluetooth bluetooth;
    List<String> s = new ArrayList<String>();
    List<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();

    public static RPIDialogFragment newInstance(int num) {
        RPIDialogFragment rpiDialogFragment = new RPIDialogFragment();
//        Bundle bundle = new Bundle();
//        bundle.putInt("num", num);
//        dialogFragment.setArguments(bundle);

        return rpiDialogFragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Build the dialog and set up the button click handlers
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View mView = inflater.inflate(R.layout.activity_rpi_dialog_fragment,null);

        listView = mView.findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mChatService.connect(devices.get(position), true);
            }
        });
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothCheck();
        mBluetoothAdapter.startDiscovery();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        getActivity().registerReceiver(mReceiver, filter);

        if(mChatService == null){
            setupBluetoothService();
        }
        final AlertDialog mDialog = getAlertDialog(mView);





        return mDialog;
    }

    private void connectDevice(Intent data) {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mChatService.connect(device, true);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }
    protected AlertDialog getAlertDialog(View mView) {
        return new AlertDialog.Builder(getActivity())
                .setView(mView)
                .setTitle(R.string.dialog_message)
                .setIcon(R.drawable.dialog_icon)
//                .setPositiveButton(R.string.start_configuration, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        int selected = listView.getSelectedItemPosition();
//                        String item = s.get(selected);
//                    }
//                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_CANCELED, getActivity().getIntent());
                        getActivity().unregisterReceiver(mReceiver);
                    }
                })
                .create();
    }
    protected void bluetoothCheck() {
        if(mBluetoothAdapter == null){
            Log.i("Bluetooth Adapter", "Bluetooth not supported");
            Toast.makeText(getContext(), "Your Bluetooth Is Not Enabled or Not Supported", Toast.LENGTH_LONG).show();
            getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_CANCELED, getActivity().getIntent());
            getActivity().unregisterReceiver(mReceiver);
        }
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    public final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.e("BLUETOOTH", "HELLO");
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                if(deviceName!=null){
//                    deviceNames.add(deviceName);
//                    deviceId.add(deviceHardwareAddress);
                    devices.add(device);
                    s.add(deviceName+ "\n" + deviceHardwareAddress);
                    listView.setAdapter(new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, s));
                }
                Log.e("Broadcast BT", device.getName() + "\n" + device.getAddress());
            }
        }
    };
    private void setupBluetoothService() {
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
    private final CustomHandler mHandler = new CustomHandler(getActivity()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };
}
