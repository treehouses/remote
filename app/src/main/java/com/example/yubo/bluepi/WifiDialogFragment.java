package com.example.yubo.bluepi;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

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

    // Layout Views
    private EditText mSSIDEditText;
    private EditText mPWDEditText;

    private boolean isIvalidInput;

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

        // Build the dialog and set up the button click handlers

        LayoutInflater inflater = getActivity().getLayoutInflater();
        AlertDialog mDialog = new AlertDialog.Builder(getActivity())
                .setView(inflater.inflate(R.layout.dialog_design,null))
                .setTitle(R.string.dialog_message)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(R.string.start_configuration,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                if(checkValidInput()){
                                    getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, getActivity().getIntent());
                                }
                            }
                        }
                )
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_CANCELED, getActivity().getIntent());
                    }
                })
                .create();

        initLayoutView();

        //mDialog.getButton(AlertDialog.BUTTON_POSITIVE)
        return mDialog;


    }

    private void initLayoutView() {
        isIvalidInput = true;
        mSSIDEditText = (EditText)getActivity().findViewById(R.id.SSID);
        mPWDEditText = (EditText)getActivity().findViewById(R.id.password);

    }

    private boolean checkValidInput(){
        if(mSSIDEditText.getText().toString().length() > 0){
            return true;
        }

        mSSIDEditText.setError(getString(R.string.error_ssid_empty));
        return false;

    }

}
