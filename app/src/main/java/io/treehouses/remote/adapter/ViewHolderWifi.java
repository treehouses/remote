package io.treehouses.remote.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.InputType;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import io.treehouses.remote.R;
import io.treehouses.remote.callback.HomeInteractListener;
import io.treehouses.remote.utils.ButtonConfiguration;
import io.treehouses.remote.utils.TextWatcherUtils;

class ViewHolderWifi extends ButtonConfiguration {

    private static TextInputEditText etPassword;

    ViewHolderWifi(View v, final HomeInteractListener listener, final Context context) {
        etSsid = v.findViewById(R.id.et_ssid);
        etPassword = v.findViewById(R.id.et_password);
        btnStartConfiguration = v.findViewById(R.id.btn_start_config);
        btnWifiSearch = v.findViewById(R.id.btnWifiSearch);

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
    }
}
