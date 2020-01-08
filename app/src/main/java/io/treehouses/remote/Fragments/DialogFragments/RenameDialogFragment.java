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

import androidx.fragment.app.DialogFragment;

import io.treehouses.remote.Fragments.TextBoxValidation;
import io.treehouses.remote.R;

public class RenameDialogFragment extends DialogFragment {
    private static final String TAG = "RenameDialogFragment";

    // Layout Views
    protected EditText mHostNameEditText;
    TextBoxValidation textboxValidation = new TextBoxValidation();

    public static RenameDialogFragment newInstance(int num){

        RenameDialogFragment dialogFragment = new RenameDialogFragment();
//        Bundle bundle = new Bundle();
//        bundle.putInt("num", num);
//        dialogFragment.setArguments(bundle);

        return dialogFragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.d(TAG,"In onCreateDialog()");

        // Build the dialog and set up the button click handlers
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View mView = inflater.inflate(R.layout.dialog_rename,null);
        initLayoutView(mView);

        final AlertDialog mDialog = getAlertDialog(mView);

        //initially disable button click
        textboxValidation.getListener(mDialog);
        setTextChangeListener(mDialog);

        return mDialog;
    }

    protected AlertDialog getAlertDialog(View mView) {
        return new AlertDialog.Builder(getActivity())
                .setView(mView)
                .setTitle(R.string.dialog_message)
                .setMessage(R.string.rename_message)
                .setIcon(R.drawable.dialog_icon)
                .setPositiveButton(R.string.start_configuration,
                        (dialog, whichButton) -> {
                            Intent intent = new Intent();
                            String hostname = mHostNameEditText.getText().toString();
                            intent.putExtra("hostname", hostname);
                            intent.putExtra("type", "rename");
                            getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
                        }
                )
                .setNegativeButton(R.string.cancel, (dialog, whichButton) -> getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_CANCELED, getActivity().getIntent()))
                .create();
    }

    private void setTextChangeListener(final AlertDialog mDialog) {
        textboxValidation.setmDialog(mDialog);
        textboxValidation.setTextWatcher(mHostNameEditText);
        textboxValidation.setSSID(mHostNameEditText);
        textboxValidation.textboxValidation(getActivity(), "rename");
    }

    private void initLayoutView(View mView) {
        mHostNameEditText = mView.findViewById(R.id.hostname);
    }
}
