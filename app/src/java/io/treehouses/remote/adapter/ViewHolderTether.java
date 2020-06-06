package io.treehouses.remote.adapter;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import io.treehouses.remote.R;
import io.treehouses.remote.callback.HomeInteractListener;

public class ViewHolderTether {

    private WifiManager.LocalOnlyHotspotReservation mReservation;
    private static TextInputEditText editTextSSID;

    ViewHolderTether(View v, HomeInteractListener listener, Context context) {
        ImageView imageViewSettings = v.findViewById(R.id.imageViewSettings);
        Button btnStartConfig = v.findViewById(R.id.btn_start_config);
        editTextSSID = v.findViewById(R.id.editTextSSID);
        TextInputEditText editTextPassword = v.findViewById(R.id.editTextPassword);

        imageViewSettings.setOnClickListener(v1 -> openHotspotSettings(context));

        if (!isApOn(context)) {
            showAlertDialog(context);
        }

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

    private AlertDialog showAlertDialog(Context context) {
        return new AlertDialog.Builder(context)
                .setTitle("OUTPUT:")
                .setMessage("Hotspot is disabled, open hotspot settings?")
                .setIcon(R.drawable.wificon)
                .setPositiveButton(R.string.yes, (dialog, which) -> openHotspotSettings(context))
                .setNegativeButton("NO", (dialog, which) -> dialog.cancel()).show();
    }

    private static boolean isApOn(Context context) {
        WifiManager manager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        int actualState = 0;

        Method method = null;
        try {
            method = manager.getClass().getDeclaredMethod("getWifiApState");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        method.setAccessible(true);
        try {
            actualState = (Integer) method.invoke(manager, (Object[]) null);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return (actualState == 13);
    }
}
