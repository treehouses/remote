package io.treehouses.remote.Fragments

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import io.treehouses.remote.R
import io.treehouses.remote.utils.SaveUtils

class SettingsFragment : PreferenceFragmentCompat(), Preference.OnPreferenceClickListener {
    var preferenceChangeListener: OnSharedPreferenceChangeListener? = null
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        setPreferencesFromResource(R.xml.app_preferences, rootKey)
        val clearCommandsList = findPreference<Preference>("clear_commands")
        val resetCommandsList = findPreference<Preference>("reset_commands")
        val clearNetworkProfiles = findPreference<Preference>("network_profiles")
        val reactivateTutorials = findPreference<Preference>("reactivate_tutorials")

        setClickListener(clearCommandsList)
        setClickListener(resetCommandsList)
        setClickListener(clearNetworkProfiles)
        setClickListener(reactivateTutorials)

        preferenceChangeListener = OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == "dark_mode") {
                darkMode(sharedPreferences.getString(key, "").toString())
            }
        }
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

    fun darkMode(key: String) {
        if (key == "ON") {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else if (key == "OFF") {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        } else if (key == "Follow System") {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    override fun onPreferenceClick(preference: Preference): Boolean {
        when (preference.key) {
            "clear_commands" -> clearCommands()
            "reset_commands" -> resetCommands()
            "network_profiles" -> networkProfiles()
            "reactivate_tutorials" -> reactivateTutorialsPrompt()
        }
        return false
    }

    private fun clearCommands() {
        createAlertDialog("Clear Commands List", "Would you like to completely clear the commands list that is found in terminal? ", "Clear", CLEAR_COMMANDS_ID)
    }

    private fun resetCommands() {
        createAlertDialog("Default Commands List", "Would you like to reset the command list to the default commands? ", "Reset", RESET_COMMANDS_ID)
    }

    private fun networkProfiles() {
        createAlertDialog("Clear Network Profiles", "Would you like to remove all network profiles? ", "Clear", NETWORK_PROFILES_ID)
    }

    private fun createAlertDialog(title: String, message: String, positive: String, ID: Int) {
        AlertDialog.Builder(ContextThemeWrapper(context, R.style.CustomAlertDialogStyle))
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positive) { dialog: DialogInterface?, which: Int -> onClickDialog(ID) }
                .setNegativeButton("Cancel") { dialog: DialogInterface?, which: Int -> }
                .create()
                .show()
    }

    private fun clearNetworkProfiles() {
        SaveUtils.clearProfiles(requireContext())
        Toast.makeText(context, "Network Profiles have been reset", Toast.LENGTH_LONG).show()
    }

    private fun onClickDialog(id: Int) {
        when (id) {
            CLEAR_COMMANDS_ID -> {
                SaveUtils.clearCommandsList(requireContext())
                Toast.makeText(context, "Commands List has been Cleared", Toast.LENGTH_LONG).show()
            }
            RESET_COMMANDS_ID -> {
                SaveUtils.clearCommandsList(requireContext())
                SaveUtils.initCommandsList(requireContext())
                Toast.makeText(context, "Commands has been reset to default", Toast.LENGTH_LONG).show()
            }
            NETWORK_PROFILES_ID -> clearNetworkProfiles()
            REACTIVATE_TUTORIALS -> reactivateTutorials()
        }
    }

    private fun reactivateTutorialsPrompt() {
        createAlertDialog("Reactivate Tutorials", "Would you like to reactivate all the tutorials in the application? ", "Reactivate", REACTIVATE_TUTORIALS)
    }

    private fun reactivateTutorials() {
        SaveUtils.setFragmentFirstTime(requireContext(), SaveUtils.Screens.HOME, true)
        SaveUtils.setFragmentFirstTime(requireContext(), SaveUtils.Screens.NETWORK, true)
        SaveUtils.setFragmentFirstTime(requireContext(), SaveUtils.Screens.SERVICES_DETAILS, true)
        SaveUtils.setFragmentFirstTime(requireContext(), SaveUtils.Screens.SERVICES_OVERVIEW, true)
        SaveUtils.setFragmentFirstTime(requireContext(), SaveUtils.Screens.STATUS, true)
        SaveUtils.setFragmentFirstTime(requireContext(), SaveUtils.Screens.SYSTEM, true)
        SaveUtils.setFragmentFirstTime(requireContext(), SaveUtils.Screens.TERMINAL, true)
        SaveUtils.setFragmentFirstTime(requireContext(), SaveUtils.Screens.TUNNEL, true)
        Toast.makeText(context, "Tutorials reactivated", Toast.LENGTH_LONG).show()
    }

    companion object {
        private const val CLEAR_COMMANDS_ID = 1
        private const val RESET_COMMANDS_ID = 2
        private const val NETWORK_PROFILES_ID = 3
        private const val REACTIVATE_TUTORIALS = 4
    }
}