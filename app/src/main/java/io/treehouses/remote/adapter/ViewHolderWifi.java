package io.treehouses.remote.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.InputType;
import android.view.View;
import android.content.Context;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.List;

import io.treehouses.remote.pojo.NetworkProfile;
import io.treehouses.remote.utils.ButtonConfiguration;
import io.treehouses.remote.R;
import io.treehouses.remote.utils.SaveUtils;
import io.treehouses.remote.utils.TextWatcherUtils;
import io.treehouses.remote.callback.HomeInteractListener;
import io.treehouses.remote.utils.ButtonConfiguration;
import io.treehouses.remote.utils.TextWatcherUtils;

class ViewHolderWifi extends ButtonConfiguration {

    private static TextInputEditText etPassword;
    private Button saveProfile;

    ViewHolderWifi(View v, final HomeInteractListener listener, final Context context) {
        etSsid = v.findViewById(R.id.et_ssid);
        etPassword = v.findViewById(R.id.et_password);
        btnStartConfiguration = v.findViewById(R.id.btn_start_config);
        btnWifiSearch = v.findViewById(R.id.btnWifiSearch);
        saveProfile = v.findViewById(R.id.set_wifi_profile);

        buttonProperties(false, Color.LTGRAY, btnStartConfiguration);

        etSsid.addTextChangedListener(new TextWatcherUtils(etSsid));


        etSsid.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

        buttonWifiSearch(context);

        btnStartConfiguration.setOnClickListener(view -> {
            String ssid = etSsid.getText().toString();
            String password = etPassword.getText().toString();
            listener.sendMessage(String.format("treehouses wifi \"%s\" \"%s\"", ssid, password));
            messageSent = true;

            buttonProperties(false, Color.LTGRAY, btnStartConfiguration);

            Toast.makeText(context, "Connecting...", Toast.LENGTH_LONG).show();

        });

        saveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveUtils.addProfile(context, new NetworkProfile(etSsid.getText().toString(), etPassword.getText().toString(),""));
                Toast.makeText(context, "Profile Saved", Toast.LENGTH_LONG).show();
            }
        });

    }
}
