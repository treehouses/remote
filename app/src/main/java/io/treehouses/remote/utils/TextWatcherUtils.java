package io.treehouses.remote.utils;

import android.text.Editable;
import android.util.Log;
import android.widget.EditText;

import io.treehouses.remote.Fragments.NetworkFragment;
import io.treehouses.remote.R;
import io.treehouses.remote.adapter.NetworkListAdapter;


public class TextWatcherUtils implements android.text.TextWatcher {
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
        if (NetworkListAdapter.getLayout() == R.layout.dialog_ethernet) {             // ethernet text listener
            buttonConfiguration.ethernetLayout(editable, editText);
        } else if (NetworkListAdapter.getLayout() == R.layout.dialog_wifi) {          // wifi text listener
            buttonConfiguration.wifiLayout(editable, editText);
        } else if (NetworkListAdapter.getLayout() == R.layout.dialog_hotspot) {       // hotspot text listener
            buttonConfiguration.hotspotLayout(editable, editText);
        } else if (NetworkListAdapter.getLayout() == R.layout.dialog_bridge) {        // bridge text listener
            buttonConfiguration.bridgeLayout(editable, editText);
        }
        Log.e("TAG", "afterTextChanged()");
    }
}


