package io.treehouses.remote.adapter;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import io.treehouses.remote.Constants;
import io.treehouses.remote.Fragments.HomeFragment;
import io.treehouses.remote.Fragments.NetworkFragment;
import io.treehouses.remote.Network.BluetoothChatService;
import io.treehouses.remote.R;
import io.treehouses.remote.callback.HomeInteractListener;

public class ViewHolderReboot {

    private static ViewHolderReboot instance = null;
    private Button btnReboot;

    public ViewHolderReboot(final View v, final HomeInteractListener listener, final BluetoothChatService chatService, final Context context) {

        instance = this;
        btnReboot = v.findViewById(R.id.btnReboot);
        btnReboot.setText("Reboot Rpi");
        btnReboot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NetworkFragment.getInstance().showAlertDialog("Are you sure you want to reboot your device?", "Reboot");
            }
        });
    }

    public static ViewHolderReboot getInstance() {
        return instance;
    }

    public void reboot(HomeInteractListener listener, BluetoothChatService chatService, Context context) {
        try {
            Log.d("", "reboot: ");
            listener.sendMessage("reboot");
            Thread.sleep(1000);
            if (chatService.getState() != Constants.STATE_CONNECTED) {
                Toast.makeText(context, "Bluetooth Disconnected: Reboot in progress", Toast.LENGTH_LONG).show();
                listener.openCallFragment(new HomeFragment());
            } else {
                Toast.makeText(context, "Reboot Unsuccessful", Toast.LENGTH_LONG).show();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
