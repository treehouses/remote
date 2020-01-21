package io.treehouses.remote.Fragments.DialogFragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.treehouses.remote.Constants;
import io.treehouses.remote.Network.BluetoothChatService;
import io.treehouses.remote.R;
import io.treehouses.remote.adapter.RPIListAdapter;
import io.treehouses.remote.bases.BaseDialogFragment;
import io.treehouses.remote.callback.SetDisconnect;
import io.treehouses.remote.pojo.DeviceInfo;

import static android.widget.Toast.LENGTH_LONG;

public class RPIDialogFragment extends BaseDialogFragment {

    private static BluetoothChatService mChatService = null;

    private static RPIDialogFragment instance = null;
    private List<BluetoothDevice> raspberry_devices = new ArrayList<BluetoothDevice>(), all_devices = new ArrayList<BluetoothDevice>();
    private Set<BluetoothDevice> pairedDevices;
    private static BluetoothDevice mainDevice = null;
    private ListView listView;
    private ArrayAdapter mArrayAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private SetDisconnect checkConnectionState;
    private Context context;
    private Switch mDiscoverRaspberry;
    private AlertDialog mDialog;
    private List<DeviceInfo> raspberryDevicesText = new ArrayList<DeviceInfo>(), allDevicesText = new ArrayList<DeviceInfo>();
    private ProgressDialog pDialog;
    private ProgressBar progressBar;

    public static androidx.fragment.app.DialogFragment newInstance(int num) {
        RPIDialogFragment rpiDialogFragment = new RPIDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("num", num);
        rpiDialogFragment.setArguments(bundle);
        return rpiDialogFragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        instance = this;
        context = getContext();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothCheck();
        if (mBluetoothAdapter.isDiscovering()) { mBluetoothAdapter.cancelDiscovery(); }
        mBluetoothAdapter.startDiscovery();
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View mView = inflater.inflate(R.layout.activity_rpi_dialog_fragment, null);
        initDialog(mView);

        if (mChatService == null) { mChatService = new BluetoothChatService(mHandler); }

        pairedDevices = mBluetoothAdapter.getBondedDevices();
        setAdapterNotNull(raspberryDevicesText);
        for (BluetoothDevice d : pairedDevices) {
            if (checkPiAddress(d.getAddress())) {
                addToDialog(d, raspberryDevicesText, raspberry_devices, false);
                progressBar.setVisibility(View.INVISIBLE); }
        }
        intentFilter();

        return mDialog;
    }

