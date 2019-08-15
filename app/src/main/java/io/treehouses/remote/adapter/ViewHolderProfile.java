package io.treehouses.remote.adapter;

import android.content.Context;
import android.view.View;

import com.google.android.material.textfield.TextInputEditText;

import io.treehouses.remote.MainApplication;
import io.treehouses.remote.R;

public class ViewHolderProfile {

    private TextInputEditText editTextIp;
    private TextInputEditText editTextMask;
    private TextInputEditText editTextGateway;
    private TextInputEditText editTextDns;

    private String ip;
    private String mask;
    private String gateway;
    private String dns;


    ViewHolderProfile(Context context, View v) {
        initialize(v);
        setVariables();

        if (NetworkListAdapter.getLayout() == R.layout.profile_ethernet) {
            ethernet();
        }
    }

    private void setVariables() {
        ip = MainApplication.getSharedPreferences().getString("ip", "");
        mask = MainApplication.getSharedPreferences().getString("mask", "");
        gateway = MainApplication.getSharedPreferences().getString("gateway", "");
        dns = MainApplication.getSharedPreferences().getString("dns", "");
    }

    private void initialize(View v) {
        editTextIp = v.findViewById(R.id.editTextIp);
        editTextMask = v.findViewById(R.id.editTextMask);
        editTextGateway = v.findViewById(R.id.editTextGateway);
        editTextDns = v.findViewById(R.id.editTextDNS);
    }

    private void ethernet() {
        editTextIp.setText(ip);
        editTextMask.setText(mask);
        editTextGateway.setText(gateway);
        editTextDns.setText(dns);
    }

    private void wifi() {

    }
}
