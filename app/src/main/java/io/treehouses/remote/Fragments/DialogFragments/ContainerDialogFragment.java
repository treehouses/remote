package io.treehouses.remote.Fragments.DialogFragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;

import io.treehouses.remote.R;

public class ContainerDialogFragment extends DialogFragment {

    private static final String TAG = "ContainerDialogFragment";

    // Layout Views
    private Spinner mSpinner;

    public static ContainerDialogFragment newInstance(int num){

        //        Bundle bundle = new Bundle();
//        bundle.putInt("num", num);
//        dialogFragment.setArguments(bundle);

        return new ContainerDialogFragment();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.d(TAG,"In onCreateDialog()");

        // Build the dialog and set up the button click handlers
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View mView = inflater.inflate(R.layout.dialog_container,null);
        initLayoutView(mView);

        ArrayList<String> list = new ArrayList<String>();
        list.add("None");
        list.add("Docker");
        list.add("Balena");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(adapter);

        return getAlertDialog(mView);
    }

    private AlertDialog getAlertDialog(View mView) {
        return new AlertDialog.Builder(getActivity())
                .setView(mView)
                .setTitle(R.string.dialog_message)
                .setIcon(R.drawable.dialog_icon)
                .setPositiveButton(R.string.start_configuration,
                        (dialog, whichButton) -> {
                            String container = mSpinner.getSelectedItem().toString();
                            Intent intent = new Intent();
                            intent.putExtra("container",container);
                            intent.putExtra("type", "container");
                            getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
                        }
                )
                .setNegativeButton(R.string.cancel, (dialog, whichButton) -> getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_CANCELED, getActivity().getIntent()))
                .create();
    }
    private void initLayoutView(View mView) {
        mSpinner = mView.findViewById(R.id.spinner);
    }
}
