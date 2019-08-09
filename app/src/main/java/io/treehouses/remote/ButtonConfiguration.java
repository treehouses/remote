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
    protected static TextInputEditText etHotspotEssid;
    protected TextInputEditText etPassword;
    protected Button btnStartConfiguration;
    protected Button btnWifiSearch;

    protected static @Nullable TextInputEditText etSsid, essid;
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
        etPassword = v.findViewById(R.id.et_password);
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void afterTextChanged(Editable editable) {

                afterTextChangedListener(editable, editText);

                Log.e("TAG", "afterTextChanged()");
            }
        };
    }

    private void afterTextChangedListener(Editable editable, EditText editText) {

        if (editable == etSsid.getEditableText() || editable == etPassword.getEditableText() && !messageSent) { // wifi
            textChanged(editText.length() > 0 && etSsid.length() > 0);
        } else if (editable == essid.getEditableText() && !messageSent) {                                       // bridge
            textChanged(editText.length() > 0 && etHotspotEssid.length() > 0);
        }
    }

    private void textChanged(boolean condition) {
        if (condition) {
            buttonProperties(true, Color.WHITE, btnStartConfiguration);
        } else {
            buttonProperties(false, Color.LTGRAY, btnStartConfiguration);
        }
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
