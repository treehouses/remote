package io.treehouses.remote.Fragments;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.parse.ParseObject;

import io.treehouses.remote.Fragments.DialogFragments.RPIDialogFragment;
import io.treehouses.remote.InitialActivity;
import io.treehouses.remote.Constants;
import io.treehouses.remote.Network.BluetoothChatService;
import io.treehouses.remote.R;
import io.treehouses.remote.bases.BaseFragment;
import io.treehouses.remote.callback.SetDisconnect;

import static io.treehouses.remote.Constants.REQUEST_ENABLE_BT;
import static io.treehouses.remote.Constants.TOAST;

public class HomeFragment extends BaseFragment implements SetDisconnect {
    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothChatService mChatService = null;
    private Button connectRpi, getStarted;
    private Boolean connectionState = false;
    View view;
    SharedPreferences preferences;

    public HomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_home_fragment, container, false);
        mChatService = listener.getChatService();
        connectRpi = view.findViewById(R.id.btn_connect);
        getStarted = view.findViewById(R.id.btn_getStarted);
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        showDialogOnce();
        checkConnectionState();
        connectRpiListener();
        getStartedListener();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        showLogDialog();
    }

    private void showLogDialog() {
        int connectionCount = preferences.getInt("connection_count", 0);
        boolean showDialog = preferences.getBoolean("show_log_dialog", true);
        if (connectionCount >= 3 && showDialog) {
            new AlertDialog.Builder(getActivity()).setTitle("Alert !!!!").setCancelable(false).setMessage("Treehouses wants to collect your activities. " +
                    "Do you like to share it? It will help us to improve.").setPositiveButton("Yes", (dialogInterface, i) -> preferences.edit().putBoolean("send_log", true)).setNegativeButton("No", null).show();
            preferences.edit().putBoolean("show_log_dialog", true).commit();
        }
    }

    private void showDialogOnce() {
        SharedPreferences preferences = this.getActivity().getSharedPreferences("pref", Context.MODE_PRIVATE);
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

    public void checkConnectionState() {
        mChatService = listener.getChatService();
        int connectionCount = preferences.getInt("connection_count", 0);
        boolean sendLog = preferences.getBoolean("send_log", true);
        showLogDialog();
        Log.e("", "checkConnectionState: "+connectionCount + " " + sendLog );
        if (mChatService.getState() == Constants.STATE_CONNECTED) {
            Log.e("TREEHOUSES", "checkConnectionState: "+connectionCount + " " + sendLog );
            if (connectionCount >= 3 && sendLog) {
                Log.d("", "checkConnectionState: send log");
                ParseObject testObject = new ParseObject("userlog");
                testObject.put("title", mChatService.getConnectedDeviceName() + "");
                testObject.put("description", "Connected to bluetooth");
                testObject.put("type", "BT Connection");
                testObject.saveInBackground();

            }
            preferences.edit().putInt("connection_count", connectionCount + 1).commit();
            connectRpi.setText("Disconnect");
            connectionState = true;
        } else {
            connectRpi.setText("Connect to RPI");
            connectionState = false;
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

//    private void openURL(String url) {
//        Intent i = new Intent(Intent.ACTION_VIEW);
//        i.setData(Uri.parse(url));
//        startActivity(i);
//    }

    private void showRPIDialog() {
        androidx.fragment.app.DialogFragment dialogFrag = RPIDialogFragment.newInstance(123);
        ((RPIDialogFragment) dialogFrag).setCheckConnectionState(this);
        dialogFrag.setTargetFragment(this, Constants.REQUEST_DIALOG_FRAGMENT_HOTSPOT);
        dialogFrag.show(getFragmentManager().beginTransaction(), "rpiDialog");
    }
}
