package io.treehouses.remote.Fragments.PreferenceFragments

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceFragmentCompat
import io.treehouses.remote.R
import io.treehouses.remote.callback.BackPressReceiver

class AboutPreference: PreferenceFragmentCompat(), BackPressReceiver {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.about_preferences, rootKey)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.windowBackground))
        setDivider(null)
    }

    override fun onBackPressed() {
        parentFragmentManager.popBackStack()
    }
}