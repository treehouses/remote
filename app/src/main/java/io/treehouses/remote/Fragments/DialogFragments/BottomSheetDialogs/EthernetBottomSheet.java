package io.treehouses.remote.Fragments.DialogFragments.BottomSheetDialogs;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import io.treehouses.remote.bases.BaseBottomSheetDialog;
import io.treehouses.remote.databinding.DialogEthernetBinding;

import static io.treehouses.remote.Fragments.NewNetworkFragment.CLICKED_START_CONFIG;

public class EthernetBottomSheet extends BaseBottomSheetDialog {

    private DialogEthernetBinding bind;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        bind = DialogEthernetBinding.inflate(inflater, container, false);
//        etIP = v.findViewById(R.id.ip);
//        etMask = v.findViewById(R.id.mask);
//        etGateway = v.findViewById(R.id.gateway);
//        DNSText = v.findViewById(R.id.dns);
//
//        startConfig = v.findViewById(R.id.btn_start_config);

        bind.btnStartConfig.setOnClickListener(v1 -> {
            String ip = bind.ip.getText().toString();
            String dns = bind.dns.getText().toString();
            String gateway = bind.gateway.getText().toString();
            String mask = bind.mask.getText().toString();
            listener.sendMessage(String.format("treehouses ethernet %s %s %s %s", ip, mask, gateway, dns));
            Intent intent = new Intent();
            intent.putExtra(CLICKED_START_CONFIG, true);
            getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
            dismiss();
        });
        return bind.getRoot();
    }
}
