package io.treehouses.remote.Fragments.DialogFragments.BottomSheetDialogs;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;

import io.treehouses.remote.Constants;
import io.treehouses.remote.Fragments.DialogFragments.WifiDialogFragment;
import io.treehouses.remote.Fragments.TextBoxValidation;
import io.treehouses.remote.bases.BaseBottomSheetDialog;
import io.treehouses.remote.databinding.DialogBridgeBinding;
import io.treehouses.remote.pojo.NetworkProfile;
import io.treehouses.remote.utils.SaveUtils;

import static io.treehouses.remote.Fragments.NewNetworkFragment.CLICKED_START_CONFIG;
import static io.treehouses.remote.Fragments.NewNetworkFragment.openWifiDialog;

public class BridgeBottomSheet extends BaseBottomSheetDialog {


    private DialogBridgeBinding bind;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        bind = DialogBridgeBinding.inflate(inflater, container, false);

        try {
            bind.etEssid.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            bind.etHotspotEssid.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        } catch (Exception e) {
            e.printStackTrace();
        }

        startConfigListener();
        setAddProfileListener();

        bind.btnWifiSearch.setOnClickListener(v1 -> openWifiDialog(BridgeBottomSheet.this, context));

        TextBoxValidation validation = new TextBoxValidation(getContext(), bind.etEssid, bind.etHotspotEssid, "bridge");
        validation.setStart(bind.btnStartConfig);
        validation.setAddprofile(bind.addBridgeProfile);
        return bind.getRoot();
    }

    private void startConfigListener() {
        bind.btnStartConfig.setOnClickListener(v -> {
            String temp = "treehouses bridge \"" + bind.etEssid.getText().toString() + "\" \"" + bind.etHotspotEssid.getText().toString() + "\" ";
            String overallMessage = TextUtils.isEmpty(bind.etPassword.getText().toString()) ? temp + "\"\"" : temp + "\"" + bind.etPassword.getText().toString() + "\"";
            overallMessage += " ";

            if (!TextUtils.isEmpty(bind.etHotspotPassword.getText().toString())) {
                overallMessage += "\"" + bind.etHotspotPassword.getText().toString() + "\"";
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
        bind.addBridgeProfile.setOnClickListener(v -> {
            NetworkProfile networkProfile = new NetworkProfile(bind.etEssid.getText().toString(), bind.etPassword.getText().toString(),
                    bind.etHotspotEssid.getText().toString(), bind.etHotspotPassword.getText().toString());

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
            bind.etEssid.setText(ssid);
        }
    }
}
