package io.treehouses.remote.Fragments.DialogFragments.BottomSheetDialogs;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;

import io.treehouses.remote.bases.BaseBottomSheetDialog;
import io.treehouses.remote.databinding.DialogHotspotBinding;
import io.treehouses.remote.pojo.NetworkProfile;
import io.treehouses.remote.utils.SaveUtils;

import static io.treehouses.remote.Fragments.NewNetworkFragment.CLICKED_START_CONFIG;

public class HotspotBottomSheet extends BaseBottomSheetDialog {

    private DialogHotspotBinding bind;

    @Nullable
    @Override

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        bind = DialogHotspotBinding.inflate(inflater, container, false);

        startConfigListener();

        setAddProfileListener();
        return bind.getRoot();
    }

    private void startConfigListener() {
        bind.btnStartConfig.setOnClickListener(v -> {
            String command =  bind.checkBoxHiddenWifi.isChecked() ? "treehouses hiddenap " : "treehouses ap ";
            if (bind.etHotspotPassword.getText().toString().isEmpty()) {
                listener.sendMessage(command + "\"" + bind.spnHotspotType.getSelectedItem().toString() + "\" \"" + bind.etHotspotSsid.getText().toString() + "\"");
                Toast.makeText(context, "Connecting...", Toast.LENGTH_LONG).show();
            } else {
                listener.sendMessage(command + "\"" + bind.spnHotspotType.getSelectedItem().toString() + "\" \"" + bind.etHotspotSsid.getText().toString() + "\" \"" + bind.etHotspotPassword.getText().toString() + "\"");
                Toast.makeText(context, "Connecting...", Toast.LENGTH_LONG).show();
            }
            Intent intent = new Intent();
            intent.putExtra(CLICKED_START_CONFIG, true);
            getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
            dismiss();
        });
    }

    private void setAddProfileListener() {
        bind.setHotspotProfile.setOnClickListener(v -> {
            SaveUtils.addProfile(context, new NetworkProfile(bind.etHotspotSsid.getText().toString(), bind.etHotspotPassword.getText().toString(), bind.spnHotspotType.getSelectedItem().toString()));
            Toast.makeText(context, "Hotspot Profile Saved", Toast.LENGTH_LONG).show();
        });
    }
}
