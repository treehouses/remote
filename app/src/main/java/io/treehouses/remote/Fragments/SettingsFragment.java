package io.treehouses.remote.Fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import io.treehouses.remote.R;
import io.treehouses.remote.utils.SaveUtils;

public class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener {
    private static final int CLEAR_COMMANDS_ID = 1;
    private static final int RESET_COMMANDS_ID = 2;
    private static final int NETWORK_PROFILES_ID = 3;


    public SettingsFragment() {

    }

    public SettingsFragment getInstance() {
        return new SettingsFragment();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.app_preferences, rootKey);
        Preference clearCommandsList = findPreference("clear_commands");
        Preference resetCommandsList = findPreference("reset_commands");
        Preference clearNetworkProfiles = findPreference("network_profiles");

        setClickListener(clearCommandsList);
        setClickListener(resetCommandsList);
        setClickListener(clearNetworkProfiles);
    }

    private void setClickListener(Preference preference) {
        if (preference != null) {
            preference.setOnPreferenceClickListener(this);
        } else {
            Log.e("SETTINGS", "Unknown key");
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        switch (preference.getKey()) {
            case "clear_commands":
                clearCommands();
                break;
            case "reset_commands":
                resetCommands();
                break;
            case "network_profiles":
                networkProfiles();
                break;
        }
        return false;
    }

    private void clearCommands() {
        createAlertDialog("Clear Commands List", "Would you like to completely clear the commands list that is found in terminal? ", "Clear",  CLEAR_COMMANDS_ID);
    }

    private void resetCommands() {
        createAlertDialog("Reset Commands List", "Would you like to reset the command list to the default commands? ", "Reset", RESET_COMMANDS_ID);
    }

    private void networkProfiles() {
        createAlertDialog("Reset Network Profiles", "Would you like to remove all network profiles? ", "Clear", NETWORK_PROFILES_ID);

    }
    private void createAlertDialog(String title, String message, String positive, int ID) {
        new AlertDialog.Builder(getContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onClickDialog(ID);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .create()
                .show();
    }
    private void onClickDialog(int id) {
        switch (id) {
            case CLEAR_COMMANDS_ID:
                SaveUtils.clearCommandsList(getContext());
                Toast.makeText(getContext(), "Commands List has been Cleared", Toast.LENGTH_LONG).show();
                break;
            case RESET_COMMANDS_ID:
                SaveUtils.clearCommandsList(getContext());
                SaveUtils.initCommandsList(getContext());
                Toast.makeText(getContext(), "Commands has been reset to default", Toast.LENGTH_LONG).show();
                break;
            case NETWORK_PROFILES_ID:
                SaveUtils.clearProfiles(getContext());
                Toast.makeText(getContext(), "Network Profiles have been reset", Toast.LENGTH_LONG).show();
                break;
        }
    }
}
