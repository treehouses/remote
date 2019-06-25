package io.treehouses.remote.adapter;

import android.graphics.Color;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputEditText;
import io.treehouses.remote.ButtonConfiguration;
import io.treehouses.remote.Fragments.NetworkFragment;
import io.treehouses.remote.R;
import io.treehouses.remote.callback.ButtonConfig;
import io.treehouses.remote.callback.HomeInteractListener;

public class ViewHolderEthernet implements ButtonConfig {
    public TextInputEditText etIp, etDNS, etGateway, etMask;
    public Button btnStartConfiguration;

    public ViewHolderEthernet(View v, final HomeInteractListener listener, final Context context) {
        btnStartConfiguration = v.findViewById(R.id.btn_start_config);
        etIp = v.findViewById(R.id.ip);
        etDNS = v.findViewById(R.id.dns);
        etGateway = v.findViewById(R.id.gateway);
        etMask = v.findViewById(R.id.mask);
        btnStartConfiguration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ip = etIp.getText().toString();
                String dns = etDNS.getText().toString();
                String gateway = etGateway.getText().toString();
                String mask = etMask.getText().toString();
                listener.sendMessage(String.format("treehouses ethernet \"%s\" \"%s\" \"%s\" \"%s\"", ip, mask, gateway, dns));

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
