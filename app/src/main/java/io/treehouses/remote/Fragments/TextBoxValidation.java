package io.treehouses.remote.Fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputLayout;

import io.treehouses.remote.R;

/**
 * This class is the hotspot and wifi dialog validator
 */

public class TextBoxValidation {

    private AlertDialog mDialog;
    private EditText textWatcher;
    private EditText SSID;
    private EditText PWD;
    private Button start, addprofile;
    private TextInputLayout textInputLayout;

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

    public TextBoxValidation(Context context, EditText e1, EditText e2, String type) {
        if (type.equals("wifi")) {
            setSSID(e1);
            setPWD(e2);
            textboxValidation(context, type, e1);
            textboxValidation(context, type, e2);
        }
        else if (type.equals("bridge")) {
            this.ESSIDEditText = e1;
            this.HotspotESSIDEditText = e2;
            textboxValidation(context, type, ESSIDEditText);
            textboxValidation(context, type, HotspotESSIDEditText);
        }
    }

    public void setStart(Button start) {
        this.start = start;
    }

    public void setAddprofile(Button addprofile) {
        this.addprofile = addprofile;
    }

    public void setTextInputLayout(TextInputLayout textInputLayout) {
        this.textInputLayout = textInputLayout;
    }

    public TextBoxValidation() {

    }


    /**
     * Textwatcher for most dialogs
     *
     */
    public void textboxValidation(final Context context, final String type, final EditText toWatch) {
        toWatch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

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
            public void afterTextChanged(Editable editable) { }
        });
    }

    /**
     * Textwatcher for the change password dialog
     *
     */
    public void changePWValidation(final EditText confirmPWD, final Context context) {
        textWatcher.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                validateChangedPassword(confirmPWD, context);
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        });
    }

    /**
     * Method that sets the dialog positive button to true or false
     */
    public void dialogButtonTrueOrFalse(AlertDialog mDialog, Boolean button){
        if (mDialog == null) return;
        mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setClickable(button);
        mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(button);
    }

    public void dialogButtonTrueOrFalse(Button button1, Button button2, boolean enabled){
        if (button1 != null) {
            button1.setClickable(enabled);
            button1.setEnabled(enabled);
        }
        if (button2 != null) {
            button2.setEnabled(enabled);
            button2.setEnabled(enabled);
        }
    }


    /**
     * WiFi dialog validator
     *
     */
    private void validateWIFI(Context context) {
        boolean flag = true;
        if (SSID.length() == 0 ) {
            dialogButtonTrueOrFalse(start, addprofile, false);
            SSID.setError(context.getString(R.string.error_ssid_empty));
            flag = false;
        }
        if (PWD.length() > 0 && PWD.length() < 8) {
            dialogButtonTrueOrFalse(start,addprofile, false);
            textInputLayout.setError(context.getString(R.string.error_pwd_length));
            flag = false;
        }
        if (flag){
            dialogButtonTrueOrFalse(start, addprofile,true);
            textInputLayout.setError(null);
        }
    }

    /**
     * ETHERNET dialog validator
     *
     */
    private void validateETHERNET(final Context context) {
        if (IpAddressEditText.length() == 0 || MaskEditText.length() == 0 || GateWayEditText.length() == 0 || DNSEditText.length() == 0) {
            dialogButtonTrueOrFalse(start,addprofile, false);
        }else {
            dialogButtonTrueOrFalse(start, addprofile,true);
        }
    }

    /**
     * ETHERNET dialog validator
     *
     */
    private void validateBridge(final Context context) {
        boolean flag = true;
        if (ESSIDEditText.length() == 0) {
            flag = false;
            ESSIDEditText.setError("This field cannot be empty");
        }
        if (HotspotESSIDEditText.length() == 0) {
            flag = false;
            HotspotESSIDEditText.setError("This field cannot be empty");
        }
        dialogButtonTrueOrFalse(start, addprofile, flag);
    }

    /**
     * Change password validator
     *
     */
    private void validateChangedPassword(final EditText confirmPWD, final Context context) {
        if (confirmPWD.getText().toString().equals(PWD.getText().toString())) {
            dialogButtonTrueOrFalse(mDialog, true);
        } else if (!confirmPWD.getText().toString().equals(PWD.getText().toString())) {
            dialogButtonTrueOrFalse(mDialog, false);
            confirmPWD.setError(context.getString(R.string.error_pwd_confirm));
        } else {
            dialogButtonTrueOrFalse(mDialog, true);
        }
    }

    public void getListener(final AlertDialog mDialog) {
        mDialog.setOnShowListener(dialog -> dialogButtonTrueOrFalse(mDialog, false));
    }
    }

