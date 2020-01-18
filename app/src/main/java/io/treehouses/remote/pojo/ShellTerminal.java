package io.treehouses.remote.pojo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;


@SuppressLint("AppCompatCustomView")
public class ShellTerminal extends EditText {
    ShellTerminal shell;
    int cursorPosition;
    public ShellTerminal(Context context) {
        super(context);
        this.shell = new ShellTerminal(context);
        this.cursorPosition = 0;
        addTextChange();

    }

    private void addTextChange() {
        shell.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
                String lastLine = getLine(1);
                //if (cursorPosition)
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
    public String getLine(int i) {
        String[] commands = shell.getText().toString().split("\n");
        return commands[commands.length-i];
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        // Do ur task here.
        this.cursorPosition = selEnd;

    }
}
