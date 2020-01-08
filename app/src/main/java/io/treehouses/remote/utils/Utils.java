package io.treehouses.remote.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.provider.Settings.Secure;
import android.widget.Toast;

import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

import androidx.fragment.app.FragmentActivity;

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

    public static String getMacAddr() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;
                return getAddress(nif);
            }
        } catch (Exception ex) {
        }
        return "";
    }

    public static String  getAndroidId(Context context){
         return Secure.getString(context.getContentResolver(),
                Secure.ANDROID_ID);

    }

    private static String getAddress(NetworkInterface nif) throws Exception {
        byte[] macBytes = nif.getHardwareAddress();
        if (macBytes == null) {
            return "";
        }

        StringBuilder res1 = new StringBuilder();
        for (byte b : macBytes) {
            res1.append(String.format("%02X:", b));
        }

        if (res1.length() > 0) {
            res1.deleteCharAt(res1.length() - 1);
        }
        return res1.toString();
    }

    public static void toast(Context context, String s) {
        Toast.makeText(context, s, Toast.LENGTH_LONG).show();
    }
}
