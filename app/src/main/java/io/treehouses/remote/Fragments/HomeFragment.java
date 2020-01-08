package io.treehouses.remote.Fragments;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;

import android.util.Log;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
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
import io.treehouses.remote.bases.BaseHomeFragment;
import io.treehouses.remote.callback.SetDisconnect;

import io.treehouses.remote.utils.Utils;
import io.treehouses.remote.utils.VersionUtils;
import io.treehouses.remote.callback.NotificationCallback;
import io.treehouses.remote.callback.SetDisconnect;

import static io.treehouses.remote.Constants.REQUEST_ENABLE_BT;


public class HomeFragment extends BaseHomeFragment implements SetDisconnect {
    private static final String TAG = "HOME_FRAGMENT";

    private NotificationCallback notificationListener;

    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothChatService mChatService = null;
    private Button connectRpi, getStarted, testConnection;
    private Boolean connectionState = false;
    private Boolean result = false;
    private TextView welcome_text;
    private ImageView background;
    private AlertDialog testConnectionDialog;
    private int selected_LED;
    View view;
    private SharedPreferences preferences;

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
        showDialogOnce(preferences);
        checkConnectionState();
        connectRpiListener();
        getStartedListener();
        testConnectionListener();
        return view;
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
            testConnectionDialog = showTestConnectionDialog(false, "Testing Connection...", R.string.test_connection_message);
            testConnectionDialog.show();
            result = false;
        });
    }

    public void checkConnectionState() {
        mChatService = listener.getChatService();
        if (mChatService.getState() == Constants.STATE_CONNECTED) {
            showLogDialog(preferences);
            sendImageInfoCommand();
            welcome_text.setVisibility(View.GONE);
            testConnection.setVisibility(View.VISIBLE);
            connectRpi.setText("Disconnect");
            connectRpi.setBackgroundResource(R.drawable.disconnect_rpi);
            background.animate().translationY(150);
            connectRpi.animate().translationY(110);
            getStarted.animate().translationY(70);
            connectionState = true;

            testConnection.setVisibility(View.VISIBLE);
            writeToRPI("treehouses upgrade --check"); //Check upgrade status

        } else {
            connectRpi.setText("Connect to RPI");
            testConnection.setVisibility(View.GONE);
            welcome_text.setVisibility(View.VISIBLE);
            background.animate().translationY(0);
            connectRpi.animate().translationY(0);
            getStarted.animate().translationY(0);
            connectRpi.setBackgroundResource(R.drawable.connect_to_rpi);
            connectionState = false;
        }
        mChatService.updateHandler(mHandler);
    }

    private void sendImageInfoCommand() {
        listener.sendMessage("treehouses bluetooth mac\n");
        listener.sendMessage("treehouses image\n");
        listener.sendMessage("treehouses version\n");
    }

    private void sendLog() {
        int connectionCount = preferences.getInt("connection_count", 0);
        boolean sendLog = preferences.getBoolean("send_log", true);
        preferences.edit().putInt("connection_count", connectionCount + 1).commit();
        if (connectionCount >= 3 && sendLog) {
            HashMap<String, String> map = new HashMap<>();
            map.put("imageVersion", imageVersion);
            map.put("treehousesVersion", tresshousesVersion);
            map.put("bluetoothMacAddress", bluetoothMac);
            ParseDbService.sendLog(getActivity(), mChatService.getConnectedDeviceName(), map, preferences);
        }
    }

    private void showRPIDialog() {
        androidx.fragment.app.DialogFragment dialogFrag = RPIDialogFragment.newInstance(123);
        ((RPIDialogFragment) dialogFrag).setCheckConnectionState(this);
        dialogFrag.setTargetFragment(this, Constants.REQUEST_DIALOG_FRAGMENT_HOTSPOT);
        dialogFrag.show(getFragmentManager().beginTransaction(), "rpiDialog");
    }

    private AlertDialog showTestConnectionDialog(Boolean dismissable, String title, int messageID) {
        View mView = getLayoutInflater().inflate(R.layout.dialog_test_connection, null);
        ImageView mIndicatorGreen = mView.findViewById(R.id.flash_indicator_green);
        ImageView mIndicatorRed = mView.findViewById(R.id.flash_indicator_red);
        if (!dismissable) {
            mIndicatorGreen.setVisibility(View.VISIBLE);
            mIndicatorRed.setVisibility(View.VISIBLE);
        } else {
            mIndicatorGreen.setVisibility(View.INVISIBLE);
            mIndicatorRed.setVisibility(View.INVISIBLE);
        }
        setAnimatorBackgrounds(mIndicatorGreen, mIndicatorRed, selected_LED);
        AnimationDrawable animationDrawableGreen = (AnimationDrawable) mIndicatorGreen.getBackground();
        AnimationDrawable animationDrawableRed = (AnimationDrawable) mIndicatorRed.getBackground();
        animationDrawableGreen.start();
        animationDrawableRed.start();
        AlertDialog a = createTestConnectionDialog(mView, dismissable, title, messageID);
        a.show();
        return a;
    }

    private AlertDialog createTestConnectionDialog(View mView, Boolean dismissable, String title, int messageID) {
        AlertDialog.Builder d = new AlertDialog.Builder(getContext()).setView(mView).setTitle(title).setIcon(R.drawable.ic_action_device_access_bluetooth_searching).setMessage(messageID);
        if (dismissable) d.setNegativeButton("OK", (dialog, which) -> dialog.dismiss());
        return d.create();
    }

    private void dismissTestConnection() {
        if (testConnectionDialog != null) {
            testConnectionDialog.cancel();
            showTestConnectionDialog(true, "Process Finished", R.string.test_finished);
        }
    }

    private boolean checkUpgrade(String s) {
        if (!s.isEmpty() && s.contains("true") || s.contains("false")) {
            notificationListener.setNotification(s.contains("true"));
            return true;
        }
        return false;
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

    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    String imageVersion = "", tresshousesVersion = "", bluetoothMac = "";
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case Constants.MESSAGE_READ:
                    String readMessage = (String) msg.obj;
                    checkImageInfo(readMessage);
                    Log.d(TAG, "handleMessage: " + readMessage);
                    if (!readMessage.isEmpty() && !checkUpgrade(readMessage) && !result) {
                        result = true;
                        dismissTestConnection();
                    }
                    break;
            }
        }
    };

    private void checkImageInfo(String readMessage) {

        String versionRegex = ".*\\..*\\..*";
        String regexImage = "release.*";
        boolean matchesImagePattern = Pattern.matches(regexImage, readMessage);
        boolean matchesVersion = Pattern.matches(versionRegex, readMessage);
        if (readMessage.contains(":") && readMessage.split(":").length == 6) {
            bluetoothMac = readMessage;
        }
        if (matchesImagePattern)
            imageVersion = readMessage;
        if (matchesVersion) {
            tresshousesVersion = readMessage;
            sendLog();
        }

    }
}
