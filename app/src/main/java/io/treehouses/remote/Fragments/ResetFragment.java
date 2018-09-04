package io.treehouses.remote.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import io.treehouses.remote.R;

public class ResetFragment extends DialogFragment{


    public static ResetFragment newInstance(int num) {
        ResetFragment resetFragment = new ResetFragment();
        return resetFragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Build the dialog and set up the button click handlers
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View mView = inflater.inflate(R.layout.dialog_reset,null);
        final AlertDialog mDialog = getAlertDialog(mView);
        return mDialog;
    }

    protected AlertDialog getAlertDialog(View mView) {
        return new AlertDialog.Builder(getActivity())
                .setView(mView)
                .setTitle(R.string.dialog_message)
                .setIcon(R.drawable.dialog_icon)
                .setPositiveButton("Reset",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                Intent intent = new Intent();
                                getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
                            }
                        }
                )
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_CANCELED, getActivity().getIntent());
                    }
                })
                .create();
    }

}
