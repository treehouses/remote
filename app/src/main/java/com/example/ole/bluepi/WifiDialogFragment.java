package com.example.ole.bluepi;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.example.ole.bluepi.R;

/**
 * Created by yubo on 7/19/17.
 */

public class WifiDialogFragment extends DialogFragment {

    private static final String TAG = "WifiDialogFragment";

    // Layout Views
    private EditText mSSIDEditText;
    private EditText mPWDEditText;

    private boolean isValidInput;

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
        View mView = inflater.inflate(R.layout.dialog_design,null);
        initLayoutView(mView);

        final AlertDialog mDialog = new AlertDialog.Builder(getActivity())
                .setView(mView)
                .setTitle(R.string.dialog_message)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(R.string.start_configuration,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
//                                getActivity().getIntent().putExtra("isValidInput", mSSIDEditText.getText().toString().length() > 0? Boolean.TRUE: Boolean.FALSE);
                                if(isValidInput){
                                    String SSID = mSSIDEditText.getText().toString();
                                    String PWD = mPWDEditText.getText().toString();

                                    Intent intent = new Intent();
                                    intent.putExtra("SSID", SSID);
                                    intent.putExtra("PWD", PWD);
                                    getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
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

        //initially disable button click
        mDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                mDialog.getButton(DialogInterface.BUTTON_POSITIVE).setClickable(false);
                mDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                mSSIDEditText.setError(getString(R.string.error_ssid_empty));
            }
        });
        setTextChangeListener(mDialog);

        return mDialog;


    }

    private void setTextChangeListener(final AlertDialog mDialog) {

        mSSIDEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(TAG,"s.length() = " + s.length());
                if(s.length() > 0){
                    isValidInput = true;
                    mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setClickable(true);
                    mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }else{
                    isValidInput = false;
                    mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setClickable(false);
                    mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                    mSSIDEditText.setError(getString(R.string.error_ssid_empty));

                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void initLayoutView(View mView) {
        mSSIDEditText = (EditText)mView.findViewById(R.id.SSID);
        mPWDEditText = (EditText)mView.findViewById(R.id.password);

    }

}
