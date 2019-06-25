package io.treehouses.remote.adapter;

import android.graphics.Color;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputEditText;
import io.treehouses.remote.ButtonConfiguration;
import io.treehouses.remote.Fragments.NetworkFragment;
import io.treehouses.remote.callback.ButtonConfig;
import io.treehouses.remote.R;
import io.treehouses.remote.callback.HomeInteractListener;

public class ViewHolderBridge implements ButtonConfig {
    private TextInputEditText etEssid, etHotspotEssid, etPassword, etHotspotPassword;
    private Button btnStartConfiguration;

    public ViewHolderBridge(View v, final HomeInteractListener listener, final Context context) {

        etEssid = v.findViewById(R.id.et_essid);
        etHotspotEssid = v.findViewById(R.id.et_hotspot_essid);
        etPassword = v.findViewById(R.id.et_password);
        etHotspotPassword = v.findViewById(R.id.et_hotspot_password);
        btnStartConfiguration = v.findViewById(R.id.btn_start_config);
        btnStartConfiguration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String temp = "treehouses bridge \"" + etEssid.getText().toString() + "\" \"" + etHotspotEssid.getText().toString() + "\" ";
                String overallMessage = TextUtils.isEmpty(etPassword.getText().toString()) ? temp + "\"\"" : temp + "\"" + etPassword.getText().toString() + "\"";
                overallMessage += " ";
                if (!TextUtils.isEmpty(etHotspotPassword.getText().toString())) {
                    overallMessage += "\"" + etHotspotPassword.getText().toString() + "\"";
                }
                listener.sendMessage(overallMessage);

                btnConfigDisabled(false, Color.LTGRAY);
              
                Toast.makeText(context, "Connecting...", Toast.LENGTH_LONG).show();

            }
        });
    }

    @Override
    public void btnConfigDisabled(Boolean clickable, int color) {
        NetworkFragment.getInstance().setBtnConfig(this);
        ButtonConfiguration.buttonProperties(btnStartConfiguration, clickable, color);
    }
}
