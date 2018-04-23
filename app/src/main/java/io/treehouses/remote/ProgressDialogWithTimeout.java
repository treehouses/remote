package io.treehouses.remote;

import android.app.ProgressDialog;
import android.content.Context;

import java.util.Timer;
import java.util.TimerTask;

public class ProgressDialogWithTimeout extends ProgressDialog{

    private static Timer mTimer = new Timer();
    private static ProgressDialog dialog;

    public ProgressDialogWithTimeout(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }

    public ProgressDialogWithTimeout(Context context, int theme) {
        super(context, theme);
        // TODO Auto-generated constructor stub
    }

    public static ProgressDialog show (Context context,
                                       CharSequence title,
                                       CharSequence message,
                                       long timeout) {
        MyTask task = new MyTask();
        // Run task after 10 seconds
        mTimer.schedule(task, 0, timeout);

        dialog = ProgressDialog.show(context, title, message);
        return dialog;
    }

    public void dismiss(long timeout){
        MyTask task = new MyTask();
        mTimer.schedule(task, 0, timeout);
    }

    static class MyTask extends TimerTask {
        public void run() {
            // Do what you wish here with the dialog
            if (dialog != null)
            {
                dialog.cancel();
            }
        }
    }
}





