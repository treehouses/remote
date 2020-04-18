package io.treehouses.remote.bases;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import java.util.Calendar;
import java.util.HashMap;

import io.treehouses.remote.Constants;
import io.treehouses.remote.Fragments.DialogFragments.RPIDialogFragment;
import io.treehouses.remote.MainApplication;
import io.treehouses.remote.Network.ParseDbService;
import io.treehouses.remote.R;
import io.treehouses.remote.callback.SetDisconnect;
import io.treehouses.remote.utils.LogUtils;


public class BaseHomeFragment extends BaseFragment {
    public SharedPreferences preferences;
    public String imageVersion = "", tresshousesVersion = "", bluetoothMac = "", rpiVersion;

    public void setAnimatorBackgrounds(ImageView green, ImageView red, int option) {
        if (option == 1) {
            green.setBackgroundResource(R.drawable.thanksgiving_anim_green);
            red.setBackgroundResource(R.drawable.thanksgiving_anim_red);
        } else if (option == 2) {
            green.setBackgroundResource(R.drawable.newyear_anim_green);
            red.setBackgroundResource(R.drawable.newyear_anim_red);
        } else {
            green.setBackgroundResource(R.drawable.dance_anim_green);
            red.setBackgroundResource(R.drawable.dance_anim_red);
        }
    }

    protected void showLogDialog(SharedPreferences preferences) {
        int connectionCount = preferences.getInt("connection_count", 0);
        long lastDialogShown = preferences.getLong("last_dialog_shown", 0);
        Calendar date = Calendar.getInstance();
        date.add(Calendar.DAY_OF_YEAR, -7);
        View v = getLayoutInflater().inflate(R.layout.alert_log,null);
        String emoji = new String(Character.toChars(0x1F60A));

        if (lastDialogShown < date.getTimeInMillis() && !preferences.getBoolean("send_log", false)) {
            if (connectionCount >= 3) {
                preferences.edit().putLong("last_dialog_shown", Calendar.getInstance().getTimeInMillis()).commit();
                new AlertDialog.Builder(getActivity()).setTitle("Sharing is Caring  " + emoji).setCancelable(false).setMessage("Treehouses wants to collect your activities. " +
                        "Do you like to share it? It will help us to improve."  )
                        .setPositiveButton("Continue", (dialogInterface, i) -> {
                            preferences.edit().putBoolean("send_log", true).commit();
                        }).setNegativeButton("Cancel", (dialogInterface, i) -> MainApplication.showLogDialog = false).setView(v).show();
            }
        }
    }

    public void rate(SharedPreferences preferences) {
        int connectionCount = preferences.getInt("connection_count", 0);
        boolean ratingDialog = preferences.getBoolean("ratingDialog", true);
        LogUtils.log(connectionCount + "  " + ratingDialog);
        long lastDialogShown = preferences.getLong("last_dialog_shown", 0);
        Calendar date = Calendar.getInstance();
        if (lastDialogShown < date.getTimeInMillis()) {
            if (connectionCount >= 3 && ratingDialog) {
        new AlertDialog.Builder(getActivity()).setTitle("Thank You").setCancelable(false).setMessage("We're so happy to hear that you love the Treehouses app! " +
                "It'd be really helpful if you rated us. Thanks so much for spending some time with us.")
                .setPositiveButton("RATE IT NOW", (dialogInterface, i) -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=io.treehouses.remote"));
                    startActivity(intent);
                    preferences.edit().putBoolean("ratingDialog", false).commit();
                }).setNeutralButton("REMIND ME LATER", (dialogInterface, i)->{MainApplication.ratingDialog = false;})
                .setNegativeButton("NO THANKS", (dialogInterface, i) -> { preferences.edit().putBoolean("ratingDialog", false).commit();
                }).show();
    }}}


    public void checkImageInfo(String[] readMessage, String deviceName) {
        bluetoothMac = readMessage[0];
        imageVersion = readMessage[1];
        tresshousesVersion = readMessage[2];
        rpiVersion = readMessage[3];
        sendLog(deviceName);
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
            map.put("rpiVersion", rpiVersion);
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

    protected void showRPIDialog(SetDisconnect s) {
        androidx.fragment.app.DialogFragment dialogFrag = RPIDialogFragment.newInstance(123);
        ((RPIDialogFragment) dialogFrag).setCheckConnectionState(s);
        dialogFrag.setTargetFragment(this, Constants.REQUEST_DIALOG_FRAGMENT_HOTSPOT);
        dialogFrag.show(getFragmentManager().beginTransaction(), "rpiDialog");
    }

    protected boolean matchResult(String output, String option1, String option2) { return output.contains(option1) || output.contains(option2); }


    protected void showUpgradeCLI() {
        AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                .setTitle("Update Treehouses CLI")
                .setMessage("Treehouses CLI needs an upgrade to correctly function with Treehouses Remote. Please upgrade to the latest version!")
                .setPositiveButton("Upgrade", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.sendMessage("treehouses upgrade \n");
                        Toast.makeText(getContext(), "Upgraded", Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Upgrade Later", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        alertDialog.show();
    }
}
