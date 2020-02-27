package io.treehouses.remote.Fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import io.treehouses.remote.Constants;
import io.treehouses.remote.Fragments.DialogFragments.BottomSheetDialogs.BridgeBottomSheet;
import io.treehouses.remote.Fragments.DialogFragments.BottomSheetDialogs.EthernetBottomSheet;
import io.treehouses.remote.Fragments.DialogFragments.BottomSheetDialogs.HotspotBottomSheet;
import io.treehouses.remote.Fragments.DialogFragments.BottomSheetDialogs.WifiBottomSheet;
import io.treehouses.remote.Network.BluetoothChatService;
import io.treehouses.remote.R;
import io.treehouses.remote.bases.BaseFragment;

public class NewNetworkFragment extends BaseFragment implements View.OnClickListener {

    private Button wifiButton, hotspotButton, bridgeButton, ethernetButton;
    private Button updateNetwork, rebootPi, resetNetwork;
    private TextView currentNetworkMode;
    private BluetoothChatService mChatService;
    private ProgressBar progressBar;

    public static String CLICKED_START_CONFIG = "clicked_config";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.new_network, container, false);

        mChatService = listener.getChatService();
        mChatService.updateHandler(mHandler);

        //Main Buttons
        wifiButton = view.findViewById(R.id.network_wifi);
        hotspotButton = view.findViewById(R.id.network_hotspot);
        bridgeButton = view.findViewById(R.id.network_bridge);
        ethernetButton = view.findViewById(R.id.network_ethernet);

        //Commands
        updateNetwork = view.findViewById(R.id.button_network_mode);
        rebootPi = view.findViewById(R.id.reboot_raspberry);
        resetNetwork = view.findViewById(R.id.reset_network);

        currentNetworkMode = view.findViewById(R.id.current_network_mode);
        progressBar = view.findViewById(R.id.network_pbar);

        //Listeners
        wifiButton.setOnClickListener(this);
        hotspotButton.setOnClickListener(this);
        bridgeButton.setOnClickListener(this);
        ethernetButton.setOnClickListener(this);

        updateNetwork.setOnClickListener(this);
        rebootPi.setOnClickListener(this);
        resetNetwork.setOnClickListener(this);

        updateNetworkMode();

        return view;
    }

    private void writeToRPI(String ping) {
        mChatService.write(ping.getBytes());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.network_wifi:
                WifiBottomSheet wifiBottomSheet = new WifiBottomSheet(listener, getContext());
                wifiBottomSheet.setTargetFragment(NewNetworkFragment.this, Constants.NETWORK_BOTTOM_SHEET);
                wifiBottomSheet.show(getFragmentManager(), "wifi");
                break;
            case R.id.network_hotspot:
                HotspotBottomSheet hotspotBottomSheet = new HotspotBottomSheet(listener, getContext());
                hotspotBottomSheet.setTargetFragment(NewNetworkFragment.this, Constants.NETWORK_BOTTOM_SHEET);
                hotspotBottomSheet.show(getFragmentManager(), "hotspot");
                break;
            case R.id.network_bridge:
                BridgeBottomSheet bridgeBottomSheet = new BridgeBottomSheet(listener, getContext());
                bridgeBottomSheet.setTargetFragment(NewNetworkFragment.this, Constants.NETWORK_BOTTOM_SHEET);
                bridgeBottomSheet.show(getFragmentManager(), "bridge");
                break;
            case R.id.network_ethernet:
                EthernetBottomSheet ethernetBottomSheet = new EthernetBottomSheet(listener, getContext());
                ethernetBottomSheet.setTargetFragment(NewNetworkFragment.this, Constants.NETWORK_BOTTOM_SHEET);
                ethernetBottomSheet.show(getFragmentManager(), "ethernet");
                break;
            case R.id.button_network_mode:
                updateNetworkMode();
                break;
            case R.id.reboot_raspberry:
                reboot();
                break;
            case R.id.reset_network:
                resetNetwork();
                break;
        }
    }

    private void updateNetworkText(String mode) {
        currentNetworkMode.setText("Current Network Mode: " + mode);
    }

    private void updateNetworkMode() {
        writeToRPI("treehouses networkmode");
        Toast.makeText(getContext(), "Network Mode updated", Toast.LENGTH_LONG).show();
    }

    private boolean isNetworkModeReturned(String output) {
        switch (output.trim()) {
            case "wifi":
            case "bridge":
            case "ap local":
            case "ap internet":
            case "static wifi":
            case "static ethernet":
            case "default":
                return true;
            default:
                return false;
        }
    }

    private boolean isConfigReturned(String output) {
        if (output.contains("pirateship has anchored successfully") || output.contains("the bridge has been built")) {
            return true;
        }
        else if (output.contains("open wifi network") || output.contains("password network")) {
            return true;
        }
        return false;
    }


    private void performAction(String output) {
        //Return from treehouses networkmode
        if (isNetworkModeReturned(output)) {
            updateNetworkText(output);
        }
        //Return from Reset Network
        else if (output.startsWith("Success: the network mode has")) {
            Toast.makeText(getContext(), "Network Mode switched to default", Toast.LENGTH_LONG).show();
            updateNetworkMode();
        }
        //Error occurred
        else if (output.toLowerCase().contains("error")) {
            showDialog("Error", "An error has occurred.");
            progressBar.setVisibility(View.GONE);
        }

        //Returned from choosing a network
        else if (isConfigReturned(output)) {
            showDialog("Network Switched", output);
            updateNetworkMode();
            progressBar.setVisibility(View.GONE);
        }

    }
    private void showDialog(String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(getContext()).setTitle(title).setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
        alertDialog.show();
    }

    private void reboot() {
        AlertDialog a = new AlertDialog.Builder(getContext())
                .setTitle("Reboot")
                .setMessage("Are you sure you want to reboot your device?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            Log.d("", "reboot: ");
                            listener.sendMessage("reboot");
                            Thread.sleep(1000);
                            if (mChatService.getState() != Constants.STATE_CONNECTED) {
                                Toast.makeText(getContext(), "Bluetooth Disconnected: Reboot in progress", Toast.LENGTH_LONG).show();
                                listener.openCallFragment(new HomeFragment());
                            } else {
                                Toast.makeText(getContext(), "Reboot Unsuccessful", Toast.LENGTH_LONG).show();
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        dialog.dismiss();
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
        a.show();
    }

    private void resetNetwork() {
        AlertDialog a = new AlertDialog.Builder(getContext())
                .setTitle("Reset Network")
                .setMessage("Are you sure you want to reset the network to default?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.sendMessage("treehouses default network");
                        Toast.makeText(getContext(), "Switching to default network...", Toast.LENGTH_LONG).show();
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
        a.show();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if( resultCode != Activity.RESULT_OK ) {
            return;
        }
        if( requestCode == Constants.NETWORK_BOTTOM_SHEET && data.getBooleanExtra(CLICKED_START_CONFIG, false)) {
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == Constants.MESSAGE_READ) {
                String readMessage = (String) msg.obj;
                Log.d("TAG", "readMessage = " + readMessage);
                performAction(readMessage);
            }
        }
    };

//    Next Version:
//    private void elementConditions(String element) {
//        Log.e("TAG", "networkmode= " + element);
//        if (element.contains("wlan0") && !element.contains("ap essid")) {                   // bridge essid
//            setSSIDText(element.substring(14).trim());
//        } else if (element.contains("ap essid")) {                                          // ap essid
//            setSSIDText(element.substring(16).trim());
//        } else if (element.contains("ap0")) {                                               // hotspot essid for bridge
//            ButtonConfiguration.getEtHotspotEssid().setText(element.substring(11).trim());
//        } else if (element.contains("essid")) {                                             // wifi ssid
//            setSSIDText(element.substring(6).trim());
//        }
//    }
}
