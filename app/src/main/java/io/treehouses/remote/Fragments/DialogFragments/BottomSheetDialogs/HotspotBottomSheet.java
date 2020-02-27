package io.treehouses.remote.Fragments.DialogFragments.BottomSheetDialogs;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import io.treehouses.remote.R;
import io.treehouses.remote.callback.HomeInteractListener;
import io.treehouses.remote.pojo.NetworkProfile;
import io.treehouses.remote.utils.SaveUtils;

import static io.treehouses.remote.Fragments.NewNetworkFragment.CLICKED_START_CONFIG;

public class HotspotBottomSheet extends BottomSheetDialogFragment {
    private EditText essidText;
    private EditText passwordText;
    private Button startConfig, addProfile;
    private Spinner spinner;

    private HomeInteractListener listener;
    private Context context;

    public HotspotBottomSheet(HomeInteractListener listener, Context context) {
        this.listener = listener;
        this.context = context;
    }
    @Nullable
    @Override

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_hotspot, container, false);

        essidText = v.findViewById(R.id.et_hotspot_ssid);
        passwordText = v.findViewById(R.id.et_hotspot_password);
        startConfig = v.findViewById(R.id.btn_start_config);
        addProfile = v.findViewById(R.id.set_hotspot_profile);

        spinner = v.findViewById(R.id.spn_hotspot_type);

        startConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (passwordText.getText().toString().isEmpty()) {
                    listener.sendMessage("treehouses ap " + spinner.getSelectedItem().toString() + " " + essidText.getText().toString());
                    Toast.makeText(context, "Connecting...", Toast.LENGTH_LONG).show();
                } else {
                    listener.sendMessage("treehouses ap " + spinner.getSelectedItem().toString() + " " + essidText.getText().toString() + " " + passwordText.getText().toString());
                    Toast.makeText(context, "Connecting...", Toast.LENGTH_LONG).show();
                }
                Intent intent = new Intent();
                intent.putExtra(CLICKED_START_CONFIG, true);
                getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
                dismiss();
            }
        });

        addProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveUtils.addProfile(context, new NetworkProfile(essidText.getText().toString(), passwordText.getText().toString(), spinner.getSelectedItem().toString()));
                Toast.makeText(context, "Hotspot Profile Saved", Toast.LENGTH_LONG).show();
            }
        });
        return v;
    }

}
