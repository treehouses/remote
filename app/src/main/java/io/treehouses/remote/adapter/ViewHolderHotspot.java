package io.treehouses.remote.adapter;

import android.graphics.Color;
import android.content.Context;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import io.treehouses.remote.ButtonConfiguration;
import io.treehouses.remote.R;
import io.treehouses.remote.callback.HomeInteractListener;

class ViewHolderHotspot extends ButtonConfiguration{
    private EditText etPassword;
    private Spinner spn;

    public ViewHolderHotspot(View v, final HomeInteractListener listener, final Context context) {
        etSsid = v.findViewById(R.id.et_hotspot_ssid);
        spn = v.findViewById(R.id.spn_hotspot_type);
        etPassword = v.findViewById(R.id.et_hotspot_password);
        btnStartConfiguration = v.findViewById(R.id.btn_start_config);
        etSsid.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

        buttonProperties(false, Color.LTGRAY, btnStartConfiguration);

        etSsid.addTextChangedListener(getTextWatcher(etSsid, v));

        btnStartConfiguration.setOnClickListener(view -> {
            String ssid = etSsid.getText().toString();
            String spinner = spn.getSelectedItem().toString();
            String password = etPassword.getText().toString();
            if (password.isEmpty()) {
             //   listener.sendMessage("treehouses ap \"" + spinner + "\" \"" + ssid + "\"");
                Toast.makeText(context, "Connecting...", Toast.LENGTH_LONG).show();
            } else {
              //  listener.sendMessage("treehouses ap \"" + spinner + "\" \"" + ssid + "\" \"" + password+ "\"");
                Toast.makeText(context, "Connecting...", Toast.LENGTH_LONG).show();
            }

            saveNetwork(context, "ssid", ssid, "password", password, "spinner", spinner);

            buttonProperties(false, Color.LTGRAY, btnStartConfiguration);
        });
    }
}
