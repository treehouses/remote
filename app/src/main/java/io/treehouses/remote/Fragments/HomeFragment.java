package io.treehouses.remote.Fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

import io.treehouses.remote.BuildConfig;
import io.treehouses.remote.Constants;
import io.treehouses.remote.Fragments.DialogFragments.RPIDialogFragment;
import io.treehouses.remote.InitialActivity;
import io.treehouses.remote.MainApplication;
import io.treehouses.remote.Network.BluetoothChatService;
import io.treehouses.remote.R;
import io.treehouses.remote.adapter.ProfilesListAdapter;
import io.treehouses.remote.bases.BaseHomeFragment;
import io.treehouses.remote.callback.SetDisconnect;

import io.treehouses.remote.callback.NotificationCallback;
import io.treehouses.remote.pojo.NetworkProfile;
import io.treehouses.remote.utils.SaveUtils;

import static io.treehouses.remote.Constants.REQUEST_ENABLE_BT;


public class HomeFragment extends BaseHomeFragment implements SetDisconnect {
    public static final String[] group_labels = {"WiFi", "Hotspot", "Bridge", "Default"};

    private NotificationCallback notificationListener;

    private ProgressDialog progressDialog;

    private ExpandableListView network_profiles;

    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothChatService mChatService = null;
    private Button connectRpi, getStarted, testConnection;
    private Boolean connectionState = false;
    private Boolean result = false;
    private TextView welcome_text;
    private ImageView background, logo, internetstatus;
    private AlertDialog testConnectionDialog;
    private int selected_LED;
    private boolean checkVersionSent = false;

    private String network_ssid = "";
    private FrameLayout layout;

