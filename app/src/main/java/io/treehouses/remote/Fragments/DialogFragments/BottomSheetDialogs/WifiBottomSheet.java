package io.treehouses.remote.Fragments.DialogFragments.BottomSheetDialogs;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;

import io.treehouses.remote.Constants;
import io.treehouses.remote.Fragments.DialogFragments.WifiDialogFragment;
import io.treehouses.remote.Fragments.TextBoxValidation;
import io.treehouses.remote.bases.BaseBottomSheetDialog;
import io.treehouses.remote.databinding.DialogWifiBinding;
import io.treehouses.remote.pojo.NetworkProfile;
import io.treehouses.remote.utils.SaveUtils;

import static io.treehouses.remote.Fragments.NewNetworkFragment.CLICKED_START_CONFIG;
import static io.treehouses.remote.Fragments.NewNetworkFragment.openWifiDialog;


public class WifiBottomSheet extends BaseBottomSheetDialog {

    private DialogWifiBinding bind;
    @Nullable
    @Override

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        bind = DialogWifiBinding.inflate(inflater, container, false);

        setStartConfigListener();

        setAddProfileListener();

        bind.btnWifiSearch.setOnClickListener(v1 -> openWifiDialog(WifiBottomSheet.this, context));

        TextBoxValidation validation = new TextBoxValidation(getContext(), bind.editTextSSID, bind.wifipassword, "wifi");
        validation.setStart(bind.btnStartConfig);
        validation.setAddprofile(bind.setWifiProfile);
        validation.setTextInputLayout(bind.textInputLayout);


        return bind.getRoot();
    }

    private void setStartConfigListener() {
        bind.btnStartConfig.setOnClickListener(v -> {
            String ssid = bind.editTextSSID.getText().toString();
            String password = bind.wifipassword.getText().toString();
            if (bind.checkBoxHiddenWifi.isChecked()) listener.sendMessage(String.format("treehouses wifihidden \"%s\" \"%s\"", ssid, password));
            else listener.sendMessage(String.format("treehouses wifi \"%s\" \"%s\"", ssid, password));

            Toast.makeText(context, "Connecting...", Toast.LENGTH_LONG).show();
            Intent intent = new Intent();
            intent.putExtra(CLICKED_START_CONFIG, true);
            getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
            dismiss();
        });
    }

    private void setAddProfileListener() {
        bind.setWifiProfile.setOnClickListener(v -> {
            SaveUtils.addProfile(context, new NetworkProfile(bind.editTextSSID.getText().toString(), bind.wifipassword.getText().toString()));
            Toast.makeText(context, "WiFi Profile Saved", Toast.LENGTH_LONG).show();
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if((resultCode == Activity.RESULT_OK) && (requestCode == Constants.REQUEST_DIALOG_WIFI) ) {
            bind.editTextSSID.setText(data.getStringExtra(WifiDialogFragment.WIFI_SSID_KEY));
        }
    }
    

}
