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

//        etSsid.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        etHotspotEssid.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

        buttonWifiSearch(context);

        buttonProperties(false, Color.LTGRAY, btnStartConfiguration);

        essid.addTextChangedListener(getTextWatcher(etSsid, v));
        etHotspotEssid.addTextChangedListener(getTextWatcher(etHotspotEssid, v));
        etPassword.addTextChangedListener(getTextWatcher(etPassword, v));

        btnStartConfiguration.setOnClickListener(view -> {
            String essid = etSsid.getText().toString();
            String hotspotEssid = etHotspotEssid.getText().toString();
            String essidPass = etPassword.getText().toString();
            String hotspotPass = etHotspotPassword.getText().toString();

            String temp = "treehouses bridge \"" + essid + "\" \"" + hotspotEssid + "\" ";
            String overallMessage = TextUtils.isEmpty(essidPass) ? temp + "\"\"" : temp + "\"" + essidPass + "\"";
            overallMessage += " ";

            if (!TextUtils.isEmpty(hotspotPass)) {
                overallMessage += "\"" + hotspotPass + "\"";
            }
            messageSent = true;
            //listener.sendMessage(overallMessage);

            saveNetwork("ssid", essid, "hotspotSsid", hotspotEssid, "ssidPassword", essidPass, "hotspotPassword", hotspotPass);

            buttonProperties(false, Color.LTGRAY, btnStartConfiguration);

            Toast.makeText(context, "Connecting...", Toast.LENGTH_LONG).show();
        });
    }
}
