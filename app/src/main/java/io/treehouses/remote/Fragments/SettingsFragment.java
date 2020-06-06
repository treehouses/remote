package io.treehouses.remote.Fragments;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.PopupMenu;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import io.treehouses.remote.R;
import io.treehouses.remote.utils.SaveUtils;

public class SettingsFragment extends PreferenceFragmentCompat  {
    private static final int CLEAR_COMMANDS_ID = 1;
    private static final int RESET_COMMANDS_ID = 2;
    private static final int NETWORK_PROFILES_ID = 3;
    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;
    View view;
    public SettingsFragment() {

    }


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        setPreferencesFromResource(R.xml.app_preferences, rootKey);
        Preference clearCommandsList = findPreference("clear_commands");
        Preference resetCommandsList = findPreference("reset_commands");
        Preference clearNetworkProfiles = findPreference("network_profiles");

        Preference nightMode = findPreference("night_mode");


        setClickListener(clearCommandsList);
        setClickListener(resetCommandsList);
        setClickListener(clearNetworkProfiles);
        setClickListener(nightMode);

        preferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.equals("night_mode")){
                    nightMode(sharedPreferences.getString(key, ""));
                }
            }
        };

    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    private void setClickListener(Preference preference) {
        if (preference != null) {
            preference.setOnPreferenceClickListener(this::onPreferenceClick);
        } else {
            Log.e("SETTINGS", "Unknown key");
        }
    }


    public void nightMode(String key){
        if (key.equals("ON")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        else if(key.equals("OFF")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        else if(key.equals("Automatic")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
    }


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
        createAlertDialog("Default Commands List", "Would you like to reset the command list to the default commands? ", "Reset", RESET_COMMANDS_ID);
    }

    private void networkProfiles() {
        createAlertDialog("Clear Network Profiles", "Would you like to remove all network profiles? ", "Clear", NETWORK_PROFILES_ID);

    }
    private void createAlertDialog(String title, String message, String positive, int ID) {
        new AlertDialog.Builder(getContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positive, (dialog, which) -> onClickDialog(ID))
                .setNegativeButton("Cancel", (dialog, which) -> { })
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
