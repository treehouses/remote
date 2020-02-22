package io.treehouses.remote.Fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

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
    private TextView currentNetworkMode;
    private BluetoothChatService mChatService;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.new_network, container, false);

        mChatService = listener.getChatService();
        mChatService.updateHandler(mHandler);

        wifiButton = view.findViewById(R.id.network_wifi);
        hotspotButton = view.findViewById(R.id.network_hotspot);
        bridgeButton = view.findViewById(R.id.network_bridge);
        ethernetButton = view.findViewById(R.id.network_ethernet);

        currentNetworkMode = view.findViewById(R.id.current_network_mode);

        wifiButton.setOnClickListener(this);
        hotspotButton.setOnClickListener(this);
        bridgeButton.setOnClickListener(this);
        ethernetButton.setOnClickListener(this);


        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.network_wifi:
                Log.d("NETWORK", "WIFI");
                WifiBottomSheet wifiBottomSheet = new WifiBottomSheet(listener, getContext());
                wifiBottomSheet.show(getFragmentManager(), "wifi");
                break;
            case R.id.network_hotspot:
                HotspotBottomSheet hotspotBottomSheet = new HotspotBottomSheet();
                hotspotBottomSheet.show(getFragmentManager(), "hotspot");
                break;
            case R.id.network_bridge:
                BridgeBottomSheet bridgeBottomSheet = new BridgeBottomSheet();
                bridgeBottomSheet.show(getFragmentManager(), "bridge");
                break;
            case R.id.network_ethernet:
                EthernetBottomSheet ethernetBottomSheet = new EthernetBottomSheet();
                ethernetBottomSheet.show(getFragmentManager(), "ethernet");
                break;
            case R.id.button_network_mode:
                break;
            case R.id.reboot_raspberry:
                break;
            case R.id.reset_network:
                break;
        }
    }

    private void updateNetworkMode(String mode) {
        if (mode.contains("wifi")) {

        }
    }

    private void performAction(String output) {
        if (output.contains("connected to password network")) {
            updateNetworkMode("wifi");
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
}
