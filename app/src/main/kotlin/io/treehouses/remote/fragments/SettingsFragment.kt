package io.treehouses.remote.fragments

import android.content.Context
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import io.treehouses.remote.fragments.preferencefragments.GeneralPreferenceFragment
import io.treehouses.remote.fragments.preferencefragments.UserCustomizationPreferenceFragment
import io.treehouses.remote.R
import io.treehouses.remote.callback.HomeInteractListener
import io.treehouses.remote.utils.SettingsUtils

class SettingsFragment : PreferenceFragmentCompat(), Preference.OnPreferenceClickListener {
    private var preferenceChangeListener: OnSharedPreferenceChangeListener? = null
    private lateinit var listener : HomeInteractListener
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.app_preferences, rootKey)
        val general = findPreference<Preference>("general")
        val usercustomization = findPreference<Preference>("user_customization")
        val showBluetoothFile = findPreference<Preference>("bluetooth_file")
        SettingsUtils.setClickListener(this, showBluetoothFile)
        SettingsUtils.setClickListener(this, general)
        SettingsUtils.setClickListener(this, usercustomization)
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

    override fun onPreferenceClick(preference: Preference): Boolean {
        when (preference.key) {
            "bluetooth_file" -> openBluetoothFile()
            "general" -> openFragment(GeneralPreferenceFragment())
            "user_customization" -> openFragment(UserCustomizationPreferenceFragment())
        }
        return false
    }

    private fun openFragment(f: Fragment) {
        val fragmentTransaction = parentFragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_in_from_right, R.anim.slide_out_to_left, R.anim.slide_in_from_left, R.anim.slide_out_to_right)
        SettingsUtils.openFragment(false, fragmentTransaction, f)
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