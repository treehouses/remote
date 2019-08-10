package io.treehouses.remote;
import android.graphics.Color;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.content.Context;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;
import com.google.android.material.textfield.TextInputEditText;
import io.treehouses.remote.Fragments.NetworkFragment;

public abstract class ButtonConfiguration {
    protected static TextInputEditText etHotspotEssid, etSsid, essid;
    protected TextInputEditText etIp, etDNS, etGateway, etMask;
    protected Button btnStartConfiguration;
    protected Button btnWifiSearch;
    protected Boolean messageSent = false;

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
                //if (viewCondition(etIp, editable) || viewCondition(etDNS, editable)) {                  // ethernet
               //     textChanged(length(editText) && length(etIp) && length(etDNS));
                //} else
                //
                if (viewCondition(etSsid, editable)) {                                             // wifi
                    textChanged(length(editText));
                } else if (viewCondition(essid, editable) || viewCondition(etHotspotEssid, editable)) {   // bridge
                    textChanged(length(editText) && length(essid) && length(etHotspotEssid));
                }

                Log.e("TAG", "afterTextChanged()");
            }
        };
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

    public static TextInputEditText getEtHotspotEssid() {return etHotspotEssid; }
}
