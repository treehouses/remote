package io.treehouses.remote;
import android.graphics.Color;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.content.Context;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputEditText;

import org.w3c.dom.Text;

import io.treehouses.remote.Fragments.NetworkFragment;
import io.treehouses.remote.Fragments.NetworkFragment;

public abstract class ButtonConfiguration {
    protected TextInputEditText etHotspotEssid, etPassword, etHotspotPassword;
    protected Button btnStartConfiguration;
    protected Button btnWifiSearch;
    protected static TextInputEditText etSsid, essid;

    public void buttonProperties(Boolean clickable, int color, Button btnStartConfiguration) {
        NetworkFragment.getInstance().setButtonConfiguration(this);
        btnStartConfiguration.setEnabled(clickable);
        btnStartConfiguration.setTextColor(color);
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

    protected TextWatcher getTextWatcher(final EditText editText, View v) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void afterTextChanged(Editable editable) {
                if (editable == etSsid.getEditableText()) {
                    wifiTextChanged(editText, v);
                } else if (editable == essid.getEditableText()) {
                    bridgeTextChanged(editText, v);
                }

                Log.e("TAG", "afterTextChanged()");
            }
        };
    }

    private void wifiTextChanged(EditText editText, View v) {
        if (editText.length() > 0) {
            buttonProperties(true, Color.WHITE, getBtnStartConfiguration());
        } else {
            buttonProperties(false, Color.LTGRAY, getBtnStartConfiguration());
        }
    }

    private void bridgeTextChanged(EditText editText, View v) {
        if (editText.length() > 0 && etHotspotEssid.length() > 0 )  {
            buttonProperties(true, Color.WHITE, getBtnStartConfiguration());
        } else {
            buttonProperties(false, Color.LTGRAY, getBtnStartConfiguration());
        }
    }


    public static TextInputEditText getSSID() {
        return etSsid;
    }

    public static TextInputEditText getEssid() {
        return essid;
    }

    private Button getBtnStartConfiguration() {
        return btnStartConfiguration;
    }
}
