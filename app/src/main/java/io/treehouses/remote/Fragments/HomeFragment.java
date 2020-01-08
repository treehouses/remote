package io.treehouses.remote.Fragments;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import io.treehouses.remote.Constants;
import io.treehouses.remote.Fragments.DialogFragments.RPIDialogFragment;
import io.treehouses.remote.InitialActivity;
import io.treehouses.remote.MainApplication;
import io.treehouses.remote.Network.BluetoothChatService;
import io.treehouses.remote.Network.ParseDbService;
import io.treehouses.remote.R;
import io.treehouses.remote.adapter.ProfilesListAdapter;
import io.treehouses.remote.bases.BaseHomeFragment;
import io.treehouses.remote.callback.SetDisconnect;

import io.treehouses.remote.callback.NotificationCallback;
import io.treehouses.remote.pojo.NetworkProfile;
import io.treehouses.remote.utils.SaveUtils;

import static io.treehouses.remote.Constants.REQUEST_ENABLE_BT;


public class HomeFragment extends BaseHomeFragment implements SetDisconnect {
    private static final String TAG = "HOME_FRAGMENT";
    private static final String[] group_labels = {"WIFI", "Hotspot"};

    private NotificationCallback notificationListener;

    private ProgressBar progressBar;

    private ExpandableListView network_profiles;

    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothChatService mChatService = null;
    private Button connectRpi, getStarted, testConnection;
    private Boolean connectionState = false;
    private Boolean result = false;
    private TextView welcome_text;
    private ImageView background, logo;
    private AlertDialog testConnectionDialog;
    private int selected_LED;
    View view;

    private String network_ssid = "";
    private FrameLayout layout;

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
        progressBar = view.findViewById(R.id.progress_home);
        logo = view.findViewById(R.id.logo_home);
        layout = view.findViewById(R.id.layout_back);
        setupProfiles();
        showDialogOnce(preferences);
        checkConnectionState();
        connectRpiListener();
        getStartedListener();
        testConnectionListener();
        return view;
    }

    private void setupProfiles() {

        ProfilesListAdapter profileAdapter = new ProfilesListAdapter(getContext(),
                Arrays.asList(group_labels), SaveUtils.getProfiles(getContext()));

        network_profiles.setAdapter(profileAdapter);
        network_profiles.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                NetworkProfile networkProfile = SaveUtils.getProfiles(getContext()).get(Arrays.asList(group_labels).get(groupPosition)).get(childPosition);
                switchProfile(networkProfile);
                return false;
            }
        });
    }

    private void switchProfile(NetworkProfile networkProfile) {
        progressBar.setVisibility(View.VISIBLE);
        Toast.makeText(getContext(), "Connecting...", Toast.LENGTH_LONG).show();

        if (networkProfile.option.equals(SaveUtils.NONE)) {
            //WIFI
            listener.sendMessage(String.format("treehouses wifi \"%s\" \"%s\"", networkProfile.ssid, networkProfile.password));
            network_ssid = networkProfile.ssid;
        } else {
            //Hotspot
            if (networkProfile.password.equals(SaveUtils.NONE)) {
                listener.sendMessage("treehouses ap \"" + networkProfile.option + "\" \"" + networkProfile.ssid + "\"");
            } else {
                listener.sendMessage("treehouses ap \"" + networkProfile.option + "\" \"" + networkProfile.ssid + "\" \"" + networkProfile.password + "\"");
            }
            network_ssid = networkProfile.ssid;
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (MainApplication.showLogDialog) {
            showLogDialog(preferences);
        }
    }

    private void getStartedListener() {
        getStarted.setOnClickListener(v -> InitialActivity.getInstance().openCallFragment(new AboutFragment()));
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
                    showRPIDialog();
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

            welcome_text.setVisibility(View.GONE);
            testConnection.setVisibility(View.VISIBLE);
            connectRpi.setText("Disconnect");
            connectRpi.setBackgroundResource(R.drawable.disconnect_rpi);
            background.animate().translationY(150);
            connectRpi.animate().translationY(110);
            getStarted.animate().translationY(70);
            connectionState = true;
            writeToRPI("treehouses upgrade --check\n"); //Check upgrade status
            sendImageInfoCommand();

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

    private void sendImageInfoCommand() {
//        new Thread(() -> {
//            try {
               // Thread.sleep(200);
                listener.sendMessage("treehouses bluetooth mac\n");
               // Thread.sleep(200);
                listener.sendMessage("treehouses image\n");
               // Thread.sleep(200);
                listener.sendMessage("treehouses version\n");
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }).start();

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

    private void showRPIDialog() {
        androidx.fragment.app.DialogFragment dialogFrag = RPIDialogFragment.newInstance(123);
        ((RPIDialogFragment) dialogFrag).setCheckConnectionState(this);
        dialogFrag.setTargetFragment(this, Constants.REQUEST_DIALOG_FRAGMENT_HOTSPOT);
        dialogFrag.show(getFragmentManager().beginTransaction(), "rpiDialog");
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

    private boolean matchResult(String output, String option1, String option2) {
        return output.contains(option1) || output.contains(option2);
    }

    private void readMessage(String output) {
        if (output.contains(" ") && output.split(" ").length == 2) {
            String[] result = output.split(" ");
            for (String r : result)
                checkImageInfo(r,  mChatService.getConnectedDeviceName());
        } else {
            checkImageInfo(output,  mChatService.getConnectedDeviceName());
        }

        if (matchResult(output, "true", "false")) {
            notificationListener.setNotification(output.contains("true"));
        } else if (matchResult(output, "network", "successfully")) {
            Toast.makeText(getContext(), "Switched to " + network_ssid, Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.GONE);
        } else if (output.toLowerCase().contains("error")) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(getContext(), "Network Not Found", Toast.LENGTH_LONG).show();
        } else if (!result) {
            result = true;
            dismissTestConnection();
        }
        try {
            notificationListener = (NotificationCallback) getContext();
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NotificationListener");
        }
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
            }
        }
    };

}
