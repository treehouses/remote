package io.treehouses.remote.utils;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.text.Editable;
import android.widget.Button;
import android.widget.EditText;
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

    void ethernetLayout(Editable editable, EditText editText) {
        if (checkCondition(editable)) {
            textChanged(length(editText) && length(etIp) && length(etDNS));
        }
    }

    private boolean checkCondition(Editable editable) {
        return viewCondition(etIp, editable) || viewCondition(etDNS, editable) || viewCondition(etGateway, editable) || viewCondition(etMask, editable);
    }

    void wifiLayout(Editable editable, EditText editText) {
        if (viewCondition(etSsid, editable)) {
            textChanged(length(editText));
        }
    }

    void hotspotLayout(Editable editable, EditText editText) {
        if (viewCondition(etSsid, editable)) {
            textChanged(length(editText));
        }
    }

    void bridgeLayout(Editable editable, EditText editText) {
        if (viewCondition(essid, editable) || viewCondition(etHotspotEssid, editable)) {
            textChanged(length(editText) && length(essid) && length(etHotspotEssid));
        }
    }

    private void textChanged(boolean condition) {
        if (condition) {
            buttonProperties(true, Color.WHITE, btnStartConfiguration);
        } else {
            buttonProperties(false, Color.LTGRAY, btnStartConfiguration);
        }
    }

    private Boolean viewCondition(TextInputEditText editText, Editable editable) {
        return editable == editText.getEditableText() && !messageSent;
    }

    private Boolean length(EditText editText) {
        return editText.length() > 0;
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
