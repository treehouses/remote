package io.treehouses.remote.bases;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Calendar;
import java.util.HashMap;
import java.util.regex.Pattern;

import io.treehouses.remote.MainApplication;
import io.treehouses.remote.Network.ParseDbService;
import io.treehouses.remote.R;
import io.treehouses.remote.utils.LogUtils;

public class BaseHomeFragment extends BaseFragment {
    public SharedPreferences preferences;
    public String imageVersion = "", tresshousesVersion = "", bluetoothMac = "";

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




    public void checkImageInfo(String readMessage, String deviceName) {

        String versionRegex = ".*\\..*\\..*";
        String regexImage = "release.*";
        boolean matchesImagePattern = Pattern.matches(regexImage, readMessage);
        boolean matchesVersion = Pattern.matches(versionRegex, readMessage);
        if (readMessage.contains(":") && readMessage.split(":").length == 6) {
            bluetoothMac = readMessage;
        }
        if (matchesImagePattern)
            imageVersion = readMessage;
        if (matchesVersion) {
            tresshousesVersion = readMessage;
            sendLog(deviceName);
        }
    }


    private void sendLog(String deviceName) {
        int connectionCount = preferences.getInt("connection_count", 0);
        boolean sendLog = preferences.getBoolean("send_log", true);
        preferences.edit().putInt("connection_count", connectionCount + 1).commit();
        if (connectionCount >= 3 && sendLog) {
            HashMap<String, String> map = new HashMap<>();
            map.put("imageVersion", imageVersion);
            map.put("treehousesVersion", tresshousesVersion);
            map.put("bluetoothMacAddress", bluetoothMac);
            ParseDbService.sendLog(getActivity(), deviceName, map, preferences);
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
                "\n\nhttps://treehouses.io/#!pages/download.md\nhttps://github.com/treehouses/control\nhttps://github.com/treehouses/cli");
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

    public AlertDialog showTestConnectionDialog(Boolean dismissable, String title, int messageID, int selected_LED) {
        View mView = getLayoutInflater().inflate(R.layout.dialog_test_connection, null);
        ImageView mIndicatorGreen = mView.findViewById(R.id.flash_indicator_green);
        ImageView mIndicatorRed = mView.findViewById(R.id.flash_indicator_red);
        if (!dismissable) {
            mIndicatorGreen.setVisibility(View.VISIBLE);
            mIndicatorRed.setVisibility(View.VISIBLE);
        } else {
            mIndicatorGreen.setVisibility(View.INVISIBLE);
            mIndicatorRed.setVisibility(View.INVISIBLE);
        }
        setAnimatorBackgrounds(mIndicatorGreen, mIndicatorRed, selected_LED);
        AnimationDrawable animationDrawableGreen = (AnimationDrawable) mIndicatorGreen.getBackground();
        AnimationDrawable animationDrawableRed = (AnimationDrawable) mIndicatorRed.getBackground();
        animationDrawableGreen.start();
        animationDrawableRed.start();
        AlertDialog a = createTestConnectionDialog(mView, dismissable, title, messageID);
        a.show();
        return a;
    }

    public AlertDialog createTestConnectionDialog(View mView, Boolean dismissable, String title, int messageID) {
        AlertDialog.Builder d = new AlertDialog.Builder(getContext()).setView(mView).setTitle(title).setIcon(R.drawable.ic_action_device_access_bluetooth_searching).setMessage(messageID);
        if (dismissable) d.setNegativeButton("OK", (dialog, which) -> dialog.dismiss());
        return d.create();
    }
}
