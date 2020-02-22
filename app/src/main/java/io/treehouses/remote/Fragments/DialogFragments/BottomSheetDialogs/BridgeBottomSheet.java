package io.treehouses.remote.Fragments.DialogFragments.BottomSheetDialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import io.treehouses.remote.R;

public class BridgeBottomSheet extends BottomSheetDialogFragment {
    @Nullable
    @Override

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_bridge, container, false);
        return v;
    }
}
