package io.treehouses.remote.adapter;

import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;

import io.treehouses.remote.R;
import io.treehouses.remote.callback.HomeInteractListener;

public class ViewHolderHotspot {
 private    EditText etEssid, etPassword;
private Spinner spn;
    public ViewHolderHotspot(View v, final HomeInteractListener listener) {
        etEssid = v.findViewById(R.id.et_hotspot_ssid);
        etPassword = v.findViewById(R.id.et_hotspot_password);
        v.findViewById(R.id.btn_start_config).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (etPassword.getText().toString().isEmpty()) {
                    listener.sendMessage("treehouses ap \"" + spn.getSelectedItem().toString() + "\" \"" + etEssid.getText().toString() + "\"");
                } else {
                    listener.sendMessage("treehouses ap \"" + spn.getSelectedItem().toString() + "\" \"" + etEssid.getText().toString() + "\" \"" + etPassword.getText().toString() + "\"");
                }

            }
        });
    }
}
