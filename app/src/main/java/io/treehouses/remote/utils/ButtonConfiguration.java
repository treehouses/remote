package io.treehouses.remote.utils;

import android.content.Context;
import android.os.Build;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import io.treehouses.remote.Fragments.NetworkFragment;

public abstract class ButtonConfiguration {
    protected static TextInputEditText etHotspotEssid, etSsid, essid;
    protected TextInputEditText etIp, etDNS, etGateway, etMask;
    protected Button btnStartConfiguration;
    protected Button btnWifiSearch;
    protected Boolean messageSent = false;

    public void buttonProperties(Boolean clickable, int color, Button button) {
        NetworkFragment.getInstance().setButtonConfiguration(this);
        button.setEnabled(clickable);
        button.setTextColor(color);
    }

    protected void buttonWifiSearch(Context context) {

        btnWifiSearch.setOnClickListener(v1 -> {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
                Toast.makeText(context, "Wifi scan requires at least android API 23", Toast.LENGTH_LONG).show();
            } else {
                NetworkFragment.getInstance().showWifiDialog(v1);
            }
        });
    }

    public static TextInputEditText getSSID() {
        return etSsid;
    }

    public static TextInputEditText getEssid() {
        return essid;
    }

    public void setMessageSent(Boolean messageSent) {
        this.messageSent = messageSent;
    }

    protected void setBtnStartConfiguration(Button btnStartConfiguration) {
        this.btnStartConfiguration = btnStartConfiguration;
    }

    public static TextInputEditText getEtHotspotEssid() {
        return etHotspotEssid;
    }
}
