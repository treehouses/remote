package io.treehouses.remote.Fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.treehouses.remote.callback.ButtonConfig;
import io.treehouses.remote.adapter.NetworkListAdapter;
import io.treehouses.remote.adapter.ViewHolderReboot;
import io.treehouses.remote.bases.BaseFragment;
import io.treehouses.remote.Constants;
import io.treehouses.remote.Network.BluetoothChatService;
import io.treehouses.remote.R;
import io.treehouses.remote.pojo.NetworkListItem;

public class NetworkFragment extends BaseFragment {

    private static NetworkFragment instance = null;
    private int lastPosition = -1;
    private BluetoothChatService mChatService = null;
    private Boolean alert = true;
    private Boolean changeList = true;
    private ExpandableListView expListView;
    private NetworkListAdapter adapter;
    private ButtonConfig btnConfig;
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
        expListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                if (groupPosition == 6) {
                    Log.e("TAG", "groupPosition: " + groupPosition);
                    expListView.collapseGroup(6);
                    updateNetworkMode();
                }

                if (lastPosition != -1 && groupPosition != lastPosition) {
                    expListView.collapseGroup(lastPosition);
                }
                lastPosition = groupPosition;
            }
        });
        expListView.setAdapter(adapter);
        expListView.setGroupIndicator(null);
    }

    @Override
    public void onResume() {
        super.onResume();
        alert = false;
        updateNetworkMode();
        Log.e("TAG", "onResume is called");
        expListView.expandGroup(6);
    }

    public static NetworkFragment getInstance() {
        return instance;
    }

    public void setBtnConfig(ButtonConfig btnConfig) {
        this.btnConfig = btnConfig;
    }

    private void updateNetworkMode() {
        changeList = true;
        listener.sendMessage("treehouses networkmode");
        Toast.makeText(getContext(), "Network Mode updated", Toast.LENGTH_SHORT).show();
    }

    public AlertDialog showAlertDialog(final String message, final String title) {
        return new AlertDialog.Builder(getContext())
                .setTitle(title)
                .setMessage(message)
                .setIcon(R.drawable.wificon)
                .setPositiveButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (title) {
                            case "Reset":
                                listener.sendMessage("treehouses default network");
                                break;
                            case "Reboot":
                                ViewHolderReboot.getInstance().reboot(listener, mChatService, getContext());
                                break;
                        }
                    }
                }).show();
    }

    private AlertDialog showAlertDialog(final String message) {
        return new AlertDialog.Builder(getContext())
                .setTitle("OUTPUT:")
                .setMessage(message)
                .setIcon(R.drawable.wificon)
                .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).show();
    }

    private void changeList(String readMessage) {
        adapter.setNetworkMode("Network Mode: " + readMessage);
        switch (readMessage) {
            case "default":
            case "static ethernet":
                expListView.expandGroup(0);
                break;
            case "wifi":
                expListView.expandGroup(1);
                break;
            case "internet":
            case "local":
                expListView.expandGroup(2);
                break;
            case "bridge":
                expListView.expandGroup(3);
                break;
        }
    }

    private Boolean btnConfigValidation(String readMessage) {
        switch (readMessage) {
            case "This pirateship has anchored successfully!": // hotspot or ethernet
            case "the bridge has been built ;), a reboot is required to apply changes": // bridge
            case "open wifi network": // wifi with no password
            case "password network": // wifi with password
                alert = false;
                btnConfig.btnConfigDisabled(true, Color.WHITE);
                updateNetworkMode();
                return true;
        }

        if (readMessage.contains("Error")) {
            btnConfig.btnConfigDisabled(true, Color.WHITE);
            alert = true;
        }
        return false;
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