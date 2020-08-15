package io.treehouses.remote.Fragments.PreferenceFragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import io.treehouses.remote.Fragments.ShowBluetoothFile
import io.treehouses.remote.R
import io.treehouses.remote.callback.BackPressReceiver
import io.treehouses.remote.callback.HomeInteractListener

class AdvancedPreference: PreferenceFragmentCompat(), BackPressReceiver, Preference.OnPreferenceClickListener {
    private lateinit var listener : HomeInteractListener

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.advanced_preferences, rootKey)
        val showBluetoothFile = findPreference<Preference>("bluetooth_file")
        setClickListener(showBluetoothFile)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.windowBackground))
        setDivider(null)
    }

    private fun setClickListener(preference: Preference?) {
        if (preference != null) {
            preference.onPreferenceClickListener = this
        } else {
            Log.e("SETTINGS", "Unknown key")
        }
    }

    override fun onPreferenceClick(preference: Preference): Boolean {
        when (preference.key) {
            "bluetooth_file" -> openBluetoothFile()
        }
        return false
    }

    override fun onBackPressed() {
        parentFragmentManager.popBackStack()
    }

    private fun openBluetoothFile() {
        listener.openCallFragment(ShowBluetoothFile())
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = if (context is HomeInteractListener) context
        else throw Exception("Context does not implement HomeInteractListener")
    }

}