    private void initDialog(View mView) {
        pDialog = new ProgressDialog(getActivity(), ProgressDialog.THEME_DEVICE_DEFAULT_LIGHT);
        listView = mView.findViewById(R.id.listView);
        mDialog = getAlertDialog(mView, context, false);
        mDialog.setTitle(R.string.select_device);
        listViewOnClickListener(mView);
        Button mCloseButton = mView.findViewById(R.id.rpi_close_button);
        mCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetoothCheck("unregister");
                dismiss();
            }
        });
        mDiscoverRaspberry = mView.findViewById(R.id.rpi_switch);
        mDiscoverRaspberry.setChecked(true);
        switchViewOnClickListener();
        progressBar = mView.findViewById(R.id.progressBar);
    }

    private void intentFilter() {
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        getActivity().registerReceiver(mReceiver, filter);
    }

    private void listViewOnClickListener(View mView) {
        listView.setOnItemClickListener((parent, view, position, id) -> {
            mChatService = new BluetoothChatService(mHandler);
            List<BluetoothDevice> deviceList;
            if (mDiscoverRaspberry.isChecked()) deviceList = raspberry_devices;
            else deviceList = all_devices;
            if (checkPiAddress(deviceList.get(position).getAddress())) {
                mainDevice = deviceList.get(position);
                mChatService.connect(deviceList.get(position),true);
                int status = mChatService.getState();
                mDialog.cancel();
                finish(status, mView);
                Log.e("Connecting Bluetooth", "Position: " + position + " ;; Status: " + status);
                pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                pDialog.setTitle("Connecting...");
                pDialog.setMessage("Device Name: " + mainDevice.getName() + "\nDevice Address: " + mainDevice.getAddress());
                pDialog.show();
            }
            else { Toast.makeText(getContext(), "Device Unsupported",LENGTH_LONG).show(); }
        });
    }

    private void switchViewOnClickListener() {
        mDiscoverRaspberry.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    setAdapterNotNull(raspberryDevicesText);
                    buttonView.setText(R.string.paired_devices);
                    if (raspberry_devices.isEmpty()) progressBar.setVisibility(View.VISIBLE);
                }
                else {
                    setAdapterNotNull(allDevicesText);
                    buttonView.setText(R.string.all_devices);
                    progressBar.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    public static RPIDialogFragment getInstance() { return instance; }

    public void setCheckConnectionState(SetDisconnect checkConnectionState)  { this.checkConnectionState = checkConnectionState; }

    private void finish(int status, View mView) {
        final AlertDialog mDialog = getAlertDialog(mView, context, false);
        if (status == 3) mDialog.setTitle("BLUETOOTH IS CONNECTED");
        else if (status == 2) mDialog.setTitle("BLUETOOTH IS CONNECTING...");
        else mDialog.setTitle("BLUETOOTH IS NOT CONNECTED");
        setAdapterNotNull(new ArrayList<>());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try { if (mBluetoothAdapter == null) context.unregisterReceiver(mReceiver); } catch (Exception e) { e.printStackTrace(); }
    }

    public AlertDialog getAlertDialog(View mView, Context context, Boolean wifi) {
        return new AlertDialog.Builder(context).setView(mView).setIcon(R.drawable.dialog_icon).create();
    }

    public void bluetoothCheck(String... args) {
        if (mBluetoothAdapter == null) {
            Toast.makeText(getActivity(), "Your Bluetooth Is Not Enabled or Not Supported", LENGTH_LONG).show();
            getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_CANCELED, getActivity().getIntent());
            context.unregisterReceiver(mReceiver);
        }
        if (args.length >= 1) {
            if (args[0].equals("unregister")) {
                context.unregisterReceiver(mReceiver);
                Intent intent = new Intent();
                intent.putExtra("mChatService", mChatService);
                getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
            }
        }
    }

    private void setAdapterNotNull(List<DeviceInfo> listVal) {
        mArrayAdapter = new RPIListAdapter(getContext(), listVal);
        listView.setAdapter(mArrayAdapter);
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (checkPiAddress(device.getAddress())) {
                    addToDialog(device, raspberryDevicesText, raspberry_devices, true);
                    progressBar.setVisibility(View.INVISIBLE);
                }
                addToDialog(device, allDevicesText, all_devices, true);
                Log.e("Broadcast BT", device.getName() + "\n" + device.getAddress());
            }
        }
    };

    private void addToDialog(BluetoothDevice device, List<DeviceInfo> textList, List<BluetoothDevice> mDevices, Boolean inRange) {
        if (!mDevices.contains(device)){
            mDevices.add(device);
            textList.add(new DeviceInfo(device.getName() + "\n" + device.getAddress(), pairedDevices.contains(device), inRange));
        }
        else {
            textList.get(mDevices.indexOf(device)).setInRange(true);
        }
        mArrayAdapter.notifyDataSetChanged();
    }

    private boolean checkPiAddress(String deviceHardwareAddress) {
        Set<String> piAddress = new HashSet<String>(Arrays.asList("B8:27:EB", "DC:A6:32", "B8-27-EB", "DC-A6-32", "B827.EB", "DCA6.32"));
        return piAddress.contains(deviceHardwareAddress.substring(0, 7)) || piAddress.contains(deviceHardwareAddress.substring(0, 8));
    }

    @SuppressLint("HandlerLeak")
    public final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.e("RPIDialogFragment", "" + msg.what);
            String readMessage = (String) msg.obj;

            if (!TextUtils.isEmpty(readMessage) && readMessage.equals("connectionCheck")) {
                pDialog.dismiss();
            }

            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case Constants.STATE_CONNECTED:
                            Log.e("RPIDialogFragment", "Bluetooth Connection Status Change: State Listen");
                            pDialog.dismiss();
                            listener.setChatService(mChatService);
                            checkConnectionState.checkConnectionState();
                            mBluetoothAdapter.cancelDiscovery();
                            Toast.makeText(context, "Bluetooth Connected", LENGTH_LONG).show();
                            break;
                        case Constants.STATE_NONE:
                            Log.e("RPIDialogFragment", "Bluetooth Connection Status Change: State None");
                            break;
                    }
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    Log.e("RPIDialogFragment", "Device Name " + msg.getData().getString(Constants.DEVICE_NAME));
                    break;
            }
        }
    };

    public Handler getmHandler() { return mHandler; }
}