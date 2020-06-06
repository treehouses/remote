package io.treehouses.remote.Fragments.DialogFragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import io.treehouses.remote.Fragments.TextBoxValidation;
import io.treehouses.remote.R;
import io.treehouses.remote.databinding.ChpassDialogBinding;

/**
 * Created by going-gone on 4/19/2018.
 */

public class ChPasswordDialogFragment extends androidx.fragment.app.DialogFragment {

    private static String TAG = "ChPasswordDialogFragment";

//    private EditText passwordEditText;
//    private EditText confirmPassEditText;

    ChpassDialogBinding bind;
    private TextBoxValidation textBoxValidation = new TextBoxValidation();

    public static ChPasswordDialogFragment newInstance() {
        return new ChPasswordDialogFragment();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.d(TAG,"In onCreateDialog()");
        bind = ChpassDialogBinding.inflate(getActivity().getLayoutInflater());

        final AlertDialog mDialog = getAlertDialog(bind.getRoot());
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
                            String chPass = bind.changePassword.getText().toString();

                            Intent i = new Intent();
                            i.putExtra("type", "chPass");
                            i.putExtra("password", chPass);
                            getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, i);
                        }
                )
                .setNegativeButton(R.string.cancel, (dialog, whichButton) -> getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_CANCELED, getActivity().getIntent()))
                .create();
    }

    //listener for text change within this dialog
    private void setTextChangeListener(final AlertDialog mDialog) {
        textBoxValidation.setmDialog(mDialog);
        textBoxValidation.setTextWatcher(bind.changePassword);
        textBoxValidation.PWD = bind.changePassword;
        textBoxValidation.changePWValidation(bind.confirmPassword, getActivity());
    }
}
