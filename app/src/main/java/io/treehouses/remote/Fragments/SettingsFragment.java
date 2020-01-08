package io.treehouses.remote.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import io.treehouses.remote.R;
import io.treehouses.remote.utils.SaveUtils;

public class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener {
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

        setClickListener(clearCommandsList);
        setClickListener(resetCommandsList);
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
                SaveUtils.clearCommandsList(getContext());
                Toast.makeText(getContext(), "Commands List has been Cleared", Toast.LENGTH_LONG).show();
                break;
            case "reset_commands":
                SaveUtils.clearCommandsList(getContext());
                SaveUtils.initCommandsList(getContext());
                Toast.makeText(getContext(), "Commands has been reset to default", Toast.LENGTH_LONG).show();
                break;
            case "led_pattern":

        }
        return false;
    }
}
