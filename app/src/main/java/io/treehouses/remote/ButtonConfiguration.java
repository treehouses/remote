package io.treehouses.remote;

import android.widget.Button;

public class ButtonConfiguration {

    public static void buttonProperties(Button button, Boolean clickable, int color) {
       button.setClickable(clickable);
       button.setTextColor(color);
   }
}
