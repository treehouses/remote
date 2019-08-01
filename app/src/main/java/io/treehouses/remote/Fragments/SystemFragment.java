package io.treehouses.remote.Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Objects;

import io.treehouses.remote.Constants;
import io.treehouses.remote.Network.BluetoothChatService;
import io.treehouses.remote.R;
import io.treehouses.remote.bases.BaseFragment;

import static android.content.Context.WIFI_SERVICE;

public class SystemFragment extends BaseFragment {

    private BluetoothChatService mChatService = null;
    private WifiManager manager;
    View view;


    public SystemFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_system_fragment, container, false);


        ArrayList<String> list = new ArrayList<String>();
        list.add("Reboot");
        list.add("Expand File System");
        list.add("Rename Hostname");
        list.add("RPI Password Settings");
        list.add("Container");
        list.add("Upgrade CLI");
        list.add("Tether");





        ListView listView = view.findViewById(R.id.listView);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, list);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                getListFragment(position);
            }
        });

        mChatService = listener.getChatService();

        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void getListFragment(int position) {
        switch (position) {
            case 0:
                listener.sendMessage("reboot");
                try {
                    Thread.sleep(1000);

                    if (mChatService.getState() != Constants.STATE_CONNECTED) {
                        Toast.makeText(getContext(), "Bluetooth Disconnected: Reboot in progress", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getContext(), "Reboot Unsuccessful", Toast.LENGTH_LONG).show();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                break;
            case 1:
                listener.sendMessage("treehouses expandfs");
                break;
            case 2:
                showRenameDialog();
                break;
            case 3:
                showChPasswordDialog();
                break;
            case 4:
                showContainerDialog();
                break;
            case 5:
                listener.sendMessage("treehouses upgrade");
                break;
            case 6:
                configureHotspot();
            default:
                Log.e("Default Network Switch", "Nothing...");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void configureHotspot() {
        turnOnHotspot();
    }

    private WifiManager.LocalOnlyHotspotReservation mReservation;

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void turnOnHotspot() {
        manager = (WifiManager) getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        manager.startLocalOnlyHotspot(new WifiManager.LocalOnlyHotspotCallback() {

            @Override
            public void onStarted(WifiManager.LocalOnlyHotspotReservation reservation) {
                super.onStarted(reservation);
                Log.d("TAG", "Wifi Hotspot is on now");
                mReservation = reservation;
            }

            @Override
            public void onStopped() {
                super.onStopped();
                Log.d("TAG", "onStopped: ");
            }

            @Override
            public void onFailed(int reason) {
                super.onFailed(reason);
                Log.d("TAG", "onFailed: ");
            }
        }, new Handler());
    }

    private void showRenameDialog() {
        androidx.fragment.app.DialogFragment dialogFrag = RenameDialogFragment.newInstance(123);
        dialogFrag.setTargetFragment(this, Constants.REQUEST_DIALOG_FRAGMENT_HOTSPOT);
        dialogFrag.show(getFragmentManager().beginTransaction(), "renameDialog");
    }

    private void showContainerDialog() {
        androidx.fragment.app.DialogFragment dialogFrag = ContainerDialogFragment.newInstance(123);
        dialogFrag.setTargetFragment(this, Constants.REQUEST_DIALOG_FRAGMENT_HOTSPOT);
        dialogFrag.show(getFragmentManager().beginTransaction(), "ethernetDialog");
    }

    private void showChPasswordDialog() {
        // Create an instance of the dialog fragment and show it
        androidx.fragment.app.DialogFragment dialogFrag = ChPasswordDialogFragment.newInstance(123);
        dialogFrag.setTargetFragment(this, Constants.REQUEST_DIALOG_FRAGMENT_CHPASS);
        dialogFrag.show(getFragmentManager().beginTransaction(), "ChangePassDialog");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            Bundle bundle = data.getExtras();
            String type = bundle.getString("type");
            switch (type) {
                case "rename":
                    listener.sendMessage("treehouses rename \"" + bundle.getString("hostname") + "\"");
                    break;
                case "container":
                    listener.sendMessage("treehouses container \"" + bundle.getString("container") + "\"");
                    break;
                case "chPass":
                    listener.sendMessage("treehouses password \"" + bundle.getString("password") + "\"");
                    break;
            }
        }
    }
}
