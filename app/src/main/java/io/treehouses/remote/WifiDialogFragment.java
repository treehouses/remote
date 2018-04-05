package io.treehouses.remote;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

/**
 * Created by yubo on 7/19/17.
 */

public class WifiDialogFragment extends DialogFragment {

    private static final String TAG = "WifiDialogFragment";

    // Layout Views
    protected EditText mSSIDEditText;
    protected EditText mPWDEditText;
    TextBoxValidation TBV = new TextBoxValidation();

    protected boolean isValidInput;

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

        final AlertDialog mDialog = getAlertDialog(mView);

        //initially disable button click
        getListener(mDialog);
        setTextChangeListener(mDialog);

        return mDialog;


    }

    protected void getListener(final AlertDialog mDialog) {
        mDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                TBV.dialogButtonTrueOrFalse(mDialog, false);
                mSSIDEditText.setError(getString(R.string.error_ssid_empty));
            }
        });

    }

    protected AlertDialog getAlertDialog(View mView) {
        return new AlertDialog.Builder(getActivity())
                    .setView(mView)
                    .setTitle(R.string.dialog_message)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(R.string.start_configuration,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
    //                                getActivity().getIntent().putExtra("isValidInput", mSSIDEditText.getText().toString().length() > 0? Boolean.TRUE: Boolean.FALSE);
                                        String SSID = mSSIDEditText.getText().toString();
                                        String PWD = mPWDEditText.getText().toString();

                                        Intent intent = new Intent();
                                        intent.putExtra("SSID", SSID);
                                        intent.putExtra("PWD", PWD);
                                        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
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

    public void setTextChangeListener(final AlertDialog mDialog) {
        TBV.textboxValidation(mDialog, mSSIDEditText,mSSIDEditText, mPWDEditText, null, getContext());
        TBV.textboxValidation(mDialog, mPWDEditText,mSSIDEditText, mPWDEditText, null, getContext());
    }

    /**
     * This block checks for the input in the ssid textbox and the pwd textbox, and if requirements
     *are met the positive button will be enabled.
     */




    protected void initLayoutView(View mView) {
        mSSIDEditText = (EditText)mView.findViewById(R.id.SSID);
        mPWDEditText = (EditText)mView.findViewById(R.id.password);

    }

}

