package io.treehouses.remote.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.Toast;
import java.util.ArrayList;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
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
    ExpandableListView expListView;
    android.widget.ExpandableListAdapter adapter;
    private ArrayList<String> groups;
    private String[][] children;

    public NetworkFragment() {}

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        groups = new String[] { "Ethernet: 192.168.0.100 - Automatic", "WiFi", "Hotspot", "Bridge", "Reset", "Reboot", "Network Mode: " };

        children = new String [][] {
                {"\tDNS", "\tGateway", "\tSubnet"},
                {"\tESSID", "\tPassword"},
                {"\tESSID", "\tPassword"},
                {"\tESSID", "\tPassword", "\tHotspot ESSID", "\tHotspot Password"},
                {"\tReset now"},
                {"\tReboot now"},
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
        adapter = new ExpandableListAdapter(getContext(), groups, children);
        expListView = view.findViewById(R.id.lvExp);

        expListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                String group = groups.get(groupPosition);
                if (group.contains("Network Mode")) {
                    updateNetworkMode();
                    if (group.contains("ethernet")) {
                        expListView.expandGroup(0);
                    } else if (group.contains("wifi")) {
                        expListView.expandGroup(1);
                    } else if (group.contains("ap local")) {
                        expListView.expandGroup(2);
                    } else if (group.contains("bridge")) {
                        expListView.expandGroup(3);
                    }

                    expListView.collapseGroup(groupPosition);
                    alert = false;
                }

            }
        });

//        expListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
//            @Override
//            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
//                if (groupPosition == 6) {
//                    updateNetworkMode();
//                    return true;
//                }
//                return false;
//            }
//        });

        expListView.setAdapter(adapter);
        expListView.setGroupIndicator(null);

    }

    public void updateNetworkMode() {
        alert = false;
        networkStatus = true;
        listener.sendMessage("treehouses networkmode");
        Toast.makeText(getContext(), "Network mode updated", Toast.LENGTH_LONG).show();
    }

    public void getListFragment(int position) {
        switch (position) {
            case 0:
                showEthernetDialog();
                break;
            case 1:
                showWifiDialog();
                break;
            case 2:
                showHotspotDialog();
                break;
            case 3:
                showBridgeDialog();
                break;
            case 4:
                alert = true;
                listener.sendMessage("treehouses default network");
                break;
            case 5:
                alert = false;
                listener.sendMessage("reboot");
                try {
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
                break;
            default:
                Log.e("Default Network Switch", "Nothing...");
                break;
        }
    }

    public void showBridgeDialog() {
        androidx.fragment.app.DialogFragment dialogFrag = BridgeDialogFragment.newInstance(123);
        dialogFrag.setTargetFragment(this, Constants.REQUEST_DIALOG_FRAGMENT_HOTSPOT);
        dialogFrag.show(getFragmentManager().beginTransaction(), "bridgeDialog");
    }

    public void showEthernetDialog() {
        androidx.fragment.app.DialogFragment dialogFrag = EthernetDialogFragment.newInstance(123);
        dialogFrag.setTargetFragment(this, Constants.REQUEST_DIALOG_FRAGMENT_HOTSPOT);
        dialogFrag.show(getFragmentManager().beginTransaction(), "ethernetDialog");
    }

    public void showHotspotDialog() {
        androidx.fragment.app.DialogFragment dialogFrag = HotspotDialogFragment.newInstance(123);
        dialogFrag.setTargetFragment(this, Constants.REQUEST_DIALOG_FRAGMENT_HOTSPOT);
        dialogFrag.show(getFragmentManager().beginTransaction(), "hotspotDialog");
    }

    public void showWifiDialog() {
        androidx.fragment.app.DialogFragment dialogFrag = WifiDialogFragment.newInstance(123);
        dialogFrag.setTargetFragment(this, Constants.REQUEST_DIALOG_FRAGMENT);
        dialogFrag.show(getFragmentManager().beginTransaction(), "wifiDialog");
    }
//    public void showResetFragment(){
//        androidx.fragment.app.DialogFragment dialogFrag = ResetFragment.newInstance(123);
//        dialogFrag.setTargetFragment(this, Constants.REQUEST_DIALOG_FRAGMENT);
//        dialogFrag.show(getFragmentManager().beginTransaction(), "resetDialog");
//    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            Bundle bundle = data.getExtras();
            String type = bundle.getString("type");
            Log.e("ON ACTIVITY RESULT", "Request Code: " + requestCode + " ;; Result Code: " + resultCode + " ;; Intent: " + bundle + " ;; Type: " + bundle.getString("type"));
            switch (type) {
                case "wifi":
                    wifiOn(bundle);
                    break;
                case "hotspot":
                    hotspotOn(bundle);
                    break;
                case "ethernet":
                    ethernetOn(bundle);
                    break;
                case "bridge":
                    bridgeOn(bundle);
                    break;
                default:
                    break;
            }
        }
    }

    private void wifiOn(Bundle bundle) {
        alert = true;
        listener.sendMessage("treehouses wifi \"" + bundle.getString("SSID") + "\" \"" + bundle.getString("PWD") + "\"");

//        WifiManager wifi = (WifiManager) getContext().getApplicationContext().getSystemService(WIFI_SERVICE);
//        WifiConfiguration wc = new WifiConfiguration();
//        wc.SSID = "\""+bundle.getString("SSID")+"\""; //IMP! This should be in Quotes!!
//        wc.hiddenSSID = true;
//        wc.status = WifiConfiguration.Status.DISABLED;
//        wc.priority = 40;
//        wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
//        wc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
//        wc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
//        wc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
//        wc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
//        wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
//        wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
//        wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
//        wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
//
//        wc.wepKeys[0] = "\""+bundle.getString("PWD")+"\""; //This is the WEP Password
//        wc.wepTxKeyIndex = 0;
//
//        boolean res1 = wifi.setWifiEnabled(true);
//        int res = wifi.addNetwork(wc);
//        Log.d("WifiPreference", "add Network returned " + res );
//        boolean es = wifi.saveConfiguration();
//        Log.d("WifiPreference", "saveConfiguration returned " + es );
//        boolean b = wifi.enableNetwork(res, true);
//        Log.d("WifiPreference", "enableNetwork returned " + b );
    }

    private void hotspotOn(Bundle bundle) {
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
            FragmentActivity activity = getActivity();
            switch (msg.what) {
                case Constants.MESSAGE_READ:
                    readMessage = (String) msg.obj;
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
