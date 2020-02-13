package io.treehouses.remote.utils;

import android.graphics.Color;
import android.text.Editable;
import android.util.Log;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputEditText;

import io.treehouses.remote.Fragments.NetworkFragment;
import io.treehouses.remote.R;
import io.treehouses.remote.adapter.NetworkListAdapter;
import io.treehouses.remote.adapter.ViewHolderBridge;


public class TextWatcherUtils extends ViewHolderBridge implements android.text.TextWatcher {
    EditText editText;
    ButtonConfiguration buttonConfiguration;

    public TextWatcherUtils(EditText editText) {
        this.editText = editText;
        buttonConfiguration = NetworkFragment.getInstance().getButtonConfiguration();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        if (NetworkListAdapter.position == 0) {             // ethernet text listener
            ethernetLayout(editable, editText);
        } else if (NetworkListAdapter.position == 1) {          // wifi text listener
            wifiLayout(editable, editText);
        } else if (NetworkListAdapter.position == 2) {       // hotspot text listener
            hotspotLayout(editable, editText);
        } else if (NetworkListAdapter.position == 3) {        // bridge text listener
            bridgeLayout(editable, editText);
        }
        Log.e("TAG", "afterTextChanged()");
    }

    void wifiLayout(Editable editable, EditText editText) {
        if (viewCondition(etSsid, editable)) {
            textChanged(length(editText));
        }
    }

    void hotspotLayout(Editable editable, EditText editText) {
        if (viewCondition(etSsid, editable)) {
            textChanged(length(editText));
        }
    }

    void bridgeLayout(Editable editable, EditText editText) {
        if (viewCondition(essid, editable) || viewCondition(etHotspotEssid, editable)) {
            textChanged(length(editText) && length(essid) && length(etHotspotEssid));
        }
    }

    void ethernetLayout(Editable editable, EditText editText) {
        if (checkCondition(editable)) {
            textChanged(length(editText) && length(etIp) && length(etDNS));
        }
    }

    private boolean checkCondition(Editable editable) {
        return viewCondition(etIp, editable) || viewCondition(etDNS, editable) || viewCondition(etGateway, editable) || viewCondition(etMask, editable);
    }


    private void textChanged(boolean condition) {
        if (condition) {
            buttonProperties(true, Color.WHITE, buttonConfiguration.btnStartConfiguration);
        } else {
            buttonProperties(false, Color.LTGRAY, buttonConfiguration.btnStartConfiguration);
        }
    }

    private Boolean viewCondition(TextInputEditText editText, Editable editable) {
        if (editText != null && editable != null && !editText.getText().toString().isEmpty()) {
            return editable == editText.getEditableText() && !messageSent;
        }
        return false;
    }

    private Boolean length(EditText editText) {
        return editText.length() > 0;
    }

}


