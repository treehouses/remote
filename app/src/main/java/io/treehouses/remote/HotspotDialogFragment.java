package io.treehouses.remote;

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


/**
 * Created by Terrence on 3/12/2018.
 */

public class HotspotDialogFragment extends DialogFragment {

    private static final String TAG = "HotspotDialogFragment";

    protected EditText HSSIDEditText;
    protected EditText HPWDEditText;
    protected EditText HCPWDEditText;


    public static HotspotDialogFragment newInstance(int num) {
        HotspotDialogFragment hDialogFragment = new HotspotDialogFragment();
//        Bundle bundle = new Bundle();
//        bundle.putInt("num", num);
//        dialogFragment.setArguments(bundle);

        return hDialogFragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.d(TAG,"In onCreateDialog()");

        // Build the dialog and set up the button click handlers
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View mView = inflater.inflate(R.layout.hotspot_dialog,null);
        initLayoutView(mView);

        final AlertDialog mDialog = getAlertDialog(mView);
        mDialog.setTitle(R.string.dialog_message_hotspot);

        //initially disable button click
        getListener(mDialog);
        setTextChangeListener(mDialog);

        return mDialog;
    }

    @NonNull
    protected void getListener(final AlertDialog mDialog) {
        mDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                hDialogButtonTrueOrFalse(mDialog, false);
                HSSIDEditText.setError("Name your Hotspot");
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
                                String SSID = HSSIDEditText.getText().toString();
                                String PWD = HPWDEditText.getText().toString();
                                String CPWD = HCPWDEditText.getText().toString();

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
        hotspotTextWatcher(mDialog,HSSIDEditText);
        hotspotTextWatcher(mDialog,HPWDEditText);
        hotspotTextWatcher(mDialog,HCPWDEditText);
    }

    /**
     * This block checks for the input in the ssid textbox and the pwd textbox, and if requirements
     *are met the positive button will be enabled.
     */
    public void hotspotTextWatcher(final AlertDialog mDialog, final EditText textWatcher) {
        textWatcher.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (textWatcher.length() > 0 && textWatcher.length() < 8 && (textWatcher.getId() == HPWDEditText.getId())) {
                    hDialogButtonTrueOrFalse(mDialog, false);
                    HPWDEditText.setError(getString(R.string.error_pwd_length));
                }else if (textWatcher.length() == 0 && (textWatcher.getId() == HSSIDEditText.getId())) {
                    hDialogButtonTrueOrFalse(mDialog, false);
                    HSSIDEditText.setError(getString(R.string.error_ssid_empty));
                } else {
                    hDialogButtonTrueOrFalse(mDialog, true);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
                 if (HPWDEditText.getText().toString().equals(HCPWDEditText.getText().toString())) {
                    hDialogButtonTrueOrFalse(mDialog, true);
                }else {
                     hDialogButtonTrueOrFalse(mDialog, false);
                     HCPWDEditText.setError(getString(R.string.error_pwd_confirm));
                 }
            }
        });
    }

    public void hDialogButtonTrueOrFalse(AlertDialog mDialog, Boolean button){
        if (button){
            mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setClickable(true);
            mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
        }else if(!button){
            mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setClickable(false);
            mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        }
    }

    protected void initLayoutView(View mView) {
        HSSIDEditText = (EditText)mView.findViewById(R.id.HSSID);
        HPWDEditText = (EditText)mView.findViewById(R.id.Hpassword);
        HCPWDEditText = (EditText)mView.findViewById(R.id.HCpassword);

    }

}


