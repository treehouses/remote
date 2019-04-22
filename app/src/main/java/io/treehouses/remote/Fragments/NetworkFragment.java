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
import io.treehouses.remote.bases.BaseFragment;
import io.treehouses.remote.MiscOld.Constants;
import io.treehouses.remote.Network.BluetoothChatService;
import io.treehouses.remote.R;
import io.treehouses.remote.ExpandableListAdapter;

import static io.treehouses.remote.MiscOld.Constants.getGroups;

public class NetworkFragment extends BaseFragment {

    View view;
    private BluetoothChatService mChatService = null;
    private Boolean alert = true;
    private Boolean networkStatus = false;
    private Boolean bridge = false;
    private ExpandableListView expListView;
    ExpandableListAdapter adapter;
    private String[][] children;
    private int count = 0;
    private boolean first;
    private int CurrentExpandedGroup = 0;
    private int i = 0;
    private int j = 0;

    public NetworkFragment() { }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        children = new String[][]{
                {"\tIP Address", "\tDNS", "\tGateway", "\tMask", "Start Configuration"},
                {"\tESSID", "\tPassword", "Start Configuration"},
                {"\tESSID", "\tPassword", "Spinner", "Start Configuration"},
                {"\tESSID", "\tPassword", "\tHotspot ESSID", "\tHotspot Password", "Start Configuration"},
                {"Reset Network"},
                {"Reboot RPI"},
                {""}
        };
        Constants.setGroups();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_network_fragment, container, false);
        mChatService = listener.getChatService();
        mChatService.updateHandler(mHandler);
        updateNetworkMode();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter = new ExpandableListAdapter(getContext(), getGroups(), children, mChatService);
        expListView = view.findViewById(R.id.lvExp);

        expListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                String group = getGroups().get(groupPosition);
                if (group.contains("Network Mode")) {
                    expListView.collapseGroup(groupPosition);
                    if (group.contains("default")) { expListView.expandGroup(0); }
                    else if (group.contains("wifi")) { expListView.expandGroup(1); }
                    else if (group.contains("ap local") || group.contains("ap internet")) { expListView.expandGroup(2); }
                    else if (group.contains("bridge")) { expListView.expandGroup(3); }
                    alert = false;
                    Toast.makeText(getContext(), "Network Mode updated", Toast.LENGTH_LONG).show();
                }
                if (count == 0) { first = true;
                } else { first = false; }
                ++count;
                expandOneGroup();
            }
        });

        expListView.setAdapter(adapter);
        expListView.setGroupIndicator(null);
    }

    //makes sure that only one group is expanded at once
    private void expandOneGroup() {
        currentExpandedGroup();

        if (i == CurrentExpandedGroup) {
            otherExpandedGroup();
        }
        if (j == 7 && i != j) {
            j = 0;
        }
    }

    private void currentExpandedGroup() {
        for (; i < getGroups().size(); i++) {
            if (expListView.isGroupExpanded(i)) {
                CurrentExpandedGroup = i;
                break;
            }
        }
    }

    private void otherExpandedGroup() {
        for (; j < getGroups().size(); j++) {
            if (expListView.isGroupExpanded(j) && j != i) {
                if (!first) {
                    expListView.collapseGroup(i);
                }
                i = j;
            }
        }
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
                    if (networkStatus) {
                        changeList(readMessage);
                        networkStatus = false;
                        alert = false;
                    }
                    if (alert) { showAlertDialog(readMessage);
                    } else { alert = false; }
                    if (bridge) { updateNetworkMode();
                    } else{ alert = true; }
                    break;
            }
        }
    };

    private void changeList(String readMessage) {
        if (getGroups() != null && getGroups().size() >= 6) {
            Constants.changeGroup(readMessage);
            expListView.performItemClick(null, 6, expListView.getItemIdAtPosition(6));
            alert = false;
        }
    }
}
