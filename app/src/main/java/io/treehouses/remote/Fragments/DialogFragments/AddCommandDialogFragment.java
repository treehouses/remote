package io.treehouses.remote.Fragments.DialogFragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import io.treehouses.remote.R;
import io.treehouses.remote.pojo.CommandListItem;
import io.treehouses.remote.utils.SaveUtils;


public class AddCommandDialogFragment extends androidx.fragment.app.DialogFragment {

    private static String TAG = "AddCommandDialogFragment";

    private EditText commandTitle;
    private EditText commandValue;

    public static AddCommandDialogFragment newInstance() {
        return new AddCommandDialogFragment();
    }

    @Override @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View mView = inflater.inflate(R.layout.dialog_add_command,null);
        initLayoutView(mView);

        final AlertDialog mDialog = getAlertDialog(mView);
        mDialog.setTitle(R.string.add_command_title);

        return mDialog;
    }

    private AlertDialog getAlertDialog(View mView) {
        return new AlertDialog.Builder(getActivity())
                .setView(mView)
                .setTitle(R.string.change_password)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Add Command", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (commandTitle.getText().toString().length() > 0 && commandValue.getText().toString().length() > 0) {
                                    SaveUtils.addToCommandsList(getContext(),
                                            new CommandListItem(commandTitle.getText().toString(), commandValue.getText().toString()));
                                    done();
                                    dismiss();
                                }
                                else {
                                    Toast.makeText(getContext(), "Please Enter Text", Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                )
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                }).create();
    }

    private void done() {
        Intent intent = new Intent();
        intent.putExtra("done",true);
        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
    }

    //initialize views
    private void initLayoutView(View mView) {
        commandTitle = mView.findViewById(R.id.commandName);
        commandValue = mView.findViewById(R.id.commandValue);
    }

}
