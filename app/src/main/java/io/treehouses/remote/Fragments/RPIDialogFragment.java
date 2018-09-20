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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import io.treehouses.remote.R;
import me.aflak.bluetooth.Bluetooth;

public class RPIDialogFragment extends DialogFragment{


    TextBoxValidation textboxValidation = new TextBoxValidation();
    ListView listView;
    BluetoothAdapter mBluetoothAdapter;
    Bluetooth bluetooth;
    List<String> s = new ArrayList<String>();

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
//        mBluetoothAdapter.startDiscovery();

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothAdapter.startDiscovery();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        getContext().registerReceiver(mReceiver, filter);

        final AlertDialog mDialog = getAlertDialog(mView);
        listView = mView.findViewById(R.id.listView);

        initLayoutView(mView);

        return mDialog;
    }

    @Override
    public void onStart() {
        super.onStart();

//        mBluetoothAdapter.star
//        bluetooth.enable();
    }

    @Override
    public void onStop() {
        super.onStop();
//        bluetooth.onStop();

    }
    protected AlertDialog getAlertDialog(View mView) {
        return new AlertDialog.Builder(getActivity())
                .setView(mView)
                .setTitle(R.string.dialog_message)
                .setIcon(R.drawable.dialog_icon)
                .setPositiveButton(R.string.start_configuration,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
//                                listView = dialog.
//                                listView = dialog.findViewById(R.id.listView);
//                                mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//                                if(mBluetoothAdapter == null){
//                                    Log.i("Bluetooth Adapter", "Bluetooth not supported");
//                                }else{
//                                    IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
//                                    getContext().registerReceiver(mReceiver, filter);
//                                    Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
//
//                                    List<String> s = new ArrayList<String>();
//                                    for(BluetoothDevice bt : pairedDevices) {
//                                        s.add(bt.getName());
//                                        Log.e("BT", bt.getName() + "\n");
//                                    }
//                                    listView.setAdapter(new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, s));
//                                }
//                                Intent intent = new Intent();
//                                getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);

                            }
                        }
                )
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_CANCELED, getActivity().getIntent());
                    }
                })
                .create();
    }
    protected void initLayoutView(View mView) {
//        listView = .findViewById(R.id.listView);
        if(mBluetoothAdapter == null){
            Log.i("Bluetooth Adapter", "Bluetooth not supported");
        }else{

//            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
//
//            for(BluetoothDevice bt : pairedDevices) {
////                s.add(bt.getName());
//                Log.e("BT", bt.getName() + "\n");
//            }
//            listView.setAdapter(new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, s));

        }
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                s.add(deviceName+ "\n" + deviceHardwareAddress);
                Log.e("Broadcast BT", device.getName() + "\n" + device.getAddress());
            }
        }
    };

}
