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
                Toast.makeText(c, "Incorrect ssh key input", Toast.LENGTH_LONG).show();
            }
//            listener.sendMessage("ssh-keygen -lf /dev/stdin <<< \"fdf\"");

//            listener.sendMessage("")
        });

    }



    private void openVnc(Context context, View v, TextInputEditText in) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("vnc://%s:5900", "192.168.1.1")));
        List<ResolveInfo> activities = context.getPackageManager().queryIntentActivities(intent, 0);
        if (activities.size() == 0) {
            Snackbar.make(v, "No VNC Client installed on you device", Snackbar.LENGTH_LONG).setAction("Install", view -> {
                Intent intent1 = new Intent(Intent.ACTION_VIEW);
                intent1.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.realvnc.viewer.android"));
                context.startActivity(intent1);
            }).show();
            return;
        }
        String ip = in.getText().toString();
        if (TextUtils.isEmpty(ip)) {
            Toast.makeText(context, "Invalid ip address", Toast.LENGTH_LONG).show();
            return;
        }
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("vnc://%s:5900", ip))));
        } catch (Exception e) { }
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
