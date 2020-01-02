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
import java.util.List;

import io.treehouses.remote.Fragments.DialogFragments.RPIDialogFragment;
import io.treehouses.remote.InitialActivity;
import io.treehouses.remote.Constants;
import io.treehouses.remote.MainApplication;
import io.treehouses.remote.Network.BluetoothChatService;
import io.treehouses.remote.R;
import io.treehouses.remote.bases.BaseHomeFragment;
import io.treehouses.remote.callback.SetDisconnect;
import io.treehouses.remote.utils.VersionUtils;

import com.parse.ParseObject;

import static io.treehouses.remote.Constants.REQUEST_ENABLE_BT;

public class HomeFragment extends BaseHomeFragment implements SetDisconnect {
    private static final String TAG = "HOME_FRAGMENT";
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
        view = inflater.inflate(R.layout.activity_home_fragment, container, false);
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
            showLogDialog(preferences);
            sendLog();
            welcome_text.setVisibility(View.GONE);
            testConnection.setVisibility(View.VISIBLE);
            connectRpi.setText("Disconnect");
            connectRpi.setBackgroundResource(R.drawable.disconnect_rpi);
            background.animate().translationY(150);
            connectRpi.animate().translationY(110);
            getStarted.animate().translationY(70);
            connectionState = true;

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
            testObject.saveInBackground();
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
