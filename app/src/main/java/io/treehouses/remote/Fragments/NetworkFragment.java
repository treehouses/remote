package io.treehouses.remote.Fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import io.treehouses.remote.adapter.NetworkListAdapter;
import io.treehouses.remote.bases.BaseFragment;
import io.treehouses.remote.MiscOld.Constants;
import io.treehouses.remote.Network.BluetoothChatService;
import io.treehouses.remote.R;
import io.treehouses.remote.pojo.NetworkListItem;


public class NetworkFragment extends BaseFragment {

    View view;
    private BluetoothChatService mChatService = null;
    private Boolean alert = true;
    private Boolean networkStatus = false;
    private Boolean bridge = false;
    private ExpandableListView expListView;
    private int lastPosition = -1;
    NetworkListAdapter adapter;
    public NetworkFragment() { }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_network_fragment, container, false);
        expListView = view.findViewById(R.id.lvExp);
        mChatService = listener.getChatService();
        mChatService.updateHandler(mHandler);
        updateNetworkMode();
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



    private void updateNetworkMode() {
        alert = false;
        networkStatus = true;
        listener.sendMessage("treehouses networkmode");
        Toast.makeText(getContext(), "Network Mode updated", Toast.LENGTH_SHORT).show();
    }

    private AlertDialog showAlertDialog(String message) {
        return new AlertDialog.Builder(getContext())
                .setTitle("OUTPUT:")
                .setMessage(message)
                .setIcon(R.drawable.wificon)
                .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .show();
    }

    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    public final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_READ:
                    String readMessage = (String) msg.obj;
                    Log.d("TAG", "readMessage = " + readMessage);
                    if (readMessage.trim().equals("password network") || readMessage.trim().contains("This pirateship has") || readMessage.trim().contains("the bridge has been built")) {
                        bridge = readMessage.trim().contains("the bridge has been built");
                        if (!bridge) { updateNetworkMode(); }
                        else { alert = true; }
                        if (networkStatus && !bridge) { return; }
                    }
                    if (readMessage.contains("please reboot your device")) { alert = true; }
                    Log.d("TAG", "readMessage = " + readMessage);
                    if (readMessage.trim().contains("default network")) { networkStatus = true; }
                    if (readMessage.trim().equals("false") || readMessage.trim().contains("true")) { return; }

                    if (networkStatus) {
                        changeList(readMessage);
                        networkStatus = false;
                        alert = false;
                    }
                    if (alert) { showAlertDialog(readMessage); }
                    else { alert = false; }
                    if (bridge) { updateNetworkMode(); }
                    else { alert = true; }
                    break;
            }
        }
    };

    private void changeList(String readMessage) {
        adapter.setNetworkMode("Network Mode: " + readMessage);
    }
}
