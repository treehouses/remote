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

import io.treehouses.remote.Constants;
import io.treehouses.remote.R;
import io.treehouses.remote.bases.BaseFragment;

public class NewNetworkFragment extends BaseFragment implements View.OnClickListener {

    private Button wifiButton, hotspotButton, bridgeButton, ethernetButton;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.new_network, container, false);
        mChatService = listener.getChatService();
        mChatService.updateHandler(mHandler);

        wifiButton = view.findViewById(R.id.network_wifi);
        hotspotButton = view.findViewById(R.id.network_hotspot);
        bridgeButton = view.findViewById(R.id.network_bridge);
        ethernetButton = view.findViewById(R.id.network_ethernet);

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
                break;
            case R.id.network_hotspot:
                break;
            case R.id.network_bridge:
                break;
            case R.id.network_ethernet:
                break;
            case R.id.button_network_mode:
                break;
            case R.id.reboot_raspberry:
                break;
            case R.id.reset_network:
                break;
        }
    }

    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == Constants.MESSAGE_READ) {
                String readMessage = (String) msg.obj;
                Log.d("TAG", "readMessage = " + readMessage);


            }
        }
    };
}
