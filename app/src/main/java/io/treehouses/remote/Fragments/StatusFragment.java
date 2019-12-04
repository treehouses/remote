package io.treehouses.remote.Fragments;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentActivity;
import io.treehouses.remote.Constants;
import io.treehouses.remote.Network.BluetoothChatService;
import io.treehouses.remote.R;
import io.treehouses.remote.bases.BaseFragment;
import io.treehouses.remote.utils.SaveUtils;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class StatusFragment extends BaseFragment {
    public StatusFragment() {}

    View view;

    private static final String TAG = "StatusFragment";
    private ImageView wifiStatus, btRPIName, rpiType, memoryStatus;
    private ImageView btStatus, ivUpgrade;
    private TextView tvStatus, tvStatus1, tvStatus2, tvStatus3, tvUpgrade, tvMemory;
    private List<String> outs = new ArrayList<>();
    private Boolean wifiStatusVal = false;
    private Button upgrade;
    private ProgressDialog pd;
    private Boolean updateRightNow = false;
    private BluetoothChatService mChatService = null;
    private CardView cardRPIName;
    /**
     * Name of the connected device
     */
    private String mConnectedDeviceName = null;
    private String deviceName = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_status_fragment, container, false);
        initializeUIElements(view);

        mChatService = listener.getChatService();
        mChatService.updateHandler(mHandler);
        deviceName = mChatService.getConnectedDeviceName();

        tvStatus.setText("Bluetooth Connection: " + deviceName);

        Log.e("STATUS", "device name: " + deviceName);
        if (mChatService.getState() == Constants.STATE_CONNECTED) {
            btStatus.setImageDrawable(getResources().getDrawable(R.drawable.tick));
        }
        checkStatusNow();

        String ping = "treehouses detectrpi";
        byte[] pSend1 = ping.getBytes();
        mChatService.write(pSend1);
        return view;
    }

    private void initializeUIElements(View view) {
        btStatus = view.findViewById(R.id.btStatus);
        wifiStatus = view.findViewById(R.id.wifiStatus);
        btRPIName = view.findViewById(R.id.rpiName);
        rpiType = view.findViewById(R.id.rpiType);
        memoryStatus = view.findViewById(R.id.memoryStatus);

        ivUpgrade = view.findViewById(R.id.upgradeCheck);
        tvStatus = view.findViewById(R.id.tvStatus);
        tvStatus1 = view.findViewById(R.id.tvStatus1);
        tvStatus2 = view.findViewById(R.id.tvStatus2);
        tvStatus3 = view.findViewById(R.id.tvStatus3);
        tvUpgrade = view.findViewById(R.id.tvUpgradeCheck);
        tvMemory = view.findViewById(R.id.tvMemoryStatus);
        upgrade = view.findViewById(R.id.upgrade);
        upgrade.setVisibility(View.GONE);
        cardRPIName = view.findViewById(R.id.cardView);

        upgradeOnViewClickListener();
        rpiNameOnViewClickListener();
    }

    private void upgradeOnViewClickListener() {
        upgrade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writeToRPI("treehouses upgrade");
                updateRightNow = true;
                pd = ProgressDialog.show(getActivity(), "Updating...", "Please wait a few seconds...");
                pd.setCanceledOnTouchOutside(true);
            }
        });
    }
    private void rpiNameOnViewClickListener() {
        cardRPIName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRenameDialog();
            }
        });
    }

    public void checkStatusNow() {
        Log.e("DEVICE", "" + mConnectedDeviceName);
    }

    private void updateStatus() {
        setRPIDeviceName();
        if (outs.size() == 1) {
            setRPIType();
        }
        if (outs.size() == 2) {
            getMemory();
        }
        if (outs.size() == 3) {
            checkWifiStatus();
        }
        if (outs.size() == 4) {
            checkUpgradeStatus();
        }
        if (outs.size() == 5) {
            outs.remove(3);
            outs.remove(3);
            checkWifiStatus();
        }
    }

    private void writeToRPI(String ping) {
        byte[] pSend = ping.getBytes();
        mChatService.write(pSend);
    }

    private void setRPIDeviceName() {
        String name = deviceName.substring(0, deviceName.indexOf("-"));
        tvStatus2.setText("Connected RPI Name: " + name);
        btRPIName.setImageDrawable(getResources().getDrawable(R.drawable.tick));
    }

    private void setRPIType() {
        tvStatus3.setText("RPI Type: " + outs.get(0));
        rpiType.setImageDrawable(getResources().getDrawable(R.drawable.tick));
        writeToRPI("treehouses memory free");
    }

    private void getMemory() {
        tvMemory.setText("Memory: " + outs.get(1) + "bytes available");
        memoryStatus.setImageDrawable(getResources().getDrawable(R.drawable.tick));
        writeToRPI("treehouses internet");
    }

    private void checkWifiStatus() {
        tvStatus1.setText("RPI Wifi Connection: " + outs.get(2));
        Log.e("StatusFragment", "**" + outs.get(2) + "**" + outs.get(2).equals("true "));
        if (outs.get(2).equals("true ")) {
            Log.e("StatusFragment", "TRUE");
            wifiStatusVal = true;
            wifiStatus.setImageDrawable(getResources().getDrawable(R.drawable.tick));
        }
        if (wifiStatusVal) {
            writeToRPI("treehouses upgrade --check");
        } else {
            tvUpgrade.setText("Upgrade Status: NO INTERNET");
            upgrade.setVisibility(View.GONE);
        }
    }

    private void checkUpgradeStatus() {
        if (updateRightNow) {
            updateRightNow = false;
            pd.dismiss();
            Toast.makeText(getContext(), "Treehouses Cli has been updated!!!", Toast.LENGTH_LONG).show();
        }
        if (outs.get(3).equals("false ")) {
            ivUpgrade.setImageDrawable(getResources().getDrawable(R.drawable.tick));
            tvUpgrade.setText("Upgrade Status: Latest Version");
            upgrade.setVisibility(View.GONE);
        } else {
            ivUpgrade.setImageDrawable(getResources().getDrawable(R.drawable.tick_png));
            tvUpgrade.setText("Upgrade Status: Required for Version: " + outs.get(3).substring(4));
            upgrade.setVisibility(View.VISIBLE);
        }
    }

    private void showRenameDialog() {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View mView = inflater.inflate(R.layout.dialog_rename_status,null);
        EditText mHostNameEditText = mView.findViewById(R.id.hostname);
        mHostNameEditText.setHint("New Name");
        AlertDialog alertDialog = createRenameDialog(mView, mHostNameEditText);
        alertDialog.show();
    }

    private AlertDialog createRenameDialog(View view, EditText mEditText) {
        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .setTitle("Rename " + deviceName.substring(0, deviceName.indexOf("-")))
                .setIcon(R.drawable.dialog_icon)
                .setPositiveButton("Rename", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (!mEditText.getText().toString().equals("")) {
                                    writeToRPI("treehouses rename " + mEditText.getText().toString());
                                    Toast.makeText(getContext(), "Raspberry Pi Renamed", Toast.LENGTH_LONG).show();
                                }
                                else {
                                    Toast.makeText(getContext(), "Please enter a new name", Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                )
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
    }


    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    public final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            FragmentActivity activity = getActivity();
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    checkStatusNow();
                    break;
                case Constants.MESSAGE_WRITE:
                    Log.e("StatusFragment", "WRITE");
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);

                    Log.d(TAG, "writeMessage = " + writeMessage);
                    break;
                case Constants.MESSAGE_READ:
                    Log.e("StatusFragment", "READ");
                    String readMessage = (String) msg.obj;
                    Log.d(TAG, "readMessage = " + readMessage);
                    outs.add(readMessage);

                    updateStatus();
                    //TODO: if message is json -> callback from RPi
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    break;
            }
        }
    };
}
