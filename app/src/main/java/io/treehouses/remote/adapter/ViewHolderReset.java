package io.treehouses.remote.adapter;

import android.view.View;
import android.widget.Button;

import io.treehouses.remote.Fragments.NetworkFragment;
import io.treehouses.remote.R;

public class ViewHolderReset {

    private Button btnReset;

    public ViewHolderReset(final View v) {
        btnReset = v.findViewById(R.id.btnReset);
        btnReset.setText("Reset Network");
        v.findViewById(R.id.btnReset).setOnClickListener(view -> NetworkFragment.getInstance().showAlertDialog("Are you sure you want to reset the network to default?", "Reset"));
    }
}
