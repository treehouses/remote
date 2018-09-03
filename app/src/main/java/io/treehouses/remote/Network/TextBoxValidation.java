package io.treehouses.remote.Network;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import io.treehouses.remote.R;

/**
 * This class is the hotspot and wifi dialog validator
 */

public class TextBoxValidation extends DialogFragment{

    AlertDialog mDialog;
    EditText textWatcher;
    EditText SSID;
    EditText PWD;

    /**
     * Textwatcher method for the hotspot dialog
     */
    public void hotspotTextboxValidation(@Nullable final EditText confirmPWD, final Context context) {
            textWatcher.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    validateHotspot(confirmPWD, context);
                }
                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }

    /**
     * Textwatcher for the WiFi dialog
     *
     */
    public void wifiTextboxValidation(final Context context) {
        textWatcher.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                validateWIFI(context);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    /**
     * Textwatcher for the change password dialog
     *
     */
    public void changePWValidation(final EditText confirmPWD, final Context context) {
        textWatcher.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                validateChangedPassword(confirmPWD, context);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    /**
     * Method that sets the dialog positive button to true or false
     */
    public void dialogButtonTrueOrFalse(AlertDialog mDialog, Boolean button){
        if (button){
            mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setClickable(true);
            mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
        }else if (!button){
            mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setClickable(false);
            mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        }
    }

    /**
     * Hotspot dialog validator
     *
     */
    private void validateHotspot(final EditText confirmPWD, final Context context) {
        if (SSID.length() == 0) {
            dialogButtonTrueOrFalse(mDialog, false);
            SSID.setError(context.getString(R.string.error_ssid_empty));
        } else if (PWD.length() > 0 && PWD.length() < 8) {
            dialogButtonTrueOrFalse(mDialog, false);
            PWD.setError(context.getString(R.string.error_pwd_length));
        } else if (PWD.length() >= 8 && confirmPWD.getText().toString() == PWD.getText().toString()) {
            dialogButtonTrueOrFalse(mDialog, true);
        } else if (PWD.length() >= 8 && !confirmPWD.getText().toString().equals(PWD.getText().toString())) {
            dialogButtonTrueOrFalse(mDialog, false);
            confirmPWD.setError(context.getString(R.string.error_pwd_confirm));
        } else {
            dialogButtonTrueOrFalse(mDialog, true);
        }
    }

    /**
     * WiFi dialog validator
     *
     */
    private void validateWIFI(final Context context) {
        if (SSID.length() == 0 ) {
            dialogButtonTrueOrFalse(mDialog, false);
            SSID.setError(context.getString(R.string.error_ssid_empty));
        }else if (PWD.length() > 0 && PWD.length() < 8) {
            dialogButtonTrueOrFalse(mDialog, false);
            PWD.setError(context.getString(R.string.error_pwd_length));
        }else {
            dialogButtonTrueOrFalse(mDialog,true);
        }
    }

    /**
     * Change password validator
     *
     */
    private void validateChangedPassword(final EditText confirmPWD, final Context context) {
        if (confirmPWD.getText().toString() == PWD.getText().toString()) {
            dialogButtonTrueOrFalse(mDialog, true);
        } else if (!confirmPWD.getText().toString().equals(PWD.getText().toString())) {
            dialogButtonTrueOrFalse(mDialog, false);
            confirmPWD.setError(context.getString(R.string.error_pwd_confirm));
        } else {
            dialogButtonTrueOrFalse(mDialog, true);
        }
    }

    protected void getListener(final AlertDialog mDialog) {
        mDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                dialogButtonTrueOrFalse(mDialog, false);
            }
        });
    }
    }

