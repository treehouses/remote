package io.treehouses.remote.Fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import androidx.fragment.app.FragmentActivity;
import io.treehouses.remote.Constants;
import io.treehouses.remote.Network.BluetoothChatService;
import io.treehouses.remote.R;
import io.treehouses.remote.bases.BaseDialogFragment;
import io.treehouses.remote.callback.SetDisconnect;

public class RPIDialogFragment extends BaseDialogFragment {

    private static BluetoothChatService mChatService = null;
    private static final String TAG = "RaspberryDialogFragment";
    private static RPIDialogFragment instance = null;
    private List<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();
    private static BluetoothDevice mainDevice = null;
    private ListView listView;
    private BluetoothAdapter mBluetoothAdapter;
    private SetDisconnect checkConnectionState;
    private Context context;

    AlertDialog mDialog;
    List<String> s = new ArrayList<String>();
    ProgressDialog dialog;

    public static androidx.fragment.app.DialogFragment newInstance(int num) {
        RPIDialogFragment rpiDialogFragment = new RPIDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("num", num);
        rpiDialogFragment.setArguments(bundle);
        return rpiDialogFragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Build the dialog and set up the button click handlers
        instance = this;
        context = getContext();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothCheck();
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        mBluetoothAdapter.startDiscovery();
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View mView = inflater.inflate(R.layout.activity_rpi_dialog_fragment, null);
        dialog = new ProgressDialog(getActivity(), ProgressDialog.THEME_HOLO_DARK);
        listView = mView.findViewById(R.id.listView);
        mDialog = getAlertDialog(mView, context, false);
        mDialog.setTitle(R.string.select_device);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mChatService = new BluetoothChatService(mHandler);
                mainDevice = devices.get(position);
                mChatService.connect(devices.get(position), true);
                int status = mChatService.getState();
                mDialog.cancel();
                finish(status, mView);
                Log.e("Connecting Bluetooth", "Position: " + position + " ;; Status: " + status);
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.setTitle("Connecting...");
                dialog.setMessage("Device Name: " + mainDevice.getName() + "\nDevice Address: " + mainDevice.getAddress());
                dialog.show();
            }
        });

        if (mChatService == null) {
            setupBluetoothService();
        }

        Set<BluetoothDevice> pairedDevice = mBluetoothAdapter.getBondedDevices();
        if (pairedDevice.size() > 0) {
            for (BluetoothDevice device : pairedDevice) {
                devices.add(device);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                s.add(deviceName + "\n" + deviceHardwareAddress);
                setAdapterNotNull(s);
            }
        }
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        getActivity().registerReceiver(mReceiver, filter);

        return mDialog;
    }

    public static RPIDialogFragment getInstance() {
        return instance;
    }

    public void setCheckConnectionState(SetDisconnect checkConnectionState)  {
        this.checkConnectionState = checkConnectionState;
    }

    private void finish(int status, View mView) {
        final AlertDialog mDialog = getAlertDialog(mView, context, false);
        if (status == 3) {
            mDialog.setTitle("BLUETOOTH IS CONNECTED");
        } else if (status == 2) {
            mDialog.setTitle("BLUETOOTH IS CONNECTING...");
        } else {
            mDialog.setTitle("BLUETOOTH IS NOT CONNECTED");
        }
        List<String> empty = new ArrayList<>();
        setAdapterNotNull(empty);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (mBluetoothAdapter == null) {
                context.unregisterReceiver(mReceiver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public AlertDialog getAlertDialog(View mView, Context context, Boolean wifi) {
        return new AlertDialog.Builder(context)
                .setView(mView)
                .setIcon(R.drawable.dialog_icon)
                .setNegativeButton(R.string.material_drawer_close, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (!wifi) {
                            bluetoothCheck("unregister");
                        }
                    }
                })
                .create();
    }

    public void bluetoothCheck(String... args) {
        if (mBluetoothAdapter == null) {
            Log.i("Bluetooth Adapter", "Bluetooth not supported");
            Toast.makeText(getActivity(), "Your Bluetooth Is Not Enabled or Not Supported", Toast.LENGTH_LONG).show();
            getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_CANCELED, getActivity().getIntent());
            context.unregisterReceiver(mReceiver);
        }
        if (args.length >= 1) {
            if (args[0].equals("unregister")) {
                context.unregisterReceiver(mReceiver);
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                intent.putExtra("mChatService", mChatService);
                getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
            }
        }
    }

    private void setAdapterNotNull(List<String> listVal) {
        if (getActivity() == null) {
            Log.e("RPI DIALOG ACTIVITY", "null");
        } else {
            listView.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, listVal));
        }
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                boolean alreadyExist = false;
                for (BluetoothDevice checkDevices : devices) {
                    if (checkDevices.equals(device)) {
                        alreadyExist = true;
                    }
                }
                if (!alreadyExist) {
                    devices.add(device);
                    s.add(deviceName + "\n" + deviceHardwareAddress);
                    setAdapterNotNull(s);
                }
                Log.e("Broadcast BT", device.getName() + "\n" + device.getAddress());
            }
        }
    };

    private void setupBluetoothService() {
        Log.d(TAG, "setupChat()");
        mChatService = new BluetoothChatService(mHandler);
    }

    @SuppressLint("HandlerLeak")
    public final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.e("RPIDialogFragment", "" + msg.what);
            FragmentActivity activity = getActivity();

            String readMessage = (String)msg.obj;

            if (!TextUtils.isEmpty(readMessage) && readMessage.equals("connectionCheck")) {
                dialog.dismiss();
            }

            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case Constants.STATE_CONNECTED:
                            Log.e("RPIDialogFragment", "Bluetooth Connection Status Change: State Listen");
                            dialog.dismiss();
                            listener.setChatService(mChatService);
                            checkConnectionState.checkConnectionState();
                            Toast.makeText(context, "Bluetooth Connected", Toast.LENGTH_LONG).show();
                            break;
                        case Constants.STATE_NONE:
                            Log.e("RPIDialogFragment", "Bluetooth Connection Status Change: State None");
                            break;
                    }
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    Log.e("RPIDialogFragment", "Device Name " + msg.getData().getString(Constants.DEVICE_NAME));
                    break;
            }
        }
    };

    public Handler getmHandler() {

        return mHandler;
    }

}
