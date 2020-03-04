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

import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import io.treehouses.remote.R;
import io.treehouses.remote.bases.BaseBottomSheetDialog;
import io.treehouses.remote.callback.HomeInteractListener;

import static io.treehouses.remote.Fragments.NewNetworkFragment.CLICKED_START_CONFIG;

public class EthernetBottomSheet extends BaseBottomSheetDialog {

    private EditText etIP, etMask, etGateway, DNSText;
    private Button startConfig;

    @Nullable
    @Override

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_ethernet, container, false);
        etIP = v.findViewById(R.id.ip);
        etMask = v.findViewById(R.id.mask);
        etGateway = v.findViewById(R.id.gateway);
        DNSText = v.findViewById(R.id.dns);

        startConfig = v.findViewById(R.id.btn_start_config);

        startConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ip = etIP.getText().toString();
                String dns = DNSText.getText().toString();
                String gateway = etGateway.getText().toString();
                String mask = etMask.getText().toString();
                listener.sendMessage(String.format("treehouses ethernet %s %s %s %s", ip, mask, gateway, dns));
                Intent intent = new Intent();
                intent.putExtra(CLICKED_START_CONFIG, true);
                getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
                dismiss();
            }
        });
        return v;
    }
}
