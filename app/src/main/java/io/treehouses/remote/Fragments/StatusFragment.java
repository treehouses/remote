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

import androidx.annotation.NonNull;
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
    private Button upgrade;
    private ProgressDialog pd;
    private Boolean updateRightNow = false;
    private BluetoothChatService mChatService = null;
    private CardView cardRPIName;
    private NotificationCallback notificationListener;
    private String lastCommand = "hostname";
    private String deviceName = "";
    private String rpiVersion = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_status_fragment, container, false);
        initializeUIElements(view);
        mChatService = listener.getChatService();
        mChatService.updateHandler(mHandler);
        deviceName = mChatService.getConnectedDeviceName();

        tvStatus.setText(String.format("Bluetooth Connection: %s", deviceName));

        Log.e("STATUS", "device name: " + deviceName);
        if (mChatService.getState() == Constants.STATE_CONNECTED) {
            btStatus.setImageDrawable(getResources().getDrawable(R.drawable.tick));
        }
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
        upgrade.setOnClickListener(v -> {
            writeToRPI("treehouses upgrade");
            updateRightNow = true;
            pd = ProgressDialog.show(getActivity(), "Updating...", "Please wait a few seconds...");
            pd.setCanceledOnTouchOutside(true);

        });
    }

    private void rpiNameOnViewClickListener() {
        cardRPIName.setOnClickListener(v -> showRenameDialog());
    }

    private void updateStatus(String readMessage) {
        Log.d(TAG, "updateStatus: " + lastCommand + " response " + readMessage);
        if (lastCommand.equals("hostname")) {
            setCard(tvStatus2, btRPIName, "Connected RPI Name: " + readMessage);
            writeToRPI("treehouses remote status");
        } else if (readMessage.split(" ").length == 5 && lastCommand.equals("treehouses remote status")) {
            String[] res = readMessage.split(" ");
            setCard(tvStatus1, wifiStatus, "RPI Wifi Connection : " + res[0]);
            tvImage.setText(String.format("Treehouses Image Version: %s", res[2]));
            setCard(tvStatus3, rpiType, "RPI Type : " + res[4]);
            rpiVersion = res[3];
            writeToRPI("treehouses memory free");
        } else if (lastCommand.equals("treehouses memory free")) {
            setCard(tvMemory, memoryStatus, "Memory: " + readMessage + "bytes available");
            writeToRPI("treehouses internet");
        } else if (lastCommand.equals("treehouses internet")) {
            checkWifiStatus(readMessage);
        } else {
            checkUpgradeStatus(readMessage);
        }
    }

    private void checkWifiStatus(String readMessage) {
        tvStatus1.setText(String.format("RPI Wifi Connection: %s", readMessage));
        if (readMessage.startsWith("true")) {
            wifiStatus.setImageDrawable(getResources().getDrawable(R.drawable.tick));
            writeToRPI("treehouses upgrade --check");
        } else {
            wifiStatus.setImageDrawable(getResources().getDrawable(R.drawable.tick_png));
            tvUpgrade.setText("Upgrade Status: NO INTERNET");
            upgrade.setVisibility(View.GONE);
        }
    }

    private void writeToRPI(String ping) {
        lastCommand = ping;
        byte[] pSend = ping.getBytes();
        mChatService.write(pSend);
    }

    private void setCard(TextView textView, ImageView tick, String text) {
        textView.setText(text);
        tick.setImageDrawable(getResources().getDrawable(R.drawable.tick));
    }

    private void checkUpgradeNow() {
        if (updateRightNow) {
            updateRightNow = false;
            pd.dismiss();
            Toast.makeText(getContext(), "Treehouses Cli has been updated!!!", Toast.LENGTH_LONG).show();
            notificationListener.setNotification(false);
            getFragmentManager().beginTransaction().replace(R.id.fragment_container, new StatusFragment()).commit();
        }
    }
    private void checkUpgradeStatus(String readMessage) {
        checkUpgradeNow();
        if (readMessage.startsWith("false ") && readMessage.length() < 14) {
            ivUpgrade.setImageDrawable(getResources().getDrawable(R.drawable.tick));
            tvUpgrade.setText(String.format("Upgrade Status: Latest Version: %s", rpiVersion));
            upgrade.setVisibility(View.GONE);
        } else if (readMessage.startsWith("true ") && readMessage.length() < 14){
            ivUpgrade.setImageDrawable(getResources().getDrawable(R.drawable.tick_png));
            tvUpgrade.setText(String.format("Upgrade available from %s to %s", rpiVersion, readMessage.substring(4)));
            upgrade.setVisibility(View.VISIBLE);
        }
    }

    private void showRenameDialog() {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View mView = inflater.inflate(R.layout.dialog_rename_status, null);
        EditText mHostNameEditText = mView.findViewById(R.id.hostname);
        mHostNameEditText.setHint("New Name");
        AlertDialog alertDialog = createRenameDialog(mView, mHostNameEditText);
        alertDialog.show();
    }

    private AlertDialog createRenameDialog(View view, EditText mEditText) {
        return new AlertDialog.Builder(getActivity())
                .setView(view).setTitle("Rename " + deviceName.substring(0, deviceName.indexOf("-"))).setIcon(R.drawable.dialog_icon)
                .setPositiveButton("Rename", (dialog, which) -> {
                    if (!mEditText.getText().toString().equals("")) {
                        writeToRPI("treehouses rename " + mEditText.getText().toString());
                        Toast.makeText(getContext(), "Raspberry Pi Renamed", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getContext(), "Please enter a new name", Toast.LENGTH_LONG).show();
                    }
                }
                )
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                .create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try { notificationListener = (NotificationCallback) getContext();
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
                    String writeMessage = new String(writeBuf);
                    Log.d(TAG, "writeMessage = " + writeMessage);
                    break;
                case Constants.MESSAGE_READ:
                    String readMessage = (String) msg.obj;
                    Log.d(TAG, "readMessage = " + readMessage);
                    updateStatus(readMessage);
                    break;
            }
        }
    };
}
