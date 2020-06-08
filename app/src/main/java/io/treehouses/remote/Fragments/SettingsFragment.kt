package io.treehouses.remote.Fragments

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import io.treehouses.remote.R
import io.treehouses.remote.utils.SaveUtils

class SettingsFragment : PreferenceFragmentCompat(), Preference.OnPreferenceClickListener {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.app_preferences, rootKey)
        val clearCommandsList = findPreference<Preference>("clear_commands")
        val resetCommandsList = findPreference<Preference>("reset_commands")
        val clearNetworkProfiles = findPreference<Preference>("network_profiles")
        setClickListener(clearCommandsList)
        setClickListener(resetCommandsList)
        setClickListener(clearNetworkProfiles)
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
            "clear_commands" -> clearCommands()
            "reset_commands" -> resetCommands()
            "network_profiles" -> networkProfiles()
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
        AlertDialog.Builder(context)
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
        }
    }

    companion object {
        private const val CLEAR_COMMANDS_ID = 1
        private const val RESET_COMMANDS_ID = 2
        private const val NETWORK_PROFILES_ID = 3
    }
}