package io.treehouses.remote.adapter;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.RequiresApi;
import com.google.android.material.textfield.TextInputEditText;
import io.treehouses.remote.R;
import io.treehouses.remote.callback.HomeInteractListener;

public class ViewHolderTether {

    private WifiManager.LocalOnlyHotspotReservation mReservation;
    private static TextInputEditText editTextSSID;

    @RequiresApi(api = Build.VERSION_CODES.O)
    ViewHolderTether(View v, HomeInteractListener listener, Context context) {
        Button btnEnableHotspot = v.findViewById(R.id.btnEnableHotspot);
        Button btnFindSSID = v.findViewById(R.id.btnFindSSID);
        Button btnStartConfig = v.findViewById(R.id.btn_start_config);
        editTextSSID = v.findViewById(R.id.editTextSSID);
        TextInputEditText editTextPassword = v.findViewById(R.id.editTextPassword);

        btnEnableHotspot.setOnClickListener(v12 -> turnOnHotspot(context));

        btnFindSSID.setOnClickListener(v1 -> openHotspotSettings(context));

        btnStartConfig.setOnClickListener(v13 -> {
            String ssid = editTextSSID.getText().toString();
            String password = editTextPassword.getText().toString();

            if (!ssid.isEmpty()) {
                listener.sendMessage("treehouses wifi " + ssid  + " " + (password.isEmpty() ? "" : password));
                Toast.makeText(context, "Connecting...", Toast.LENGTH_LONG).show();

            } else {
                Toast.makeText(context, "Error: Invalid SSID", Toast.LENGTH_LONG).show();
            }
        });
    }

    public static TextInputEditText getEditTextSSID() {
        return editTextSSID;
    }

    private void openHotspotSettings(Context context) {
        final Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        final ComponentName cn = new ComponentName("com.android.settings", "com.android.settings.TetherSettings");
        intent.setComponent(cn);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity( intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void turnOnHotspot(Context context) {
        WifiManager manager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        manager.startLocalOnlyHotspot(new WifiManager.LocalOnlyHotspotCallback() {
            @Override
            public void onStarted(WifiManager.LocalOnlyHotspotReservation reservation) {
                super.onStarted(reservation);
                Log.d("TAG", "Wifi Hotspot is on now");
                mReservation = reservation;
            } @Override
            public void onStopped() {
                super.onStopped();
                Log.d("TAG", "onStopped: ");
                mReservation.close();
            } @Override
            public void onFailed(int reason) {
                super.onFailed(reason);
                Log.d("TAG", "onFailed: ");
            }
        }, new Handler());
    }
}
