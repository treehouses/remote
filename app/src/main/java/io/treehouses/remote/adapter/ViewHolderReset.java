package io.treehouses.remote.adapter;

import android.view.View;
import android.widget.Button;

import io.treehouses.remote.Fragments.NetworkFragment;
import io.treehouses.remote.R;
import io.treehouses.remote.callback.HomeInteractListener;

public class ViewHolderReset {

    private Button btnReset;

    public ViewHolderReset(final View v, final HomeInteractListener listener) {
        btnReset = v.findViewById(R.id.btnReset);
        btnReset.setText("Reset Network");
        v.findViewById(R.id.btnReset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NetworkFragment.getInstance().showAlertDialog("Are you sure you want to reset the network to default?", "Reset");
            }
        });
    }
}
