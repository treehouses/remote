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

public class ViewHolderSSHKey {

    private static TextInputEditText editTextSSHKey;
    private BluetoothChatService mChatService;
    private Context c;

    ViewHolderSSHKey(View v, Context context, HomeInteractListener listener) {
        c = context;
        mChatService = listener.getChatService();
        mChatService.updateHandler(mHandler);
        Button btnStartConfig = v.findViewById(R.id.btn_save_key);
        editTextSSHKey = v.findViewById(R.id.editTextSSHKey);

        btnStartConfig.setOnClickListener(v1 -> {

            if(!editTextSSHKey.getText().toString().equals("")){
                Log.d("1111111", editTextSSHKey.getText().toString());
                listener.sendMessage("treehouses sshkey add" + " \"" +  editTextSSHKey.getText().toString() + "\"");
            }
            else{
                Toast.makeText(c, "Incorrect SSH Key Input", Toast.LENGTH_LONG).show();
            }
//            listener.sendMessage("ssh-keygen -lf /dev/stdin <<< \"fdf\"");
        });

    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == Constants.MESSAGE_READ) {
                String readMessage = (String) msg.obj;
                if (readMessage.contains("Added to 'pi' and 'root' user's authorized_keys")){
                    Toast.makeText(c, "Added to 'pi' and 'root' user's authorized_keys", Toast.LENGTH_LONG).show();
                }


            }
        }
    };
}
