package io.treehouses.remote.Fragments.DialogFragments.BottomSheetDialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import io.treehouses.remote.R;
import io.treehouses.remote.bases.BaseBottomSheetDialog;

public class TorBottomSheet extends BaseBottomSheetDialog {

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_tor, container, false);
        String[] mobileArray = {"Android","IPhone","WindowsMobile","Blackberry",
                "WebOS","Ubuntu","Windows7","Max OS X"};

        return v;
    }
}