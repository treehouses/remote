package io.treehouses.remote;

import android.widget.Button;
import android.widget.Spinner;
import com.google.android.material.textfield.TextInputEditText;

public class ViewHolder {
    private String listSpinner;
    private TextInputEditText editText1;
    private TextInputEditText editText2;
    private TextInputEditText editText3;
    private TextInputEditText editText4;
    private Button button;

    public ViewHolder() { }

    public void setListSpinner(String listSpinner) {
        this.listSpinner = listSpinner;
    }

    public TextInputEditText getEditText4() {
        return editText4;
    }

    public void setEditText4(TextInputEditText editText4) {
        this.editText4 = editText4;
    }

    public TextInputEditText getEditText1() {
        return editText1;
    }

    public void setEditText1(TextInputEditText editText1) {
        this.editText1 = editText1;
    }

    public TextInputEditText getEditText2() {
        return editText2;
    }

    public void setEditText2(TextInputEditText editText2) {
        this.editText2 = editText2;
    }

    public TextInputEditText getEditText3() {
        return editText3;
    }

    public void setEditText3(TextInputEditText editText3) {
        this.editText3 = editText3;
    }

    public Button getButton() {
        return button;
    }

    public void setButton(Button button) {
        this.button = button;
    }
}

