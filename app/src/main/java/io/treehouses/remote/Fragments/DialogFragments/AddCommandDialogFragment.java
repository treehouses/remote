package io.treehouses.remote.Fragments.DialogFragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import io.treehouses.remote.R;
import io.treehouses.remote.databinding.DialogAddCommandBinding;
import io.treehouses.remote.pojo.CommandListItem;
import io.treehouses.remote.utils.SaveUtils;


public class AddCommandDialogFragment extends androidx.fragment.app.DialogFragment {

    private static String TAG = "AddCommandDialogFragment";

//    private EditText commandTitle;
//    private EditText commandValue;

    DialogAddCommandBinding bind;
    public static AddCommandDialogFragment newInstance() {
        return new AddCommandDialogFragment();
    }

    @Override @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        bind = DialogAddCommandBinding.inflate(getActivity().getLayoutInflater());

        final AlertDialog mDialog = getAlertDialog(bind.getRoot());
        mDialog.setTitle(R.string.add_command_title);

        return mDialog;
    }

    private AlertDialog getAlertDialog(View mView) {
        return new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.CustomAlertDialogStyle))
                .setView(mView)
                .setTitle(R.string.change_password)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Add Command", (dialog, which) -> {
                    if (bind.commandName.getText().toString().length() > 0 && bind.commandValue.getText().toString().length() > 0) {
                        SaveUtils.addToCommandsList(getContext(),
                                new CommandListItem(bind.commandName.getText().toString(), bind.commandValue.getText().toString()));
                        done();
                        dismiss();
                    }
                    else {
                        Toast.makeText(getContext(), "Please Enter Text", Toast.LENGTH_LONG).show();
                    }
                }
                )
                .setNegativeButton(R.string.cancel, (dialog, which) -> dismiss()).create();
    }

    private void done() {
        Intent intent = new Intent();
        intent.putExtra("done",true);
        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
    }

}
