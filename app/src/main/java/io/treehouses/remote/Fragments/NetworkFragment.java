package io.treehouses.remote.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import io.treehouses.remote.ViewHolder;
import io.treehouses.remote.bases.BaseFragment;
import io.treehouses.remote.MiscOld.Constants;
import io.treehouses.remote.Network.BluetoothChatService;
import io.treehouses.remote.R;
import io.treehouses.remote.ExpandableListAdapter;

public class NetworkFragment extends BaseFragment {

    View view;
    private BluetoothChatService mChatService = null;
    String readMessage;
    Boolean alert = true;
    Boolean networkStatus = false;
    Boolean wifiDialog = false;
    Boolean bridge = false;
    ExpandableListView expListView;
    android.widget.ExpandableListAdapter adapter;
    private ArrayList<String> groups;
    private String[][] children;
    private int count = 0;
    private boolean first;
    private int CurrentExpandedGroup = 0;
    private int i = 0;
    private int j = 0;

    public NetworkFragment() {
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        children = new String[][]{
                {"\tDNS", "\tGateway", "\tSubnet", "Start Configuration"},
                {"\tESSID", "\tPassword", "Start Configuration"},
                {"\tESSID", "\tPassword", "Start Configuration"},
                {"\tESSID", "\tPassword", "\tHotspot ESSID", "\tHotspot Password", "Start Configuration"},
                {"Reset network mode"},
                {"Reboot raspberry pi"},
                {""}
        };

        groups = new ArrayList<>();
        groups.add("Ethernet: 192.168.0.100 - Automatic");
        groups.add("WiFi");
        groups.add("Hotspot");
        groups.add("Bridge");
        groups.add("Reset");
        groups.add("Reboot");
        groups.add("Network Mode: ");
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
        adapter = new ExpandableListAdapter(getContext(), groups, children, mChatService);
        expListView = view.findViewById(R.id.lvExp);

        expListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                String group = groups.get(groupPosition);
                if (group.contains("Network Mode")) {
                    expListView.collapseGroup(groupPosition);
                    if (group.contains("default")) {
                        expListView.expandGroup(0);
                    } else if (group.contains("wifi")) {
                        expListView.expandGroup(1);
                    } else if (group.contains("ap local")) {
                        expListView.expandGroup(2);
                    } else if (group.contains("bridge")) {
                        expListView.expandGroup(3);
                    }
                    alert = false;
                    Toast.makeText(getContext(), "Network Mode updated", Toast.LENGTH_LONG).show();
                }

                if (count == 0) {
                    first = true;
                } else {
                    first = false;
                }
                ++count;
                expandOneGroup();
            }
        });
//        ViewGroup parent = new ViewGroup(getContext()) {
//            @Override
//            protected void onLayout(boolean changed, int l, int t, int r, int b) {
//
//            }
//        };
//
//        LayoutInflater inf = LayoutInflater.from(getContext());
//        View convertView = inf.inflate(R.layout.list_child2, parent, false);
//        ViewHolder holder = new ViewHolder();
//
//        holder.editText = convertView.findViewById(R.id.lblListItem);
//        view.setTag(holder);
//
//        holder.editText.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//              //  Log.e("TAG", "Testing");
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                Toast.makeText(getContext(), "this is a toast:", Toast.LENGTH_SHORT).show();
//            }
//        });

        expListView.setAdapter(adapter);
        expListView.setGroupIndicator(null);

    public void updateNetworkMode() {
        alert = false;
        networkStatus = true;
        listener.sendMessage("treehouses networkmode");
        Toast.makeText(getContext(), "Network mode updated", Toast.LENGTH_LONG).show();
    }

    //makes sure that only one group is expanded at once
    public void expandOneGroup() {
        for (; i < groups.size(); i++) {
            if (expListView.isGroupExpanded(i)) {
                CurrentExpandedGroup = i;
                break;
            }
        }
        if (i == CurrentExpandedGroup) {
            for (; j < groups.size(); j++) {
                if (expListView.isGroupExpanded(j) && j != i) {
                    if (!first) {
                        expListView.collapseGroup(i);
                    }
                    i = j;
                }
            }
        }
        if (j == 7 && i != j) {
            j = 0;
        }
    }


    public void updateNetworkMode() {
        alert = false;
        networkStatus = true;
        listener.sendMessage("treehouses networkmode");
        Toast.makeText(getContext(), "Network mode updated", Toast.LENGTH_SHORT).show();
    }
      
    private void wifiOn(Bundle bundle) {
        alert = false;
        listener.sendMessage("treehouses wifi \"" + bundle.getString("SSID") + "\" \"" + bundle.getString("PWD") + "\"");
        Toast.makeText(getContext(), "Connecting...", Toast.LENGTH_LONG).show();
    }

    private void hotspotOn(Bundle bundle) {
        alert = false;
        if (bundle.getString("HPWD").equals("")) {
            listener.sendMessage("treehouses ap \"" + bundle.getString("hotspotType") + "\" \"" + bundle.getString("HSSID") + "\"");
        } else {
            listener.sendMessage("treehouses ap \"" + bundle.getString("hotspotType") + "\" \"" + bundle.getString("HSSID") + "\" \"" + bundle.getString("HPWD") + "\"");
        }
    }

    private void ethernetOn(Bundle bundle) {
        alert = true;
        listener.sendMessage("treehouses ethernet \"" + bundle.getString("ip") + "\" \"" + bundle.getString("mask") + "\" \"" + bundle.getString("gateway") + "\" \"" + bundle.getString("dns") + "\"");
    }

    private void bridgeOn(Bundle bundle) {
        alert = true;
        String overallMessage = "treehouses bridge \"" + (bundle.getString("essid")) + "\" \"" + bundle.getString("hssid") + "\" ";

        if (TextUtils.isEmpty(bundle.getString("password"))) {
            overallMessage += "\"\"";
        } else {
            overallMessage += "\"" + bundle.getString("password") + "\"";
        }
        overallMessage += " ";
        if (!TextUtils.isEmpty(bundle.getString("hpassword"))) {
            overallMessage += "\"" + bundle.getString("hpassword") + "\"";
        }
        Log.e("NetworkFragment", "Bridge RPI Message = " + overallMessage);
        listener.sendMessage(overallMessage);
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
                    readMessage = (String) msg.obj;
                    Log.d("TAG", "readMessage = " + readMessage);
                    if (readMessage.trim().equals("password network") || readMessage.trim().contains("This pirateship has") || readMessage.trim().contains("the bridge has been built")) {
                        bridge = readMessage.trim().contains("the bridge has been built");
                        if (!bridge) {
                            updateNetworkMode();
                        } else {
                            alert = true;
                        }
                        if (networkStatus && !bridge) { return; }
                    }
                    if (readMessage.contains("please reboot your device")) {
                        alert = true;
                    }
                    Log.d("TAG", "readMessage = " + readMessage);
                    if (networkStatus) {
                        changeList(readMessage);
                        networkStatus = false;
                        alert = false;
                    }
                    if (alert) {
                        showAlertDialog(readMessage);

                    } else {
                        alert = false;
                    }
                    if (bridge) { updateNetworkMode(); }
                        alert = false;
                    } else { alert = true; }
                    break;
            }
        }
    };

    private void changeList(String readMessage) {
        if (groups != null && groups.size() >= 6) {
            groups.remove(6);
            groups.add(6,"Network Mode: "+ readMessage);
            expListView.performItemClick(null, 6, expListView.getItemIdAtPosition(6));
            alert = false;
        }
    }
}
