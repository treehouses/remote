package io.treehouses.remote.Fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.treehouses.remote.Constants;
import io.treehouses.remote.Fragments.DialogFragments.WifiDialogFragment;
import io.treehouses.remote.Network.BluetoothChatService;
import io.treehouses.remote.R;
import io.treehouses.remote.adapter.NetworkListAdapter;
import io.treehouses.remote.adapter.ViewHolderReboot;
import io.treehouses.remote.bases.BaseFragment;
import io.treehouses.remote.pojo.NetworkListItem;
import io.treehouses.remote.utils.ButtonConfiguration;

public class NetworkFragment extends BaseFragment {

    private static NetworkFragment instance = null;
    private int lastPosition = -1;
    private BluetoothChatService mChatService = null;
    private Boolean alert = true;
    private Boolean changeList = true;
    private ExpandableListView expListView;
    private NetworkListAdapter adapter;
    private ButtonConfiguration buttonConfiguration;
    View view;

    public NetworkFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_network_fragment, container, false);
        expListView = view.findViewById(R.id.lvExp);
        instance = this;
        mChatService = listener.getChatService();
        mChatService.updateHandler(mHandler);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter = new NetworkListAdapter(getContext(), NetworkListItem.getNetworkList(), mChatService);
        adapter.setListener(listener);
        expListView.setOnGroupExpandListener(groupPosition -> {
            if (groupPosition == 6) {
                Log.e("TAG", "groupPosition: " + groupPosition);
                expListView.collapseGroup(6);
                updateNetworkMode();
            }

            if (lastPosition != -1 && groupPosition != lastPosition) {
                expListView.collapseGroup(lastPosition);
            }
            lastPosition = groupPosition;
        });
        expListView.setAdapter(adapter);
        expListView.setGroupIndicator(null);
    }

    @Override
    public void onResume() {
        super.onResume();
        alert = false;
        expListView.expandGroup(6);
    }

    public static NetworkFragment getInstance() {
        return instance;
    }

    public void setButtonConfiguration(ButtonConfiguration buttonConfiguration) {
        this.buttonConfiguration = buttonConfiguration;
    }

    public ButtonConfiguration getButtonConfiguration(){
        return buttonConfiguration;
    }

    private void updateNetworkMode() {
        changeList = true;
        listener.sendMessage("treehouses networkmode");
        Log.e("TAG", "network mode updated");
        Toast.makeText(getContext(), "Network Mode updated", Toast.LENGTH_SHORT).show();
    }

    public AlertDialog showAlertDialog(final String message, final String title) {
        return new AlertDialog.Builder(getContext())
                .setTitle(title)
                .setMessage(message)
                .setIcon(R.drawable.wificon)
                .setPositiveButton("No", (dialog, which) -> dialog.cancel())
                .setNegativeButton("Yes", (dialog, which) -> {
                    switch (title) {
                        case "Reset":
                            caseReset();
                            break;
                        case "Reboot":
                            ViewHolderReboot.getInstance().reboot(listener, mChatService, getContext());
                            break;
                    }
                }).show();
    }

    private void caseReset() {
        listener.sendMessage("treehouses default network");
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(2000);
                updateNetworkMode();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    private AlertDialog showAlertDialog(final String message) {
        return new AlertDialog.Builder(getContext())
                .setTitle("OUTPUT:")
                .setMessage(message)
                .setIcon(R.drawable.wificon)
                .setNegativeButton("OK", (dialog, which) -> dialog.cancel()).show();
    }

    public void showWifiDialog(View v) {
        androidx.fragment.app.DialogFragment dialogFrag = WifiDialogFragment.newInstance();
        dialogFrag.setTargetFragment(this, Constants.REQUEST_DIALOG_FRAGMENT_HOTSPOT);
        dialogFrag.show(getActivity().getSupportFragmentManager().beginTransaction(), "wifiDialog");
    }

    private void changeList(String readMessage) {
        listener.sendMessage("treehouses networkmode info");
        adapter.changeList("Network Mode: " + readMessage, 6);
        switch (readMessage) {
            case "default": // ethernet
                expListView.expandGroup(0);
                adapter.changeList("Ethernet: Automatic", 0);
                break;
            case "static ethernet": // ethernet
                expListView.expandGroup(0);
                adapter.changeList("Ethernet: Manual", 0);
                break;
            case "wifi": // wifi
                expListView.expandGroup(1);
                break;
            case "ap internet": // hotspot
            case "ap local": // hotspot
                expListView.expandGroup(2);
                break;
            case "bridge": // bridge
                expListView.expandGroup(3);
                break;
        }
    }

    private Boolean btnConfigValidation(String readMessage) {
        Button btnConfig = view.findViewById(R.id.btn_start_config);
        if (buttonConfiguration == null) return false;
        switch (readMessage) {
            case "This pirateship has anchored successfully!": break; // hotspot or ethernet
            case "the bridge has been built ;), a reboot is required to apply changes": break; // bridge
            case "open wifi network": break; // wifi with no password
            case "password network": // wifi with password
                alert = false;
                buttonConfiguration.buttonProperties(true, Color.WHITE, btnConfig);
                buttonConfiguration.setMessageSent(false);
                updateNetworkMode();
                return true;
        }

        if (readMessage.contains("Error")) {
            try {
                buttonConfiguration.setMessageSent(false);
                buttonConfiguration.buttonProperties(true, Color.WHITE, btnConfig);
                alert = true;
            } catch (Exception e) {
                e.printStackTrace();
                changeList("Error");
            }
        }
        return false;
    }

    private void prefill(String readMessage) {
        if (readMessage.contains("essid")) {
            String[] array = readMessage.split(",");
            for (String element : array) {
                elementConditions(element);
            }
        }
    }

    private void elementConditions(String element) {
        Log.e("TAG", "networkmode= " + element);
        if (element.contains("wlan0") && !element.contains("ap essid")) {                   // bridge essid
            setSSIDText(element.substring(14).trim());
        } else if (element.contains("ap essid")) {                                          // ap essid
            setSSIDText(element.substring(16).trim());
        } else if (element.contains("ap0")) {                                               // hotspot essid for bridge
            ButtonConfiguration.getEtHotspotEssid().setText(element.substring(11).trim());
        } else if (element.contains("essid")) {                                             // wifi ssid
            setSSIDText(element.substring(6).trim());
        }
    }

    private void setSSIDText(String substring) {
        if (ButtonConfiguration.getSSID() != null)
            ButtonConfiguration.getSSID().setText(substring);
    }

    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == Constants.MESSAGE_READ) {
                String readMessage = (String) msg.obj;
                Log.d("TAG", "readMessage = " + readMessage);

                Boolean result = btnConfigValidation(readMessage.trim());
                if (result || readMessage.trim().equals("false") || readMessage.trim().contains("true")) {
                    return;
                }

                prefill(readMessage);

                if (readMessage.contains("please reboot your device")) {
                    alert = true;
                    changeList = false;
                }

                if (changeList) {
                    changeList(readMessage.trim());
                    changeList = false;
                }

                if (alert) {
                    showAlertDialog(readMessage);
                    alert = false;
                }
            }
        }
    };

}