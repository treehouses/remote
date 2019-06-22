package io.treehouses.remote.adapter;

import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import com.google.android.material.textfield.TextInputEditText;

import io.treehouses.remote.Fragments.NetworkFragment;
import io.treehouses.remote.enums.Bridge;
import io.treehouses.remote.R;
import io.treehouses.remote.callback.HomeInteractListener;

public class ViewHolderBridge {
    public TextInputEditText etEssid, etHotspotEssid, etPassword, etHotspotPassword;
    public Button btnStartConfiguration;

    public ViewHolderBridge(View v, final HomeInteractListener listener) {
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
                btnStartConfiguration.setClickable(false);
                btnStartConfiguration.setTextColor(Color.LTGRAY);

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (true) {
                            if (NetworkFragment.getBridgeStatus() == Bridge.BUILT || NetworkFragment.getBridgeStatus() == Bridge.ERROR) {
                                btnStartConfiguration.setClickable(true);
                                btnStartConfiguration.setTextColor(Color.WHITE);
                                break;
                            }
                        }

                    }
                });
                thread.start();
            }
        });
    }
}
