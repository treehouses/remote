package io.treehouses.remote.Fragments.DialogFragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import io.treehouses.remote.Fragments.TextBoxValidation;
import io.treehouses.remote.R;

/**
 * Created by going-gone on 4/19/2018.
 */

public class ChPasswordDialogFragment extends androidx.fragment.app.DialogFragment {

    private static String TAG = "ChPasswordDialogFragment";

    private EditText passwordEditText;
    private EditText confirmPassEditText;
    private TextBoxValidation textBoxValidation = new TextBoxValidation();

    public static ChPasswordDialogFragment newInstance() {
        ChPasswordDialogFragment chPassDialogFragment = new ChPasswordDialogFragment();
        return chPassDialogFragment;
    }

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
                        (dialog, whichButton) -> {
                            dialog.dismiss();
                            String chPass = passwordEditText.getText().toString();

                            Intent i = new Intent();
                            i.putExtra("type","chPass");
                            i.putExtra("password", chPass);
                            getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, i);
                        }
                )
                .setNegativeButton(R.string.cancel, (dialog, whichButton) -> getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_CANCELED, getActivity().getIntent()))
                .create();
    }
    //initialize the view
    private void initLayoutView(View mView) {
        passwordEditText = mView.findViewById(R.id.changePassword);
        confirmPassEditText = mView.findViewById(R.id.confirmPassword);
    }

    //listener for text change within this dialog
    private void setTextChangeListener(final AlertDialog mDialog) {
        textBoxValidation.setmDialog(mDialog);
        textBoxValidation.setTextWatcher(passwordEditText);
        textBoxValidation.setPWD(passwordEditText);
        textBoxValidation.changePWValidation(confirmPassEditText, getActivity());
    }
}
