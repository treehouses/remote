package io.treehouses.remote.Fragments.DialogFragments.BottomSheetDialogs;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputLayout;

import io.treehouses.remote.Constants;
import io.treehouses.remote.Fragments.DialogFragments.WifiDialogFragment;
import io.treehouses.remote.Fragments.TextBoxValidation;
import io.treehouses.remote.R;
import io.treehouses.remote.bases.BaseBottomSheetDialog;
import io.treehouses.remote.pojo.NetworkProfile;
import io.treehouses.remote.utils.SaveUtils;

import static io.treehouses.remote.Fragments.NewNetworkFragment.CLICKED_START_CONFIG;
import static io.treehouses.remote.Fragments.NewNetworkFragment.openWifiDialog;


public class WifiBottomSheet extends BaseBottomSheetDialog {

    private EditText ssidText;
    private EditText passwordText;
    private Button startConfig, addProfile, searchWifi;
    private CheckBox hiddenEnabled;

    @Nullable
    @Override

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_wifi, container, false);
        ssidText = v.findViewById(R.id.editTextSSID);
        passwordText = v.findViewById(R.id.wifipassword);
        startConfig = v.findViewById(R.id.btn_start_config);
        addProfile = v.findViewById(R.id.set_wifi_profile);
        searchWifi = v.findViewById(R.id.btnWifiSearch);
        hiddenEnabled = v.findViewById(R.id.checkBoxHiddenWifi);

        setStartConfigListener();

        setAddProfileListener();

        searchWifi.setOnClickListener(v1 -> openWifiDialog(WifiBottomSheet.this, context));

        TextBoxValidation validation = new TextBoxValidation(getContext(), ssidText, passwordText, "wifi");
        validation.setStart(startConfig);
        validation.setAddprofile(addProfile);
        validation.setTextInputLayout(v.findViewById(R.id.textInputLayout));


        return v;
    }

    private void setStartConfigListener() {
        startConfig.setOnClickListener(v -> {
            String ssid = ssidText.getText().toString();
            String password = passwordText.getText().toString();
            if (hiddenEnabled.isChecked()) listener.sendMessage(String.format("treehouses wifihidden \"%s\" \"%s\"", ssid, password));
            else listener.sendMessage(String.format("treehouses wifi \"%s\" \"%s\"", ssid, password));

            Toast.makeText(context, "Connecting...", Toast.LENGTH_LONG).show();
            Intent intent = new Intent();
            intent.putExtra(CLICKED_START_CONFIG, true);
            getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
            dismiss();
        });
    }

    private void setAddProfileListener() {
        addProfile.setOnClickListener(v -> {
            SaveUtils.addProfile(context, new NetworkProfile(ssidText.getText().toString(), passwordText.getText().toString()));
            Toast.makeText(context, "WiFi Profile Saved", Toast.LENGTH_LONG).show();
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if((resultCode == Activity.RESULT_OK) && (requestCode == Constants.REQUEST_DIALOG_WIFI) ) {
            ssidText.setText(data.getStringExtra(WifiDialogFragment.WIFI_SSID_KEY));
        }
    }
    

}
