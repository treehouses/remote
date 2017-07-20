package com.example.yubo.bluepi;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;

/**
 * Created by yubo on 7/19/17.
 */

public class WifiDialogFragment extends DialogFragment {

    private static final String TAG = "WifiDialogFragment";

//    /* The activity that creates an instance of this dialog fragment must
//    * implement this interface in order to receive event callbacks.
//    * Each method passes the DialogFragment in case the host needs to query it. */
//    public interface WifiDialogListener {
//        public void onDialogPositiveClick(DialogFragment dialog);
//        public void onDialogNegativeClick(DialogFragment dialog);
//    }

//    // Use this instance of the interface to deliver action events
//    WifiDialogListener mListener;


    public static WifiDialogFragment newInstance(int num){

        WifiDialogFragment dialogFragment = new WifiDialogFragment();
//        Bundle bundle = new Bundle();
//        bundle.putInt("num", num);
//        dialogFragment.setArguments(bundle);

        return dialogFragment;

    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.d(TAG,"In onCreateDialog()");
//
//        // Build the dialog and set up the button click handlers
//        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
////        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//        builder.setMessage(R.string.dialog_message)
//                .setPositiveButton(R.string.start_configuration, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        // Send the positive button event back to the host activity
//                        mListener.onDialogPositiveClick(WifiDialogFragment.this);
//                    }
//                })
//                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        // Send the negative button event back to the host activity
//                        mListener.onDialogNegativeClick(WifiDialogFragment.this);
//                    }
//                });
//        return builder.create();
        LayoutInflater inflater = getActivity().getLayoutInflater();
        return new AlertDialog.Builder(getActivity())
                .setView(inflater.inflate(R.layout.dialog_design,null))
                .setTitle(R.string.dialog_message)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(R.string.start_configuration,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, getActivity().getIntent());
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

//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        Log.d(TAG,"In onAttach()");
//        // Verify that the host activity implements the callback interface
//        try {
//            // Instantiate the WifiDialogListener so we can send events to the host
//            mListener = (WifiDialogListener) getParentFragment();
//            Log.d(TAG,"In onAttach(), mListener = " + mListener);
//
//        } catch (ClassCastException e) {
//            // The activity doesn't implement the interface, throw exception
//            throw new ClassCastException(getActivity().toString()
//                    + " must implement WifiDialogListener");
//        }
//    }

//    @Override
//    public void onAttachFragment(Fragment childFragment) {
//        super.onAttachFragment(childFragment);
//        Log.d(TAG,"In onAttachFragment()");
//
//        // Verify that the host activity implements the callback interface
//        try {
//            // Instantiate the WifiDialogListener so we can send events to the host
//            mListener = (WifiDialogListener) childFragment;
//            Log.d(TAG,"In onAttach(), mListener = " + mListener);
//
//        } catch (ClassCastException e) {
//            // The activity doesn't implement the interface, throw exception
//            throw new ClassCastException(getActivity().toString()
//                    + " must implement WifiDialogListener");
//        }
//    }
}
