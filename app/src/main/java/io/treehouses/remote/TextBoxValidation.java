package io.treehouses.remote;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.ContentProvider;
import android.content.Context;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

/**
 * This class is the SSID and Password validator for the hotspot dialog
 */

public class TextBoxValidation extends DialogFragment{

    AlertDialog mDialog;
    EditText textWatcher;
    EditText SSID;
    EditText PWD;

    public void textboxValidation(@Nullable final EditText confirm, final Context context) {
            textWatcher.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                     if (SSID.length() == 0 ) {
                         dialogButtonTrueOrFalse(mDialog, false);
                         SSID.setError(context.getString(R.string.error_ssid_empty));
                     }else if (PWD.length() > 0 && PWD.length() < 8) {
                         dialogButtonTrueOrFalse(mDialog, false);
                         PWD.setError(context.getString(R.string.error_pwd_length));
                     }else if (PWD.length() >= 8 && confirm.getText().toString() == PWD.getText().toString()) {
                        dialogButtonTrueOrFalse(mDialog, true);
                     }else if (PWD.length() >=8 && !confirm.getText().toString().equals(PWD.getText().toString())) {
                         dialogButtonTrueOrFalse(mDialog, false);
                         confirm.setError(context.getString(R.string.error_pwd_confirm));
                     }else
                         dialogButtonTrueOrFalse(mDialog, true);
                }
                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }

    public void dialogButtonTrueOrFalse(AlertDialog mDialog, Boolean button){
        if (button){
            mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setClickable(true);
            mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
        }else if (!button){
            mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setClickable(false);
            mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        }
    }
    }

