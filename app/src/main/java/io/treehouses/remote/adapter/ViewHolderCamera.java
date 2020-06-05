package io.treehouses.remote.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

import io.treehouses.remote.Constants;
import io.treehouses.remote.Network.BluetoothChatService;
import io.treehouses.remote.R;
import io.treehouses.remote.callback.HomeInteractListener;

public class ViewHolderCamera {

    private static TextInputEditText editTextSSHKey;
    private BluetoothChatService mChatService;
    private Context c;
    private Switch cameraSwitch;

    ViewHolderCamera(View v, Context context, HomeInteractListener listener) {
        c = context;
        mChatService = listener.getChatService();
        mChatService.updateHandler(mHandler);
        cameraSwitch = v.findViewById(R.id.CameraSwitch);

        listener.sendMessage("treehouses camera");
        cameraSwitch.setEnabled(false);

        cameraSwitch.setOnClickListener(v2 -> {
            if (cameraSwitch.isChecked()) {
                listener.sendMessage("treehouses camera on");
                cameraSwitch.setEnabled(false);
            } else {
                listener.sendMessage("treehouses camera off");
                cameraSwitch.setEnabled(false);
            }
        });


    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == Constants.MESSAGE_READ) {
                readCameraReply(msg.obj.toString());
            }
        }
    };
    private void readCameraReply(String readMessage){
        if (readMessage.contains("Camera settings which are currently enabled") || readMessage.contains("have been enabled")){
            Toast.makeText(c, "Camera is enabled", Toast.LENGTH_LONG).show();
            cameraSwitch.setChecked(true);
            cameraSwitch.setEnabled(true);
        }
        else if(readMessage.contains("currently disabled") || readMessage.contains("has been disabled")){
            Toast.makeText(c, "Camera is disabled", Toast.LENGTH_LONG).show();
            cameraSwitch.setEnabled(true);
            cameraSwitch.setChecked(false);
        }
    }
}
