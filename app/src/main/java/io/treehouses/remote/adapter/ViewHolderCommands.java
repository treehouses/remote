package io.treehouses.remote.adapter;

import android.view.View;
import android.widget.Button;
import io.treehouses.remote.Fragments.TerminalFragment;
import io.treehouses.remote.MainApplication;
import io.treehouses.remote.R;
import io.treehouses.remote.callback.HomeInteractListener;

class ViewHolderCommands {

    private Button changePass, treehouses, detect, docker, expand, vncOn, vncStatus, clear;

    ViewHolderCommands(View v, final HomeInteractListener listener) {
        initializeButtons(v);

        changePass.setOnClickListener(v15 -> TerminalFragment.getInstance().showChPasswordDialog());
        treehouses.setOnClickListener(v14 -> listener.sendMessage("treehouses help"));
        detect.setOnClickListener(v13 -> listener.sendMessage("treehouses detectrpi"));
        docker.setOnClickListener(v12 -> listener.sendMessage("docker ps"));
        expand.setOnClickListener(v1 -> listener.sendMessage("treehouses expandfs"));
        vncOn.setOnClickListener(v16 -> listener.sendMessage("treehouses vnc on"));
        vncStatus.setOnClickListener(v17 -> listener.sendMessage("treehouses vnc"));
        clear.setOnClickListener(v1 -> {
            MainApplication.getTerminalList().clear();
            TerminalFragment.getInstance().getmConversationArrayAdapter().notifyDataSetChanged();
        });
}

    private void initializeButtons(View v) {
        changePass = v.findViewById(R.id.btnChangePass);
        treehouses = v.findViewById(R.id.btnTreehouses);
        detect = v.findViewById(R.id.btnTreehousesDetect);
        docker = v.findViewById(R.id.btnDocker);
        expand = v.findViewById(R.id.btnExpand);
        vncOn = v.findViewById(R.id.btnVncOn);
        vncStatus = v.findViewById(R.id.btnVncStatus);
        clear = v.findViewById(R.id.btnClear);
    }
}
