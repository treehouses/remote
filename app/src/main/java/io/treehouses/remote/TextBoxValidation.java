package io.treehouses.remote;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;

public class TextBoxValidation extends DialogFragment{

    public void textboxValidation(final AlertDialog mDialog, final EditText textWatcher, final EditText SSID, final EditText PWD, @Nullable final EditText confirm, final Context context) {
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
                    Log.e("Button", "Confirmed PWD");
                    if (confirm.getText().toString().equals(PWD.getText().toString())) {
                       dialogButtonTrueOrFalse(mDialog, true);
                    }else {
                        Log.e("Button", "Not Confirmed PWD");
                        dialogButtonTrueOrFalse(mDialog, false);
                        confirm.setError(context.getString(R.string.error_pwd_confirm));
                    }
                }
            });
        }

    public void dialogButtonTrueOrFalse(AlertDialog mDialog, Boolean button){
        if (button){
            Log.e("Button", "Enabled button");
            mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setClickable(true);
            mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
        }else if(!button){
            Log.e("Button", "Disabled button");
            mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setClickable(false);
            mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        }
    }
    }

