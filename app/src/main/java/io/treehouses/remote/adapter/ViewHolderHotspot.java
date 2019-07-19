package io.treehouses.remote.adapter;

import android.graphics.Color;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import io.treehouses.remote.ButtonConfiguration;
import io.treehouses.remote.Fragments.NetworkFragment;
import io.treehouses.remote.R;
import io.treehouses.remote.callback.HomeInteractListener;

class ViewHolderHotspot extends ButtonConfiguration{
    private EditText etEssid, etPassword;
    private Spinner spn;

    public ViewHolderHotspot(View v, final HomeInteractListener listener, final Context context) {
        etEssid = v.findViewById(R.id.et_hotspot_ssid);
        spn = v.findViewById(R.id.spn_hotspot_type);
        etPassword = v.findViewById(R.id.et_hotspot_password);
        btnStartConfiguration = v.findViewById(R.id.btn_start_config);
        btnStartConfiguration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (etPassword.getText().toString().isEmpty()) {
                    listener.sendMessage("treehouses ap \"" + spn.getSelectedItem().toString() + "\" \"" + etEssid.getText().toString() + "\"");
                    messageSent = true;
                    Toast.makeText(context, "Connecting...", Toast.LENGTH_LONG).show();
                } else {
                    listener.sendMessage("treehouses ap \"" + spn.getSelectedItem().toString() + "\" \"" + etEssid.getText().toString() + "\" \"" + etPassword.getText().toString() + "\"");
                    messageSent = true;
                    Toast.makeText(context, "Connecting...", Toast.LENGTH_LONG).show();
                }

                buttonProperties(false, Color.LTGRAY, btnStartConfiguration);
            }
        });

    }
}
