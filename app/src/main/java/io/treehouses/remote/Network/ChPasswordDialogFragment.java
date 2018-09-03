package io.treehouses.remote.Network;

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
import android.widget.EditText;

import io.treehouses.remote.R;

/**
 * Created by going-gone on 4/19/2018.
 */

public class ChPasswordDialogFragment extends android.support.v4.app.DialogFragment {

    private static String TAG = "ChPasswordDialogFragment";

    protected EditText passwordEditText;
    protected EditText confirmPassEditText;
    TextBoxValidation textBoxValidation = new TextBoxValidation();

    public static ChPasswordDialogFragment newInstance(int num) {
        ChPasswordDialogFragment chPassDialogFragment = new ChPasswordDialogFragment();
        return chPassDialogFragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.d(TAG,"In onCreateDialog()");

        // Build the dialog and set up the button click handlers
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View mView = inflater.inflate(R.layout.chpass_dialog,null);
        initLayoutView(mView);

        final AlertDialog mDialog = getAlertDialog(mView);
        mDialog.setTitle(R.string.change_password);

        //initially disable button click
        textBoxValidation.getListener(mDialog);
        setTextChangeListener(mDialog);

        return mDialog;
    }
    //creates the dialog for the change password dialog
    protected AlertDialog getAlertDialog(View mView) {
        return new AlertDialog.Builder(getActivity())
                .setView(mView)
                .setTitle(R.string.change_password)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(R.string.change_password,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                                String chPass = passwordEditText.getText().toString();

                                Intent i = new Intent();
                                i.putExtra("password", chPass);
                                getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, i);
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
    //initialize the view
    protected void initLayoutView(View mView) {
        passwordEditText = (EditText)mView.findViewById(R.id.changePassword);
        confirmPassEditText = (EditText)mView.findViewById(R.id.confirmPassword);

    }
    //listener for text change within this dialog
    public void setTextChangeListener(final AlertDialog mDialog) {
        textBoxValidation.mDialog = mDialog;
        textBoxValidation.textWatcher = passwordEditText;
        textBoxValidation.PWD = passwordEditText;
        textBoxValidation.changePWValidation(confirmPassEditText, getContext());

        textBoxValidation.mDialog = mDialog;
        textBoxValidation.textWatcher = confirmPassEditText;
        textBoxValidation.PWD = passwordEditText;
        textBoxValidation.changePWValidation(confirmPassEditText, getContext());

    }
}
