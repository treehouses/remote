package io.treehouses.remote;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.google.android.material.textfield.TextInputEditText;


public class ViewHolder {
    private View listButton;
    private View listGroup;
    private View listItem;
    private View listChild1;
    private View listChild2;
    private View listChild3;
    private TextInputEditText editText1;
    private TextInputEditText editText2;
    private TextInputEditText editText3;
    private TextView textView;
    private TextView textViewGroup;
    private Button button;

    public ViewHolder() { }

    public View getListButton() {
        return listButton;
    }

    public void setListButton(View listButton) {
        this.listButton = listButton;
    }

    public View getListItem() {
        return listItem;
    }

    public void setListItem(View listItem) {
        this.listItem = listItem;
    }

    public View getListGroup() {
        return listGroup;
    }

    public void setListGroup(View convertViewGroup) {
        this.listGroup = convertViewGroup;
    }

    public View getListChild1() {
        return listChild1;
    }

    public void setListChild1(View convertView1) {
        this.listChild1 = convertView1;
    }

    public View getListChild2() {
        return listChild2;
    }

    public void setListChild2(View convertView2) {
        this.listChild2 = convertView2;
    }

    public View getListChild3() {
        return listChild3;
    }

    public void setListChild3(View convertView3) {
        this.listChild3 = convertView3;
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

    public TextView getTextView() {
        return textView;
    }

    public void setTextView(TextView textView) {
        this.textView = textView;
    }

    public TextView getTextViewGroup() {
        return textViewGroup;
    }

    public void setTextViewGroup(TextView group) {
        this.textViewGroup = group;
    }

    public Button getButton() {
        return button;
    }

    public void setButton(Button button) {
        this.button = button;
    }
}

