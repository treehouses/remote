package io.treehouses.remote.adapter;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.view.View;
import android.widget.Button;
import android.content.Context;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

import io.treehouses.remote.ButtonConfiguration;
import io.treehouses.remote.Constants;
import io.treehouses.remote.Fragments.NetworkFragment;
import io.treehouses.remote.Fragments.WifiDialogFragment;
import io.treehouses.remote.R;
import io.treehouses.remote.WifiAdapter.WiFiManager;
import io.treehouses.remote.callback.HomeInteractListener;

public class ViewHolderWifi extends ButtonConfiguration {
    private TextInputEditText etSsid, etPassword;
    private Button btnStartConfiguration, btnWifiSearch;

    public ViewHolderWifi(View v, final HomeInteractListener listener, final Context context) {
        etSsid = v.findViewById(R.id.et_ssid);
        etPassword = v.findViewById(R.id.et_password);
        btnWifiSearch = v.findViewById(R.id.btnWifiSearch);
        btnStartConfiguration = v.findViewById(R.id.btn_start_config);

        btnWifiSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NetworkFragment.getInstance().showWifiDialog(v);

//                AppCompatActivity activity = (AppCompatActivity) v.getContext();
//                WifiDialogFragment wifiDialogFragment = new WifiDialogFragment();
//                activity.getSupportFragmentManager().beginTransaction()
//                        .replace(R.id.fragment_container, wifiDialogFragment)
//                        .addToBackStack(null)
//                        .commit();

//                Intent intent = new Intent(v.getContext(), WiFiManager.class);
//                v.getContext().startActivity(intent);
            }
        });

        btnStartConfiguration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ssid = etSsid.getText().toString();
                String password = etPassword.getText().toString();
                listener.sendMessage(String.format("treehouses wifi \"%s\" \"%s\"", ssid, password));

                buttonProperties(false, Color.LTGRAY);

                Toast.makeText(context, "Connecting...", Toast.LENGTH_LONG).show();

            }

        });

    }



    @Override
    public void buttonProperties(Boolean clickable, int color) {
        NetworkFragment.getInstance().setButtonConfiguration(this);
        btnStartConfiguration.setClickable(clickable);
        btnStartConfiguration.setTextColor(color);
    }
}
