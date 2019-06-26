package io.treehouses.remote;

import android.widget.Button;

import io.treehouses.remote.Fragments.NetworkFragment;

public abstract class ButtonConfiguration {
    protected Button btnStartConfiguration;
    public  void buttonProperties(Boolean clickable, int color){
        NetworkFragment.getInstance().setButtonConfiguration(this);
        btnStartConfiguration.setClickable(clickable);
        btnStartConfiguration.setTextColor(color);
    }
}
