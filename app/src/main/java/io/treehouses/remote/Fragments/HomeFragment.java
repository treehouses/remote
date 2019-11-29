package io.treehouses.remote.Fragments;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
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
import java.util.Calendar;
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
import okio.Utf8;

import com.parse.ParseObject;

import static io.treehouses.remote.Constants.REQUEST_ENABLE_BT;

public class HomeFragment extends BaseFragment implements SetDisconnect {
    private static final String TAG = "HOME_FRAGMENT";
    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothChatService mChatService = null;
    private Button connectRpi, getStarted, testConnection;
    private Boolean connectionState = false;
    private Boolean result = false;
    private String mConnectedDeviceName;
    private AlertDialog testConnectionDialog;
    View view;
    SharedPreferences preferences;

    public HomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_home_fragment, container, false);
        mChatService = listener.getChatService();
        mConnectedDeviceName = mChatService.getConnectedDeviceName();

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
        if (connectionCount >= 3 && showDialog) {
            new AlertDialog.Builder(getActivity()).setTitle("Alert !!!!").setCancelable(false).setMessage("Treehouses wants to collect your activities. " +
                    "Do you like to share it? It will help us to improve.")
                    .setPositiveButton("Yes", (dialogInterface, i) -> {
                        preferences.edit().putBoolean("send_log", true).commit();
                        preferences.edit().putBoolean("show_log_dialog", false).commit();
                    })
                    .setNegativeButton("No", null).show();
            MainApplication.showLogDialog = false;
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

    private void getStartedListener() {
        getStarted.setOnClickListener(v -> InitialActivity.getInstance().openCallFragment(new AboutFragment()));
    }

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
        testConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writeToRPI("treehouses led dance");
                testConnectionDialog = showTestConnectionDialog(false, "Testing Connection...", R.string.test_connection_message);
                testConnectionDialog.show();
            }
        });
    }

    public void checkConnectionState() {
        mChatService = listener.getChatService();
        showLogDialog();
        if (mChatService.getState() == Constants.STATE_CONNECTED) {
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
        Calendar currentDate = Calendar.getInstance();
        currentDate.add(Calendar.DAY_OF_YEAR, -7);

        long lastLogSent = preferences.getLong("last_log_sent", 0);
        if (lastLogSent == 0)
            preferences.edit().putLong("last_log_sent", Calendar.getInstance().getTimeInMillis()).commit();
        LogUtils.log(lastLogSent + " " + currentDate.getTimeInMillis());
        if (connectionCount >= 3 && sendLog) {

            if (lastLogSent < currentDate.getTimeInMillis()) {
                ParseObject testObject = new ParseObject("userlog");
                testObject.put("title", mChatService.getConnectedDeviceName() + "");
                testObject.put("description", "Connected to bluetooth");
                testObject.put("type", "BT Connection");
                testObject.saveInBackground();
                preferences.edit().putLong("last_log_sent", Calendar.getInstance().getTimeInMillis()).commit();

            }
        }
    }

    private AlertDialog showWelcomeDialog() {
        final SpannableString s = new SpannableString("Treehouses Remote only works with our treehouses images, or a raspbian image enhanced by \"control\" and \"cli\". There is more information under \"Get Started\"" +
                "\n\nhttp://download.treehouses.io\nhttps://github.com/treehouses/control\nhttps://github.com/treehouses/cli");
        Linkify.addLinks(s, Linkify.ALL);
        final AlertDialog d = new AlertDialog.Builder(getContext())
                .setTitle("Friendly Reminder")
                .setIcon(R.drawable.dialog_icon)
                .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
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
        LayoutInflater layoutInflater = getLayoutInflater();
        View mView = layoutInflater.inflate(R.layout.dialog_test_connection, null);
        ImageView mIndicatorGreen = mView.findViewById(R.id.flash_indicator_green);
        ImageView mIndicatorRed = mView.findViewById(R.id.flash_indicator_red);
        if (!dismissable) {
            mIndicatorGreen.setVisibility(View.VISIBLE);
            mIndicatorRed.setVisibility(View.VISIBLE);
        } else {
            mIndicatorGreen.setVisibility(View.INVISIBLE);
            mIndicatorRed.setVisibility(View.INVISIBLE);
        }
        mIndicatorGreen.setBackgroundResource(R.drawable.flash_anim_green);
        mIndicatorRed.setBackgroundResource(R.drawable.flash_anim_red);

        AnimationDrawable animationDrawable = (AnimationDrawable) mIndicatorGreen.getBackground();

        animationDrawable.start();

        AlertDialog a = createTestConnectionDialog(mView, dismissable, title, messageID);
        a.show();
        return a;
    }

    private AlertDialog createTestConnectionDialog(View mView, Boolean dismissable, String title, int messageID) {
        AlertDialog.Builder d = new AlertDialog.Builder(getContext())
                .setView(mView)
                .setTitle(title)
                .setIcon(R.drawable.ic_action_device_access_bluetooth_searching)
                .setMessage(messageID);
        if (dismissable) {
            d.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        }
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
                    if (!readMessage.isEmpty()) {
                        result = true;
                        dismissTestConnection();
                    }
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    break;
            }
        }
    };
}
