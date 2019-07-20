package io.treehouses.remote.Fragments;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import io.treehouses.remote.Constants;
import io.treehouses.remote.Network.BluetoothChatService;
import io.treehouses.remote.R;
import io.treehouses.remote.bases.BaseFragment;
import io.treehouses.remote.utils.Utils;

public class SystemFragment extends BaseFragment {

    View view;
    private BluetoothChatService mChatService = null;


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
        list.add("Open VNC");
        ListView listView = view.findViewById(R.id.listView);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> getListFragment(position));
        mChatService = listener.getChatService();
        return view;
    }

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
                openVnc();
            default:
                Log.e("Default Network Switch", "Nothing...");
        }
    }

    private void openVnc() {
        EditText in = new EditText(getActivity());
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
                    } catch (Exception e) {
                    }
                }).setNegativeButton("Dismiss", null).show();
    }

    public void showRenameDialog() {
        androidx.fragment.app.DialogFragment dialogFrag = RenameDialogFragment.newInstance(123);
        dialogFrag.setTargetFragment(this, Constants.REQUEST_DIALOG_FRAGMENT_HOTSPOT);
        dialogFrag.show(getFragmentManager().beginTransaction(), "renameDialog");
    }

    public void showContainerDialog() {
        androidx.fragment.app.DialogFragment dialogFrag = ContainerDialogFragment.newInstance(123);
        dialogFrag.setTargetFragment(this, Constants.REQUEST_DIALOG_FRAGMENT_HOTSPOT);
        dialogFrag.show(getFragmentManager().beginTransaction(), "ethernetDialog");
    }

    public void showChPasswordDialog() {
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
            if (type.equals("rename")) {
                listener.sendMessage("treehouses rename \"" + bundle.getString("hostname") + "\"");
            } else if (type.equals("container")) {
                listener.sendMessage("treehouses container \"" + bundle.getString("container") + "\"");
            } else if (type.equals("chPass")) {
                listener.sendMessage("treehouses password \"" + bundle.getString("password") + "\"");
            }
        }
    }
}
