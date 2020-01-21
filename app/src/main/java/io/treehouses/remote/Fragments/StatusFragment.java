package io.treehouses.remote.Fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
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

import androidx.cardview.widget.CardView;

import java.util.ArrayList;
import java.util.List;

import io.treehouses.remote.Constants;
import io.treehouses.remote.Network.BluetoothChatService;
import io.treehouses.remote.R;
import io.treehouses.remote.bases.BaseFragment;
import io.treehouses.remote.callback.NotificationCallback;

public class StatusFragment extends BaseFragment {
    View view;

    private static final String TAG = "StatusFragment";
    private ImageView wifiStatus, btRPIName, rpiType, memoryStatus;
    private ImageView btStatus, ivUpgrade;
    private TextView tvStatus, tvStatus1, tvStatus2, tvStatus3, tvUpgrade, tvMemory, tvImage;
    private List<String> outs = new ArrayList<>();
    private Boolean wifiStatusVal = false;
    private Button upgrade;
    private ProgressDialog pd;
    private Boolean updateRightNow = false;
    private BluetoothChatService mChatService = null;
    private CardView cardRPIName;

    private NotificationCallback notificationListener;
    /**
     * Name of the connected device
     */
    private String deviceName = "";
    private String rpiVersion="";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_status_fragment, container, false);
        initializeUIElements(view);

        mChatService = listener.getChatService();
        mChatService.updateHandler(mHandler);
        deviceName = mChatService.getConnectedDeviceName();

        tvStatus.setText("Bluetooth Connection: " + deviceName);

        Log.e("STATUS", "device name: " + deviceName);
        if (mChatService.getState() == Constants.STATE_CONNECTED) { btStatus.setImageDrawable(getResources().getDrawable(R.drawable.tick)); }
        checkStatusNow();

        String ping = "hostname";
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
        tvImage = view.findViewById(R.id.image_text);
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

                if (outs.size()>5) rpiVersion = outs.get(5).substring(4);
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

    private void updateStatus() {
        switch (outs.size()) {
            case 1:
                setCard(tvStatus2, btRPIName, "Connected RPI Name: " + outs.get(0), "treehouses detectrpi");
                break;
            case 2:
                setCard(tvStatus3, rpiType, "RPI Type: " + outs.get(1), "treehouses image");
                break;
            case 3:
                setImage();
                break;
            case 4:
                setVersion();
                break;
            case 5:
                setCard(tvMemory, memoryStatus, "Memory: " + outs.get(4) + "bytes available", "treehouses internet");
                break;
            default:
                checkWifiUpgrade(outs.size());
                break;

        }
    }
    private void checkWifiUpgrade(int size) {
        switch(size) {
            case 6:
                checkWifiStatus();
                break;
            case 7:
                checkUpgradeStatus();
                break;
            case 8:
                outs.remove(6);
                outs.remove(6);
                checkWifiStatus();
                break;
        }
    }

    private void writeToRPI(String ping) {
        byte[] pSend = ping.getBytes();
        mChatService.write(pSend);
    }
    private void setCard(TextView textView, ImageView tick, String text, String command) {
        textView.setText(text);
        tick.setImageDrawable(getResources().getDrawable(R.drawable.tick));
        writeToRPI(command);
    }

    private void setImage() {
        tvImage.setText("Treehouses Image Version: "+ outs.get(2));
        writeToRPI("treehouses version");

    }

    private void setVersion() {
        rpiVersion = outs.get(3);
        writeToRPI("treehouses memory free");
    }

    private void checkWifiStatus() {
        tvStatus1.setText("RPI Wifi Connection: " + outs.get(5));
        if (outs.get(5).equals("true ")) {
            wifiStatusVal = true;
            wifiStatus.setImageDrawable(getResources().getDrawable(R.drawable.tick));
        }
        else {
            wifiStatusVal = false;
            wifiStatus.setImageDrawable(getResources().getDrawable(R.drawable.tick_png));
        }
        if (wifiStatusVal) { writeToRPI("treehouses upgrade --check"); }
        else {
            tvUpgrade.setText("Upgrade Status: NO INTERNET");
            upgrade.setVisibility(View.GONE);
        }
    }

    private void checkUpgradeStatus() {
        if (updateRightNow) {
            updateRightNow = false;
            pd.dismiss();
            Toast.makeText(getContext(), "Treehouses Cli has been updated!!!", Toast.LENGTH_LONG).show();
            notificationListener.setNotification(false);
        }
        if (outs.get(6).equals("false ")) {
            ivUpgrade.setImageDrawable(getResources().getDrawable(R.drawable.tick));
            tvUpgrade.setText("Upgrade Status: Latest Version: " + rpiVersion);
            upgrade.setVisibility(View.GONE);
        } else {
            ivUpgrade.setImageDrawable(getResources().getDrawable(R.drawable.tick_png));
            if (outs.get(6).length()>4) {
                tvUpgrade.setText("Upgrade available from "+ rpiVersion +" to " + outs.get(6).substring(4));
            }
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
                .setView(view).setTitle("Rename " + deviceName.substring(0, deviceName.indexOf("-"))).setIcon(R.drawable.dialog_icon)
                .setPositiveButton("Rename", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (!mEditText.getText().toString().equals("")) {
                                    writeToRPI("treehouses rename " + mEditText.getText().toString());
                                    Toast.makeText(getContext(), "Raspberry Pi Renamed", Toast.LENGTH_LONG).show();
                                }
                                else { Toast.makeText(getContext(), "Please enter a new name", Toast.LENGTH_LONG).show(); }
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
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            notificationListener = (NotificationCallback) getContext();
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NotificationListener");
        }
    }

    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    public final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    checkStatusNow();
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);

                    Log.d(TAG, "writeMessage = " + writeMessage);
                    break;
                case Constants.MESSAGE_READ:
                    String readMessage = (String) msg.obj;
                    Log.d(TAG, "readMessage = " + readMessage);
                    outs.add(readMessage);

                    updateStatus();
                    break;
            }
        }
    };
}
