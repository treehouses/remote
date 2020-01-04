package io.treehouses.remote.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.Toast;

import io.treehouses.remote.R;
import io.treehouses.remote.callback.HomeInteractListener;
import io.treehouses.remote.utils.ButtonConfiguration;
import io.treehouses.remote.utils.TextWatcherUtils;

public class ViewHolderEthernet extends ButtonConfiguration {


    public ViewHolderEthernet(View v, final HomeInteractListener listener, final Context context) {
        btnStartConfiguration = v.findViewById(R.id.btn_start_config);
        etIp = v.findViewById(R.id.ip);
        etDNS = v.findViewById(R.id.dns);
        etGateway = v.findViewById(R.id.gateway);
        etMask = v.findViewById(R.id.mask);

        buttonProperties(false, Color.LTGRAY, btnStartConfiguration);

        etIp.addTextChangedListener(new TextWatcherUtils(etIp));
        etDNS.addTextChangedListener(new TextWatcherUtils(etDNS));
        etGateway.addTextChangedListener(new TextWatcherUtils(etGateway));
        etMask.addTextChangedListener(new TextWatcherUtils(etMask));


        btnStartConfiguration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ip = etIp.getText().toString();
                String dns = etDNS.getText().toString();
                String gateway = etGateway.getText().toString();
                String mask = etMask.getText().toString();
                listener.sendMessage(String.format("treehouses ethernet \"%s\" \"%s\" \"%s\" \"%s\"", ip, mask, gateway, dns));
                messageSent = true;

                buttonProperties(false, Color.LTGRAY, btnStartConfiguration);

                Toast.makeText(context, "Connecting...", Toast.LENGTH_LONG).show();
            }
        });

    }
}
