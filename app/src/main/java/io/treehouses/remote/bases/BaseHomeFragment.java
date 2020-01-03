package io.treehouses.remote.bases;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Calendar;

import io.treehouses.remote.MainApplication;
import io.treehouses.remote.R;
import io.treehouses.remote.utils.LogUtils;

public class BaseHomeFragment extends BaseFragment {

    public void setAnimatorBackgrounds(ImageView green, ImageView red, int option) {
        if (option == 1) {
            green.setBackgroundResource(R.drawable.thanksgiving_anim_green);
            red.setBackgroundResource(R.drawable.thanksgiving_anim_red);
        }
        else if (option == 2) {
            green.setBackgroundResource(R.drawable.newyear_anim_green);
            red.setBackgroundResource(R.drawable.newyear_anim_red);
        }
        else {
            green.setBackgroundResource(R.drawable.dance_anim_green);
            red.setBackgroundResource(R.drawable.dance_anim_red);
        }
    }

    protected void showLogDialog(SharedPreferences preferences) {
        int connectionCount = preferences.getInt("connection_count", 0);
        boolean showDialog = preferences.getBoolean("show_log_dialog", true);
        LogUtils.log(connectionCount + "  " + showDialog);
        long lastDialogShown = preferences.getLong("last_dialog_shown", 0);
        Calendar date = Calendar.getInstance();
        date.add(Calendar.DAY_OF_YEAR, -7);
        if (lastDialogShown < date.getTimeInMillis()) {
            if (connectionCount >= 3 && showDialog) {
                preferences.edit().putLong("last_dialog_shown", Calendar.getInstance().getTimeInMillis()).commit();
                new AlertDialog.Builder(getActivity()).setTitle("Alert !!!!").setCancelable(false).setMessage("Treehouses wants to collect your activities. " +
                        "Do you like to share it? It will help us to improve.")
                        .setPositiveButton("Yes", (dialogInterface, i) -> {
                            preferences.edit().putBoolean("send_log", true).commit();
                            preferences.edit().putBoolean("show_log_dialog", false).commit();
                        })
                        .setNegativeButton("No", (dialogInterface, i) -> MainApplication.showLogDialog = false).show();
            }
        }
    }

    protected void showDialogOnce(SharedPreferences preferences) {
        boolean dialogShown = preferences.getBoolean("dialogShown", false);

        if (!dialogShown) {
            showWelcomeDialog();
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("dialogShown", true);
            editor.commit();
        }
    }

    private AlertDialog showWelcomeDialog() {
        final SpannableString s = new SpannableString("Treehouses Remote only works with our treehouses images, or a raspbian image enhanced by \"control\" and \"cli\". There is more information under \"Get Started\"" +
                "\n\nhttp://download.treehouses.io\nhttps://github.com/treehouses/control\nhttps://github.com/treehouses/cli");
        Linkify.addLinks(s, Linkify.ALL);
        final AlertDialog d = new AlertDialog.Builder(getContext())
                .setTitle("Friendly Reminder")
                .setIcon(R.drawable.dialog_icon)
                .setNegativeButton("OK", (dialog, which) -> dialog.cancel())
                .setMessage(s)
                .create();
        d.show();
        ((TextView) d.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
        return d;
    }
}
