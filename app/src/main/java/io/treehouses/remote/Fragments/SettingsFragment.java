package io.treehouses.remote.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.preference.PreferenceFragmentCompat;

import io.treehouses.remote.R;
import io.treehouses.remote.bases.BaseFragment;

public class SettingsFragment extends PreferenceFragmentCompat {
    public SettingsFragment() {

    }

    public SettingsFragment getInstance() {
        return new SettingsFragment();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.app_preferences, rootKey);
    }
}
