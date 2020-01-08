package io.treehouses.remote.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

import io.treehouses.remote.R;
import io.treehouses.remote.callback.HomeInteractListener;

public class ViewHolderVnc {

    private static TextInputEditText editTextIp;

    ViewHolderVnc(View v, Context context, HomeInteractListener listener) {
        Button btnStartConfig = v.findViewById(R.id.btn_start_config);
        Switch vnc = v.findViewById(R.id.switchVnc);
        editTextIp = v.findViewById(R.id.editTextIp);

        btnStartConfig.setOnClickListener(v1 -> openVnc(context, v, editTextIp));
        vnc.setOnClickListener(v12 -> {
            if (vnc.isChecked()) {
                listener.sendMessage("treehouses vnc on");
                Toast.makeText(context, "Connecting...", Toast.LENGTH_SHORT).show();
            }
            else {
                listener.sendMessage("treehouses vnc off");
            }
        });
    }

    public static TextInputEditText getEditTextIp() {
        return editTextIp;
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
}
