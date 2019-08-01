package io.treehouses.remote.Fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.content.ComponentName;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Message;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.RequiresApi;
import java.util.ArrayList;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.snackbar.Snackbar;
import java.util.Collections;
import java.util.List;
import io.treehouses.remote.Constants;
import io.treehouses.remote.Network.BluetoothChatService;
import io.treehouses.remote.R;
import io.treehouses.remote.bases.BaseFragment;
import static android.content.Context.WIFI_SERVICE;

public class SystemFragment extends BaseFragment {

    private BluetoothChatService mChatService = null;
    private WifiManager manager;
    private EditText in;
    private Boolean network = true;
    private Boolean hostname = false;
    View view;

    public SystemFragment() { }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_system_fragment, container, false);
        ArrayList<String> list = new ArrayList<String>();
        list.add("Reboot");
        list.add("Expand File System");
        list.add("Rename Hostname");
        list.add("RPI Password Settings");
        list.add("Container");
        list.add("Upgrade CLI");
        list.add("Open VNC");
        list.add("Open Hotspot Settings");
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

        listView.setOnItemClickListener((parent, view, position, id) -> getListFragment(position));
        mChatService = listener.getChatService();
        mChatService.updateHandler(mHandler);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.e("TAG", "onResume called");
        listener.sendMessage("hostname -I");
        hostname = true;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void getListFragment(int position) {
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
                openVnc();
                break;
            case 7:
                openHotspotSettings();
                break;
            case 8:
                configureHotspot();
                break;
            default:
                Log.e("Default Network Switch", "Nothing...");
                break;
        }
    }

    private void openVnc() {
        in = new EditText(getActivity());
        in.setHint("Enter IP Address of you raspberry PI");
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("vnc://%s:5900", "192.168.1.1")));
        List<ResolveInfo> activities = getActivity().getPackageManager().queryIntentActivities(intent, 0);
        if (activities.size() == 0) {
            Snackbar.make(getView(), "No VNC Client installed on you device", Snackbar.LENGTH_LONG).setAction("Install", view -> {
                Intent intent1 = new Intent(Intent.ACTION_VIEW);
                intent1.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.realvnc.viewer.android"));
                startActivity(intent1);
            }).show();
            return;
        }
        listener.sendMessage("treehouses networkmode info");
        new AlertDialog.Builder(getActivity()).setTitle("Open VNC Client")
        .setView(in)
        .setPositiveButton("Open", (dialogInterface, i) -> {
            String ip = in.getText().toString();
            if (TextUtils.isEmpty(ip)) {
                Toast.makeText(getActivity(), "Invalid ip address", Toast.LENGTH_LONG).show();
                return;
            }
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("vnc://%s:5900", ip))));
            } catch (Exception e) { }
        }).setNegativeButton("Dismiss", null).show();
    }
  
    private void openHotspotSettings() {
        final Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        final ComponentName cn = new ComponentName("com.android.settings", "com.android.settings.TetherSettings");
        intent.setComponent(cn);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity( intent);
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
                mReservation.close();
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

    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == Constants.MESSAGE_READ) {
                String readMessage = (String) msg.obj;
                ArrayList<Long> diff = new ArrayList<>();

                Log.d("TAG", "readMessage = " + readMessage);

                if (readMessage.trim().contains("true") || readMessage.trim().contains("false")) {
                    return;
                }

                checkAndPrefilIp(readMessage, diff);
            }
        }
    };

    private void checkAndPrefilIp(String readMessage, ArrayList<Long> diff) {
        if (readMessage.contains(".") && hostname) {
            checkSubnet(readMessage, diff);
        }

        ipPrefil(readMessage, diff);
    }

    private void ipPrefil(String readMessage, ArrayList<Long> diff) {
        if (readMessage.contains("ip") && !readMessage.contains("ap0")) {
            if (network) {
                prefillIp(readMessage);
            } else {
                Toast.makeText(getContext(), "Warning: Your RPI may be in the wrong subnet", Toast.LENGTH_LONG).show();
                prefillIp(readMessage);
            }
        }
    }

    private void checkSubnet(String readMessage, ArrayList<Long> diff) {
        hostname = false;

        WifiManager wm = (WifiManager) getContext().getApplicationContext().getSystemService(WIFI_SERVICE);
        String deviceIp = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());

        long deviceIpAddress = ipToLong(deviceIp);

        convertIp(readMessage, deviceIpAddress, diff);
        network = isInNetwork(diff);
    }

    private void convertIp(String readMessage, long deviceIpAddress, ArrayList<Long> diff) {
        String[] array = readMessage.split(" ");

        for (String element : array) {
            //TODO: Need to convert IPv6 addresses to long; currently it is being skipped
            if (element.length() <= 15) {
                long ip = ipToLong(element);
                diff.add(deviceIpAddress - ip);
            }
        }
    }

    private long ipToLong(String ipAddress) {
        String[] ipAddressInArray = ipAddress.split("[.]");
        long result = 0;

        for (int i = 0; i < ipAddressInArray.length; i++) {
            int power = 3 - i;
            int ip = Integer.parseInt(ipAddressInArray[i]);
            result += ip * Math.pow(256, power);
        }
        return result;
    }

    private boolean isInNetwork(ArrayList<Long> diff) {
        Collections.sort(diff);
        return diff.get(0) <= 256;
    }

    private void prefillIp(String readMessage) {
        String[] array = readMessage.split(",");

        for (String element : array) {
            elementConditions(element);
        }
    }

    private void elementConditions(String element) {
        if (element.contains("ip")) {
            try {
                in.setText(element.trim().substring(4));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