    private NetworkProfile networkProfile;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_home_fragment, container, false);
        mChatService = listener.getChatService();
        connectRpi = view.findViewById(R.id.btn_connect);
        getStarted = view.findViewById(R.id.btn_getStarted);
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        testConnection = view.findViewById(R.id.test_connection);
        welcome_text = view.findViewById(R.id.welcome_home);
        background = view.findViewById(R.id.background_home);
        network_profiles = view.findViewById(R.id.network_profiles);
        logo = view.findViewById(R.id.logo_home);
        layout = view.findViewById(R.id.layout_back);
        internetstatus = view.findViewById(R.id.internetstatus);
        setupProfiles();
        showDialogOnce(preferences);
        checkConnectionState();
        connectRpiListener();
        getStarted.setOnClickListener(v -> InitialActivity.getInstance().openCallFragment(new AboutFragment()));
        testConnectionListener();

        try {
            String s = "{\"available\":[\"couchdb\",\"kolibri\",\"mariadb\",\"mastodon\"],\"installed\":[\"couchdb\",\"kolibri\",\"mastodon\"],\"running\":[\"couchdb\",\"mastodon\"],\"icon\":{\"couchdb\":\"<svg>\",\"kolibri\":\"<svg>\",\"mariadb\":\"<svg>\",\"mastodon\":\"<svg>\"},\"info\":{\"couchdb\":\"<info>\",\"kolibri\":\"<info>\",\"mariadb\":\"<info>\",\"mastodon\":\"<info>\"},\"autorun\":{\"couchdb\":\"true\",\"kolibri\":\"true\",\"mariadb\":\"true\",\"mastodon\":\"true\"}}";

            JSONObject jsonObject = new JSONObject(s);

            Log.d("HOME", jsonObject.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return view;
    }

    private void setupProfiles() {

        ProfilesListAdapter profileAdapter = new ProfilesListAdapter(getContext(),
                Arrays.asList(group_labels), SaveUtils.getProfiles(getContext()));

        network_profiles.setAdapter(profileAdapter);
        network_profiles.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                if (groupPosition == 3) {
                    listener.sendMessage("treehouses default network");
                    Toast.makeText(getContext(),"Switched to Default Network", Toast.LENGTH_LONG).show();
                }
                else if (SaveUtils.getProfiles(getContext()).size() > 0 && SaveUtils.getProfiles(getContext()).get(Arrays.asList(group_labels).get(groupPosition)).size() > 0) {
                    networkProfile = SaveUtils.getProfiles(getContext()).get(Arrays.asList(group_labels).get(groupPosition)).get(childPosition);
                    listener.sendMessage("treehouses default network \n");
                    Toast.makeText(getContext(), "Configuring...", Toast.LENGTH_LONG).show();
                }
                return false;
            }
        });
    }

    private void switchProfile(NetworkProfile networkProfile) {
        if (networkProfile == null) return;
        progressDialog = ProgressDialog.show(getContext(), "Connecting...", "Switching to " + networkProfile.ssid, true);
        progressDialog.show();

        if (networkProfile.isWifi()) {
            //WIFI
            listener.sendMessage(String.format("treehouses wifi %s %s", networkProfile.ssid, networkProfile.password));
            network_ssid = networkProfile.ssid;
        } else if (networkProfile.isHotspot()) {
            //Hotspot
            if (networkProfile.password.isEmpty()) {
                listener.sendMessage("treehouses ap " + networkProfile.option + " " + networkProfile.ssid);
            } else {
                listener.sendMessage("treehouses ap " + networkProfile.option + " " + networkProfile.ssid + " " + networkProfile.password);
            }
            network_ssid = networkProfile.ssid;
        } else if (networkProfile.isBridge()) {
            //Bridge
            String temp = "treehouses bridge " + networkProfile.ssid + " " + networkProfile.hotspot_ssid + " ";
            String overallMessage = TextUtils.isEmpty(networkProfile.password) ? temp + " " : temp + " " + networkProfile.password;

            if (!TextUtils.isEmpty(networkProfile.hotspot_password)) overallMessage += " " + networkProfile.hotspot_password + " ";
            listener.sendMessage(overallMessage);
        } else {
            Log.e("Home", "UNKNOWN TYPE");
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (MainApplication.showLogDialog) {
            rate(preferences);
            showLogDialog(preferences);
        }
    }


    public void connectRpiListener() {
        connectRpi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (connectionState) {
                    RPIDialogFragment.getInstance().bluetoothCheck("unregister");
                    mChatService.stop();
                    connectionState = false;
                    checkConnectionState();
                    return;
                }

                if (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_OFF) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                    Toast.makeText(getContext(), "Bluetooth is disabled", Toast.LENGTH_LONG).show();
                } else if (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_ON) {
                    showRPIDialog(HomeFragment.this);
                }
            }
        });
    }

    public void testConnectionListener() {
        testConnection.setOnClickListener(v -> {
            String preference = androidx.preference.PreferenceManager.getDefaultSharedPreferences(getContext()).getString("led_pattern", "LED Dance");
            List<String> options = Arrays.asList(getResources().getStringArray(R.array.led_options));
            String[] options_code = getResources().getStringArray(R.array.led_options_commands);
            selected_LED = options.indexOf(preference);
            writeToRPI(options_code[selected_LED]);
            testConnectionDialog = showTestConnectionDialog(false, "Testing Connection...", R.string.test_connection_message, selected_LED);
            testConnectionDialog.show();
            result = false;
        });
    }

    public void checkConnectionState() {
        mChatService = listener.getChatService();
        if (mChatService.getState() == Constants.STATE_CONNECTED) {
            showLogDialog(preferences);
            transitionOnConnected();
            connectionState = true;
            checkVersionSent = true;
            writeToRPI("treehouses remote version " + BuildConfig.VERSION_CODE + "\n");

        } else {
            transitionDisconnected();
            connectionState = false;
        }
        mChatService.updateHandler(mHandler);
    }

    private void transitionOnConnected() {
        welcome_text.setVisibility(View.GONE);
        testConnection.setVisibility(View.VISIBLE);
        connectRpi.setText("Disconnect");
        connectRpi.setBackgroundResource(R.drawable.disconnect_rpi);
        background.animate().translationY(150);
        connectRpi.animate().translationY(110);
        getStarted.animate().translationY(70);
        testConnection.setVisibility(View.VISIBLE);
        layout.setVisibility(View.VISIBLE);
        logo.setVisibility(View.GONE);
    }

    private void transitionDisconnected() {
        connectRpi.setText("Connect to RPI");
        testConnection.setVisibility(View.GONE);
        welcome_text.setVisibility(View.VISIBLE);
        background.animate().translationY(0);
        connectRpi.animate().translationY(0);
        getStarted.animate().translationY(0);
        connectRpi.setBackgroundResource(R.drawable.connect_to_rpi);
        logo.setVisibility(View.VISIBLE);
        layout.setVisibility(View.GONE);
    }

    private void dismissTestConnection() {
        if (testConnectionDialog != null) {
            testConnectionDialog.cancel();
            showTestConnectionDialog(true, "Process Finished", R.string.test_finished, selected_LED);
        }
    }

    private void writeToRPI(String ping) {
        mChatService.write(ping.getBytes());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            notificationListener = (NotificationCallback) getContext();
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NotificationListener");
        }
    }

    private void dismissPDialog() { if (progressDialog != null) progressDialog.dismiss(); }

    private void checkVersion(String output) {
        checkVersionSent = false;
        if (output.contains("Usage") || output.contains("command")) {
            //CLI Needs Upgrade
            showUpgradeCLI();
        }
        else if(BuildConfig.VERSION_CODE == 2 || output.contains("true")) {
            writeToRPI("treehouses remote status\n");
            writeToRPI("treehouses upgrade --check\n");
        }
        else {
            AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                    .setTitle("Update Required")
                    .setMessage("Please update Treehouses Remote, as it does not meet the required version on the Treehouses CLI.")
                    .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final String appPackageName = getActivity().getPackageName(); // getPackageName() from Context or Activity object
                            try {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                            } catch (android.content.ActivityNotFoundException anfe) {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                            }
                        }
                    }).create();
            alertDialog.show();
        }
    }

    private void readMessage(String output) {
        if (checkVersionSent) {
            checkVersion(output);
        } else if (output.contains(" ") && output.split(" ").length == 5) {
            checkImageInfo(output.split(" "), mChatService.getConnectedDeviceName(), internetstatus);
        } else if (matchResult(output, "true", "false") && output.length() < 14) {
            notificationListener.setNotification(output.contains("true"));
        } else if (matchResult(output, "connected", "pirateship")) {
            Toast.makeText(getContext(), "Switched to " + network_ssid, Toast.LENGTH_LONG).show();
            dismissPDialog();
        } else if (output.toLowerCase().contains("bridge has been built")) {
            dismissPDialog();
            Toast.makeText(getContext(), "Bridge Has Been Built", Toast.LENGTH_LONG).show();
        } else if (output.toLowerCase().contains("default")) {
            switchProfile(networkProfile);
        } else if (output.toLowerCase().contains("error")) {
            dismissPDialog();
            Toast.makeText(getContext(), "Network Not Found", Toast.LENGTH_LONG).show();
        } else if (!result) {
            //Test Connection
            result = true;
            dismissTestConnection();
        }
        try { notificationListener = (NotificationCallback) getContext(); }
        catch (ClassCastException e) { throw new ClassCastException("Activity must implement NotificationListener"); }
    }

    /**
     * The Handler that gets information back from the BluetoothChatService
     */

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case Constants.MESSAGE_READ:
                    String output = (String) msg.obj;
                    if (!output.isEmpty()) {
                        readMessage(output);
                    }
                    break;
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        if (mChatService.getState() == Constants.STATE_CONNECTED) {
            checkVersionSent = true;
            writeToRPI("treehouses remote version " + BuildConfig.VERSION_CODE + "\n");
        }
    }
}
