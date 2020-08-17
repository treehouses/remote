package io.treehouses.remote.Fragments

import android.content.Context
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import io.treehouses.remote.Fragments.PreferenceFragments.GeneralPreference
import io.treehouses.remote.Fragments.PreferenceFragments.UserCustomizationPreference
import io.treehouses.remote.R
import io.treehouses.remote.callback.HomeInteractListener

class SettingsFragment : PreferenceFragmentCompat(), Preference.OnPreferenceClickListener {
    private var preferenceChangeListener: OnSharedPreferenceChangeListener? = null
    private lateinit var listener : HomeInteractListener
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.app_preferences, rootKey)
        val general = findPreference<Preference>("general")
        val usercustomization = findPreference<Preference>("user_customizations")
        val showBluetoothFile = findPreference<Preference>("bluetooth_file")
        setClickListener(showBluetoothFile)
        setClickListener(general)
        setClickListener(usercustomization)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.windowBackground))
        setDivider(null)
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
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
            "general" -> openFragment(GeneralPreference())
            "user_customizations" -> openFragment(UserCustomizationPreference())
        }
        return false
    }

    private fun openFragment(f: Fragment) {
        val fragmentTransaction = parentFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, f)
        fragmentTransaction.addToBackStack(null)
        try {
            fragmentTransaction.commit()
        } catch (exception:IllegalStateException ){
            Log.e("Error", exception.toString())
        }
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