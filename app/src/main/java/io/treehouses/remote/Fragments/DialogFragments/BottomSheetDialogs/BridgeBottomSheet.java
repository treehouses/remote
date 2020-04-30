package io.treehouses.remote.Fragments.DialogFragments.BottomSheetDialogs;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
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
import io.treehouses.remote.bases.BaseBottomSheetDialog;
import io.treehouses.remote.callback.HomeInteractListener;
import io.treehouses.remote.pojo.NetworkProfile;
import io.treehouses.remote.utils.SaveUtils;

import static io.treehouses.remote.Fragments.NewNetworkFragment.CLICKED_START_CONFIG;
import static io.treehouses.remote.Fragments.NewNetworkFragment.openWifiDialog;

public class BridgeBottomSheet extends BaseBottomSheetDialog {

    private EditText essid, password, hotspotEssid, hotspotPassword;
    private Button startConfig, addProfile, btnWifiSearch;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_bridge, container, false);

        essid = v.findViewById(R.id.et_essid);
        password = v.findViewById(R.id.et_password);
        hotspotEssid = v.findViewById(R.id.et_hotspot_essid);
        hotspotPassword = v.findViewById(R.id.et_hotspot_password);

        startConfig = v.findViewById(R.id.btn_start_config);
        addProfile = v.findViewById(R.id.add_bridge_profile);
        btnWifiSearch = v.findViewById(R.id.btnWifiSearch);

        try {
            essid.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            hotspotEssid.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        } catch (Exception e) {
            e.printStackTrace();
        }

        startConfigListener();
        setAddProfileListener();

        btnWifiSearch.setOnClickListener(v1 -> openWifiDialog(BridgeBottomSheet.this, context));

        return v;
    }

    private void startConfigListener() {
        startConfig.setOnClickListener(v -> {
            String temp = "treehouses bridge \"" + essid.getText().toString() + "\" \"" + hotspotEssid.getText().toString() + "\" ";
            String overallMessage = TextUtils.isEmpty(password.getText().toString()) ? temp + "\"\"" : temp + "\"" + password.getText().toString() + "\"";
            overallMessage += " ";

            if (!TextUtils.isEmpty(hotspotPassword.getText().toString())) {
                overallMessage += "\"" + hotspotPassword.getText().toString() + "\"";
            }
            listener.sendMessage(overallMessage);

            Toast.makeText(context, "Connecting...", Toast.LENGTH_LONG).show();
            Intent intent = new Intent();
            intent.putExtra(CLICKED_START_CONFIG, true);
            getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
            dismiss();
        });
    }

    private void setAddProfileListener() {
        addProfile.setOnClickListener(v -> {
            NetworkProfile networkProfile = new NetworkProfile(essid.getText().toString(), password.getText().toString(),
                    hotspotEssid.getText().toString(), hotspotPassword.getText().toString());

            SaveUtils.addProfile(context, networkProfile);
            Toast.makeText(context, "Bridge Profile Added", Toast.LENGTH_LONG).show();
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if( resultCode != Activity.RESULT_OK ) {
            return;
        }
        if( requestCode == Constants.REQUEST_DIALOG_WIFI ) {
            String ssid = data.getStringExtra(WifiDialogFragment.WIFI_SSID_KEY);
            essid.setText(ssid);
        }
    }
}
