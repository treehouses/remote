package io.treehouses.remote.Fragments;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import io.treehouses.remote.Fragments.DialogFragments.RPIDialogFragment;
import io.treehouses.remote.InitialActivity;
import io.treehouses.remote.Constants;
import io.treehouses.remote.MainApplication;
import io.treehouses.remote.Network.BluetoothChatService;
import io.treehouses.remote.R;
import io.treehouses.remote.bases.BaseFragment;
import io.treehouses.remote.callback.SetDisconnect;
import io.treehouses.remote.utils.LogUtils;
import io.treehouses.remote.utils.Utils;
import io.treehouses.remote.utils.VersionUtils;

import com.parse.ParseObject;

import static io.treehouses.remote.Constants.REQUEST_ENABLE_BT;

public class HomeFragment extends BaseFragment implements SetDisconnect {
    private static final String TAG = "HOME_FRAGMENT";
    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothChatService mChatService = null;
    private Button connectRpi, getStarted, testConnection;
    private Boolean connectionState = false;
    private Boolean result = false;
    private AlertDialog testConnectionDialog;
    private int selected_LED;
    View view;
    SharedPreferences preferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_home_fragment, container, false);
        mChatService = listener.getChatService();
        connectRpi = view.findViewById(R.id.btn_connect);
        getStarted = view.findViewById(R.id.btn_getStarted);
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        testConnection = view.findViewById(R.id.test_connection);
        showDialogOnce();
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
            showLogDialog();
        }
    }

    private void showLogDialog() {
        int connectionCount = preferences.getInt("connection_count", 0);
        boolean showDialog = preferences.getBoolean("show_log_dialog", true);
        LogUtils.log(connectionCount + "  " + showDialog);
        long lastDialogShown = preferences.getLong("last_dialog_shown", 0);
        Calendar date = Calendar.getInstance();
        date.add(Calendar.DAY_OF_YEAR, -7);
        if (lastDialogShown < date.getTimeInMillis()) {
            if (connectionCount >= 3 && showDialog) {
                preferences.edit().putLong("last_dialog_shown", Calendar.getInstance().getTimeInMillis()).commit();
                new AlertDialog.Builder(getActivity()).setTitle("Alert !!!!").setCancelable(false).setMessage("Treehouses wants to collect your activities. " +
                        "Do you like to share it? It will help us to improve.")
                        .setPositiveButton("Yes", (dialogInterface, i) -> {
                            preferences.edit().putBoolean("send_log", true).commit();
                            preferences.edit().putBoolean("show_log_dialog", false).commit();
                        })
                        .setNegativeButton("No", (dialogInterface, i) -> MainApplication.showLogDialog = false).show();
            }
        }
    }

    private void showDialogOnce() {
        boolean dialogShown = preferences.getBoolean("dialogShown", false);

        if (!dialogShown) {
            showWelcomeDialog();
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("dialogShown", true);
            editor.commit();
        }
    }

    private void getStartedListener() { getStarted.setOnClickListener(v -> InitialActivity.getInstance().openCallFragment(new AboutFragment())); }

    public void connectRpiListener() {
        connectRpi.setOnClickListener(v -> {
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
                return;
            } else if (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_ON) {
                showRPIDialog();
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
            showLogDialog();
            sendLog();
            connectRpi.setText("Disconnect");
            connectionState = true;
            testConnection.setVisibility(View.VISIBLE);
        } else {
            connectRpi.setText("Connect to RPI");
            connectionState = false;
            testConnection.setVisibility(View.GONE);
        }
        mChatService.updateHandler(mHandler);
    }

    private void sendLog() {
        int connectionCount = preferences.getInt("connection_count", 0);
        boolean sendLog = preferences.getBoolean("send_log", true);
        preferences.edit().putInt("connection_count", connectionCount + 1).commit();
        if (connectionCount >= 3 && sendLog) {
            ParseObject testObject = new ParseObject("userlog");
            testObject.put("title", mChatService.getConnectedDeviceName() + "");
            testObject.put("description", "Connected to bluetooth");
            testObject.put("type", "BT Connection");
            testObject.put("versionCode", VersionUtils.getVersionCode(getActivity()));
            testObject.put("versionName", VersionUtils.getVersionName(getActivity()));
            testObject.put("deviceName", Build.DEVICE);
            testObject.put("deviceManufacturer", Build.MANUFACTURER);
            testObject.put("deviceModel", Build.MODEL);
            testObject.put("deviceSerialNumber", Build.SERIAL +"");
            testObject.put("macAddress", Utils.getWifiMacAddress(getActivity()));
            testObject.put("gps_latitude", preferences.getString("last_lat",""));
            testObject.put("gps_longitude",  preferences.getString("last_lng",""));
            testObject.saveInBackground();
        }
    }

    private AlertDialog showWelcomeDialog() {
        final SpannableString s = new SpannableString("Treehouses Remote only works with our treehouses images, or a raspbian image enhanced by \"control\" and \"cli\". There is more information under \"Get Started\"" +
                "\n\nhttp://download.treehouses.io\nhttps://github.com/treehouses/control\nhttps://github.com/treehouses/cli");
        Linkify.addLinks(s, Linkify.ALL);
        final AlertDialog d = new AlertDialog.Builder(getContext())
                .setTitle("Friendly Reminder")
                .setIcon(R.drawable.dialog_icon)
                .setNegativeButton("OK", (dialog, which) -> dialog.cancel())
                .setMessage(s)
                .create();
        d.show();
        ((TextView) d.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
        return d;
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
        setAnimatorBackgrounds(mIndicatorGreen, mIndicatorRed);
        AnimationDrawable animationDrawableGreen = (AnimationDrawable) mIndicatorGreen.getBackground();
        AnimationDrawable animationDrawableRed = (AnimationDrawable) mIndicatorRed.getBackground();
        animationDrawableGreen.start();
        animationDrawableRed.start();
        AlertDialog a = createTestConnectionDialog(mView, dismissable, title, messageID);
        a.show();
        return a;
    }
    private void setAnimatorBackgrounds(ImageView green, ImageView red) {
        if (selected_LED == 1) {
            green.setBackgroundResource(R.drawable.thanksgiving_anim_green);
            red.setBackgroundResource(R.drawable.thanksgiving_anim_red);
        }
        else if (selected_LED == 2) {
            green.setBackgroundResource(R.drawable.newyear_anim_green);
            red.setBackgroundResource(R.drawable.newyear_anim_red);
        }
        else {
            green.setBackgroundResource(R.drawable.dance_anim_green);
            red.setBackgroundResource(R.drawable.dance_anim_red);
        }
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

    private void writeToRPI(String ping) {
        byte[] pSend = ping.getBytes();
        mChatService.write(pSend);
    }

    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_WRITE:
                    String writeMessage = new String((byte[]) msg.obj);
                    Log.d(TAG, "WRITTEN: " + writeMessage);
                    break;
                case Constants.MESSAGE_READ:
                    String readMessage = (String) msg.obj;
                    if (!readMessage.isEmpty() && !result) {
                        result = true;
                        dismissTestConnection();
                    }
                    break;
            }
        }
    };
}
