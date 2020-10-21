package io.treehouses.remote.fragments.preferenceFragments

import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.Preference
import io.treehouses.remote.R
import io.treehouses.remote.bases.BasePreferenceFragment
import io.treehouses.remote.ui.services.ServicesViewModel
import io.treehouses.remote.utils.SaveUtils
import io.treehouses.remote.utils.SettingsUtils

class GeneralPreference: BasePreferenceFragment() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.general_preferences, rootKey)
        val reactivateTutorials = findPreference<Preference>("reactivate_tutorials")
        val clearServices = findPreference<Preference>("clear_services")
        SettingsUtils.setClickListener(this, reactivateTutorials)
        SettingsUtils.setClickListener(this, clearServices)

        preferenceChangeListener = OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == "dark_mode") {
                darkMode(sharedPreferences.getString(key, "").toString())
            }
        }
    }

    override fun onPreferenceClick(preference: Preference): Boolean {
        when (preference.key) {
            "clear_services" -> clearServicesPrompt()
            "reactivate_tutorials" -> reactivateTutorialsPrompt()
        }
        return false
    }

    override fun onClickDialog(id: Int) {
        when (id) {
            REACTIVATE_TUTORIALS -> reactivateTutorials()
            CLEAR_SERVICES -> clearServices()
        }
    }

    private fun clearServicesPrompt() {
        createAlertDialog("Clear Services Cache", "Would you like to Clear Stored Services List? This will increase the loading time on next visit to Services.", "Clear", CLEAR_SERVICES)
    }

    private fun clearServices() {
        SaveUtils.removeStringList(requireContext(), ServicesViewModel.SERVICES_CACHE)
        Toast.makeText(context, "Services Cache Cleared", Toast.LENGTH_LONG).show()
    }

    private fun reactivateTutorialsPrompt() {
        createAlertDialog("Reactivate Tutorials", "Would you like to reactivate all the tutorials in the application? ", "Reactivate", REACTIVATE_TUTORIALS)
    }

    private fun reactivateTutorials() {
        for(screen in SaveUtils.Screens.values()) SaveUtils.setFragmentFirstTime(requireContext(), screen, true)
        Toast.makeText(context, "Tutorials reactivated", Toast.LENGTH_LONG).show()
    }

    private fun darkMode(key: String) {
        when (key) {
            "ON" ->  AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "OFF" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "Follow System" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    companion object {
        private const val REACTIVATE_TUTORIALS = 1
        private const val CLEAR_SERVICES = 2
    }
}