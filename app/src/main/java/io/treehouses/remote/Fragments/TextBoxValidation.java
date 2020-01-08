package io.treehouses.remote.Fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import io.treehouses.remote.R;

/**
 * This class is the hotspot and wifi dialog validator
 */

public class TextBoxValidation extends androidx.fragment.app.DialogFragment {

    private AlertDialog mDialog;
    private EditText textWatcher;
    private EditText SSID;
    private EditText PWD;

    //TODO: this file needs to be refactored and maybe make it usable for the future
    private EditText IpAddressEditText;
    private EditText MaskEditText;
    private EditText GateWayEditText;
    private EditText DNSEditText;
    private EditText ESSIDEditText;
    private EditText HotspotESSIDEditText;
    EditText PasswordEditText;
    EditText HotspotPasswordEditText;

    public EditText getSSID() {
        return SSID;
    }

    public void setmDialog(AlertDialog mDialog) {
        this.mDialog = mDialog;
    }

    public void setTextWatcher(EditText textWatcher) {
        this.textWatcher = textWatcher;
    }

    public void setSSID(EditText SSID) {
        this.SSID = SSID;
    }

    public void setPWD(EditText PWD) {
        this.PWD = PWD;
    }


    /**
     * Textwatcher for most dialogs
     *
     */
    public void textboxValidation(final Context context, final String type) {
        textWatcher.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(type.equals("ethernet")){
                    validateETHERNET(context);
                }else if(type.equals("wifi")){
                    validateWIFI(context);
                }else if(type.equals("bridge")){
                    validateBridge(context);
                }
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
     * ETHERNET dialog validator
     *
     */
    private void validateETHERNET(final Context context) {
        if (IpAddressEditText.length() == 0 || MaskEditText.length() == 0 || GateWayEditText.length() == 0 || DNSEditText.length() == 0) {
            dialogButtonTrueOrFalse(mDialog, false);
        }else {
            dialogButtonTrueOrFalse(mDialog,true);
        }
    }

    /**
     * ETHERNET dialog validator
     *
     */
    private void validateBridge(final Context context) {
        if (ESSIDEditText.length() == 0 || HotspotESSIDEditText.length() == 0) {

            dialogButtonTrueOrFalse(mDialog, false);
            //PasswordEditText.setError(context.getString(R.string.error_pwd_length));
            //HotspotPasswordEditText.setError(context.getString(R.string.error_pwd_length));
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

    public void getListener(final AlertDialog mDialog) {
        mDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                dialogButtonTrueOrFalse(mDialog, false);
            }
        });
    }
    }

