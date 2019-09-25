package io.treehouses.remote.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.UUID;

import io.treehouses.remote.MainApplication;

public class Utils {
    public static void copyToClipboard(Context context, String clickedData) {
        if (clickedData.contains("Command: ") || clickedData.contains(" Command:") || clickedData.contains("Command:")) {
            clickedData = clickedData.substring(10, clickedData.length());
        }

        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("label", clickedData);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(context, "Copied to clipboard: " + clickedData, Toast.LENGTH_LONG).show();
    }

    public static void logEvent(String name, String description) {
        Bundle params = new Bundle();
        params.putString("device_name", name);
        params.putString("description", description);
        MainApplication.mFirebaseAnalytics.logEvent("bluetooth_connected", params);
        Log.d("TIO", "logEvent: ");
    }
}
