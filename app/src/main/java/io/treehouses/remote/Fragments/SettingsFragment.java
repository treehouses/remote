package io.treehouses.remote.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import io.treehouses.remote.R;
import io.treehouses.remote.bases.BaseFragment;
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
        if (clearCommandsList != null && resetCommandsList != null) {
            clearCommandsList.setOnPreferenceClickListener(this);
            resetCommandsList.setOnPreferenceClickListener(this);
        }
        else {
            Log.e("Settings Fragment", "Null Preference");
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
        }
        return false;
    }
}
