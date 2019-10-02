package io.treehouses.remote.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.widget.Toast;

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
}
