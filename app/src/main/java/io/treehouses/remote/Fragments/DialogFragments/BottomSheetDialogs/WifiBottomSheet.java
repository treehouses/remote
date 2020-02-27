package io.treehouses.remote.Fragments.DialogFragments.BottomSheetDialogs;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import io.treehouses.remote.Constants;
import io.treehouses.remote.Fragments.DialogFragments.WifiDialogFragment;
import io.treehouses.remote.R;
import io.treehouses.remote.callback.HomeInteractListener;
import io.treehouses.remote.pojo.NetworkProfile;
import io.treehouses.remote.utils.SaveUtils;

import static io.treehouses.remote.Fragments.NewNetworkFragment.CLICKED_START_CONFIG;
import static io.treehouses.remote.Fragments.NewNetworkFragment.openWifiDialog;


public class WifiBottomSheet extends BottomSheetDialogFragment{

    private EditText ssidText;
    private EditText passwordText;
    private Button startConfig, addProfile, searchWifi;

    private HomeInteractListener listener;
    private Context context;

    public WifiBottomSheet(HomeInteractListener listener, Context context) {
        this.listener = listener;
        this.context = context;
    }

    @Nullable
    @Override

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_wifi, container, false);
        ssidText = v.findViewById(R.id.editTextSSID);
        passwordText = v.findViewById(R.id.wifipassword);
        startConfig = v.findViewById(R.id.btn_start_config);
        addProfile = v.findViewById(R.id.set_wifi_profile);
        searchWifi = v.findViewById(R.id.btnWifiSearch);

        startConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ssid = ssidText.getText().toString();
                String password = passwordText.getText().toString();
                listener.sendMessage(String.format("treehouses wifi %s %s", ssid, password));
                Toast.makeText(context, "Connecting...", Toast.LENGTH_LONG).show();
                Intent intent = new Intent();
                intent.putExtra(CLICKED_START_CONFIG, true);
                getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
                dismiss();
            }
        });

        addProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveUtils.addProfile(context, new NetworkProfile(ssidText.getText().toString(), passwordText.getText().toString()));
                Toast.makeText(context, "WiFi Profile Saved", Toast.LENGTH_LONG).show();
            }
        });

        searchWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWifiDialog(WifiBottomSheet.this, context);

            }
        });

        return v;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if( resultCode != Activity.RESULT_OK ) {
            return;
        }
        if( requestCode == Constants.REQUEST_DIALOG_WIFI ) {
            String ssid = data.getStringExtra(WifiDialogFragment.WIFI_SSID_KEY);
            ssidText.setText(ssid);
        }
    }

    @Override
    public void onDismiss(final DialogInterface dialogInterface) {

    }

}
