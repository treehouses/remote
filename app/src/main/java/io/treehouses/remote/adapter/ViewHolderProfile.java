package io.treehouses.remote.adapter;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import com.google.android.material.textfield.TextInputEditText;

import io.treehouses.remote.ButtonConfiguration;
import io.treehouses.remote.Fragments.ProfileFragment;
import io.treehouses.remote.MainApplication;
import io.treehouses.remote.R;

public class ViewHolderProfile extends ButtonConfiguration {



    ViewHolderProfile(Context context, View v) {
        Spinner spinnerProfile = v.findViewById(R.id.spinnerProfile);



        if (NetworkListAdapter.getLayout() == R.layout.profile_ethernet) {
            ethernet(v);
        } else if (NetworkListAdapter.getLayout() == R.layout.profile_wifi) {
            wifi(v);
        } else if (NetworkListAdapter.getLayout() == R.layout.profile_hotspot) {
            hotspot(v);
        } else if (NetworkListAdapter.getLayout() == R.layout.profile_bridge) {
            bridge(v);
        }
    }

    private void ethernet(View v) {
        TextInputEditText editTextIp = v.findViewById(R.id.editTextIp);
        TextInputEditText editTextMask = v.findViewById(R.id.editTextMask);
        TextInputEditText editTextGateway = v.findViewById(R.id.editTextGateway);
        TextInputEditText editTextDns = v.findViewById(R.id.editTextDns);

        String ip = MainApplication.getSharedPreferences().getString("ip", "");
        String mask = MainApplication.getSharedPreferences().getString("mask", "");
        String gateway = MainApplication.getSharedPreferences().getString("gateway", "");
        String dns = MainApplication.getSharedPreferences().getString("dns", "");

        editTextIp.setText(ip);
        editTextDns.setText(dns);
        editTextMask.setText(mask);
        editTextGateway.setText(gateway);
    }

    private void wifi(View v) {
        TextInputEditText editTextSsid = v.findViewById(R.id.editTextSSID);
        TextInputEditText editTextPassword = v.findViewById(R.id.editTextPassword);

        String ssid = MainApplication.getSharedPreferences().getString("wifiSsid", "");
        String password = MainApplication.getSharedPreferences().getString("wifiPassword", "");

        editTextSsid.setText(ssid);
        editTextPassword.setText(password);
    }

    private void hotspot(View v) {
        TextInputEditText editTextSsid = v.findViewById(R.id.editTextSsid);
        TextInputEditText editTextPassword = v.findViewById(R.id.editTextPassword);
        Spinner hotspotSpinner = v.findViewById(R.id.spinner);

        String ssid = MainApplication.getSharedPreferences().getString("hotspotSsid", "");
        String password = MainApplication.getSharedPreferences().getString("hotspotPassword", "");
        String spinner = MainApplication.getSharedPreferences().getString("hotspotSpinner", "");

        editTextSsid.setText(ssid);
        editTextPassword.setText(password);
        hotspotSpinner.setSelection((spinner.equals("internet") ? 0 : 1));
    }

    private void bridge(View v) {
        TextInputEditText editTextSsid = v.findViewById(R.id.editTextSsid);
        TextInputEditText editTextPassword = v.findViewById(R.id.editTextPassword);
        TextInputEditText editTextHotspot = v.findViewById(R.id.editTextHostpotSsid);
        TextInputEditText editTextHotspotPass = v.findViewById(R.id.editTextHostpotPassword);

        String ssid = MainApplication.getSharedPreferences().getString("bridgeSsid", "");
        String ssidPassword = MainApplication.getSharedPreferences().getString("bridgeSsidPassword", "");
        String hotspotSsid = MainApplication.getSharedPreferences().getString("bridgeHotspotSsid", "");
        String hotspotPass = MainApplication.getSharedPreferences().getString("bridgeHotspotPassword", "");

        editTextSsid.setText(ssid);
        editTextPassword.setText(ssidPassword);
        editTextHotspot.setText(hotspotSsid);
        editTextHotspotPass.setText(hotspotPass);
    }
}
