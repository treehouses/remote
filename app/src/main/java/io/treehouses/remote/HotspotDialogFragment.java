package io.treehouses.remote;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

/**
 * Created by Terrence on 3/12/2018.
 */

public class HotspotDialogFragment extends WifiDialogFragment {

    private static final String TAG = "HotspotDialogFragment";


    public static HotspotDialogFragment newInstance(int num) {
        HotspotDialogFragment hDialogFragment = new HotspotDialogFragment();
//        Bundle bundle = new Bundle();
//        bundle.putInt("num", num);
//        dialogFragment.setArguments(bundle);

        return hDialogFragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.d(TAG,"In onCreateDialog()");

        // Build the dialog and set up the button click handlers
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View mView = inflater.inflate(R.layout.dialog_design,null);
        initLayoutView(mView);

        final AlertDialog mDialog = getAlertDialog(mView);
        mDialog.setTitle(R.string.dialog_message_hotspot);

        //initially disable button click
        getListener(mDialog);
        setTextChangeListener(mDialog);

        return mDialog;
    }
}


