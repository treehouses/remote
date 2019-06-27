package io.treehouses.remote;

import android.content.Context;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import io.treehouses.remote.Fragments.NetworkFragment;

public abstract class ButtonConfiguration {

    protected Button btnWifiSearch;
    protected static TextInputEditText etSsid;

    public abstract void buttonProperties(Boolean clickable, int color);

    protected void buttonWifiSearch(Context context) {
        if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.LOLLIPOP){
            Toast.makeText(context, "Wifi scan not available for this android version", Toast.LENGTH_LONG).show();
        } else {
            btnWifiSearch.setOnClickListener(v1 -> NetworkFragment.getInstance().showWifiDialog(v1));
        }
    }

    public static TextInputEditText getSSID() {
        return etSsid;
    }
}
