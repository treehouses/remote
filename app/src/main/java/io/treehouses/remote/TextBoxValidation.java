package io.treehouses.remote;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

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
                    if (textWatcher.length() > 0 && textWatcher.length() < 8 && (textWatcher.getId() == PWD.getId())) {
                        dialogButtonTrueOrFalse(mDialog, false);
                        PWD.setError(context.getString(R.string.error_pwd_length));
                    }else if (textWatcher.length() == 0 && (textWatcher.getId() == SSID.getId())) {
                        dialogButtonTrueOrFalse(mDialog, false);
                        SSID.setError(context.getString(R.string.error_ssid_empty));
                    } else {
                        dialogButtonTrueOrFalse(mDialog, true);
                    }
                }
                @Override
                public void afterTextChanged(Editable s) {
                    if (confirm.getText().toString().equals(PWD.getText().toString()) && (confirm.getText().length() == 0)) {
                       dialogButtonTrueOrFalse(mDialog, true);
                    }
                    else if ((confirm.getText().toString().equals(PWD.getText().toString()) && (confirm.getText().length() >= 8))){
                        dialogButtonTrueOrFalse(mDialog, true);
                    } else {
                        dialogButtonTrueOrFalse(mDialog, false);
                        confirm.setError(context.getString(R.string.error_pwd_confirm));
                    }
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

