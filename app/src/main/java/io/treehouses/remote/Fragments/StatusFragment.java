package io.treehouses.remote.Fragments;

import androidx.fragment.app.FragmentActivity;
import io.treehouses.remote.Constants;
import io.treehouses.remote.Network.BluetoothChatService;
import io.treehouses.remote.R;
import io.treehouses.remote.bases.BaseFragment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class StatusFragment extends BaseFragment {
    public StatusFragment() {}

    View view;

    private static final String TAG = "StatusFragment";
    private ImageView wifiStatus, btRPIName, rpiType;
    private ImageView btStatus, ivUpgrad;
    private TextView tvStatus1, tvStatus2, tvStatus3, tvUpgrade;
    private List<String> outs = new ArrayList<>();
    private Boolean wifiStatusVal = false;
    private Button upgrade;
    private ProgressDialog pd;
    private Boolean updateRightNow = false;
    private BluetoothChatService mChatService = null;
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
        ivUpgrad = view.findViewById(R.id.upgradeCheck);
        tvStatus1 = view.findViewById(R.id.tvStatus1);
        tvStatus2 = view.findViewById(R.id.tvStatus2);
        tvStatus3 = view.findViewById(R.id.tvStatus3);
        tvUpgrade = view.findViewById(R.id.tvUpgradeCheck);
        upgrade = view.findViewById(R.id.upgrade);
        upgrade.setVisibility(View.GONE);

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

    public void checkStatusNow() {
        Log.e("DEVICE", "" + mConnectedDeviceName);
    }

    private void updateStatus() {
        setRPIDeviceName();
        if (outs.size() == 1) {
            setRPIType();
        }
        if (outs.size() == 2) {
            checkWifiStatus();
        }
        if (outs.size() == 3) {
            checkUpgradeStatus();
        }
        if (outs.size() == 4) {
            outs.remove(2);
            outs.remove(2);
            checkWifiStatus();
        }
    }

    private void writeToRPI(String ping) {
        byte[] pSend = ping.getBytes();
        mChatService.write(pSend);
    }

    private void setRPIDeviceName() {
        tvStatus2.setText("Connected RPI Name: " + deviceName);
        btRPIName.setImageDrawable(getResources().getDrawable(R.drawable.tick));
    }

    private void setRPIType() {
        tvStatus3.setText("RPI Type: " + outs.get(0));
        rpiType.setImageDrawable(getResources().getDrawable(R.drawable.tick));
        writeToRPI("treehouses internet");
    }

    private void checkWifiStatus() {
        tvStatus1.setText("RPI Wifi Connection: " + outs.get(1));
        Log.e("StatusFragment", "**" + outs.get(1) + "**" + outs.get(1).equals("true "));
        if (outs.get(1).equals("true ")) {
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
        if (outs.get(2).equals("false ")) {
            ivUpgrad.setImageDrawable(getResources().getDrawable(R.drawable.tick));
            tvUpgrade.setText("Upgrade Status: Latest Version");
            upgrade.setVisibility(View.GONE);
        } else {
            ivUpgrad.setImageDrawable(getResources().getDrawable(R.drawable.tick_png));
            tvUpgrade.setText("Upgrade Status: Required for Version: " + outs.get(2).substring(4));
            upgrade.setVisibility(View.VISIBLE);
        }
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
