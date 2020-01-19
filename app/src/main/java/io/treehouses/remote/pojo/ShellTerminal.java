package io.treehouses.remote.pojo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatEditText;


@SuppressLint("AppCompatCustomView")
public class ShellTerminal extends AppCompatEditText implements TextView.OnEditorActionListener {
    int cursorPosition;
    boolean inputting;
    String saved = "";
    public ShellTerminal(Context context) {
        super(context);
        this.cursorPosition = 0;
        addTextChange();

    }

    public ShellTerminal(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.cursorPosition = 0;
        addTextChange();
    }

    public ShellTerminal(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.cursorPosition = 0;
        addTextChange();
    }
    private void addTextChange() {
        addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
//                if (cursorPosition < getText().toString().indexOf(lastLine)) {
//                    setFocusable(false);
//                    setFocusableInTouchMode(false);
//                    Log.d("YAYAY","SFSD");
//                }
//                else {
//                    setFocusable(true);
//                    setFocusableInTouchMode(true);
//                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
                if (cursorPosition < getText().toString().lastIndexOf("\n")) {
                    setSelection(length());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (inputting) {
                    if (s.toString().endsWith("\n")) {
                        s.delete(s.length()-1,s.length());
                    }
                }
            }
        });
    }

    public String getLine(int i) {
        String[] commands = getText().toString().split("\n");
        return commands[commands.length-i];
    }

    public void isInputting(boolean inputting) {
        this.inputting = inputting;
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        // Do ur task here.
        this.cursorPosition = selEnd;
        Log.d("CURSOR", Integer.toString(cursorPosition));
//        if (cursorPosition < getText().toString().lastIndexOf("\n")) {
//            setSelection(length());
//        }

    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (cursorPosition < getText().toString().lastIndexOf("\n")) {
            setSelection(length());
        }
        return false;
    }
}
