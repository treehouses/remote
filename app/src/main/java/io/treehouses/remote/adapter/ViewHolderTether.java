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
import android.widget.Switch;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputEditText;
import io.treehouses.remote.R;
import io.treehouses.remote.callback.HomeInteractListener;

public class ViewHolderTether {

    private WifiManager.LocalOnlyHotspotReservation mReservation;
    private static TextInputEditText editTextSSID;

    ViewHolderTether(View v, HomeInteractListener listener, Context context) {
        Switch switchEnableHotspot = v.findViewById(R.id.switchEnableHotspot);
        Button btnFindSSID = v.findViewById(R.id.btnFindSSID);
        Button btnStartConfig = v.findViewById(R.id.btn_start_config);
        editTextSSID = v.findViewById(R.id.editTextSSID);
        TextInputEditText editTextPassword = v.findViewById(R.id.editTextPassword);

        switchEnableHotspot.setOnClickListener(v12 -> {
            if (switchEnableHotspot.isChecked()) {
                turnOnHotspot(context);
            } else {
                turnOffHotspot();
            }
        });

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

    private void turnOffHotspot() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (mReservation != null) {
                mReservation.close();
            }
        }
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

    private void turnOnHotspot(Context context) {
        WifiManager manager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                manager.startLocalOnlyHotspot(new WifiManager.LocalOnlyHotspotCallback() {
                    @Override
                    public void onStarted(WifiManager.LocalOnlyHotspotReservation reservation) {
                        super.onStarted(reservation);
                        mReservation = reservation;
                    }

                    @Override
                    public void onStopped() {
                        super.onStopped();
                        mReservation.close();
                    }

                    @Override
                    public void onFailed(int reason) {
                        super.onFailed(reason);
                    }
                }, new Handler());
            }catch (Exception e){
                Toast.makeText(context,"Something went wrong, Unable to start hotspot", Toast.LENGTH_LONG).show();
            }
        }


    }
}
