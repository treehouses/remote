package io.treehouses.remote.adapter;

import android.graphics.Color;
import android.content.Context;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputEditText;
import io.treehouses.remote.ButtonConfiguration;
import io.treehouses.remote.R;
import io.treehouses.remote.callback.HomeInteractListener;

public class ViewHolderBridge extends ButtonConfiguration {
    private TextInputEditText etPassword, etHotspotPassword;
    private Button btnStartConfiguration;

    ViewHolderBridge(View v, final HomeInteractListener listener, final Context context) {

        essid = v.findViewById(R.id.et_essid);
        etHotspotEssid = v.findViewById(R.id.et_hotspot_essid);
        etPassword = v.findViewById(R.id.et_password);
        etHotspotPassword = v.findViewById(R.id.et_hotspot_password);
        setBtnStartConfiguration(btnStartConfiguration = v.findViewById(R.id.btn_start_config));
        btnWifiSearch = v.findViewById(R.id.btnWifiSearch);

        try {
            etSsid.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            etHotspotEssid.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        } catch (Exception e) {
            e.printStackTrace();
        }

        buttonWifiSearch(context);

        buttonProperties(false, Color.LTGRAY, btnStartConfiguration);

        essid.addTextChangedListener(new MyTextWatcher(etSsid));
        etHotspotEssid.addTextChangedListener(new MyTextWatcher(etHotspotEssid));
        etPassword.addTextChangedListener(new MyTextWatcher(etPassword));

        btnStartConfiguration.setOnClickListener(view -> {
            String temp = "treehouses bridge \"" + etSsid.getText().toString() + "\" \"" + etHotspotEssid.getText().toString() + "\" ";
            String overallMessage = TextUtils.isEmpty(etPassword.getText().toString()) ? temp + "\"\"" : temp + "\"" + etPassword.getText().toString() + "\"";
            overallMessage += " ";

            if (!TextUtils.isEmpty(etHotspotPassword.getText().toString())) {
                overallMessage += "\"" + etHotspotPassword.getText().toString() + "\"";
            }
            messageSent = true;
            listener.sendMessage(overallMessage);

            buttonProperties(false, Color.LTGRAY, btnStartConfiguration);

            Toast.makeText(context, "Connecting...", Toast.LENGTH_LONG).show();
        });
    }
}
