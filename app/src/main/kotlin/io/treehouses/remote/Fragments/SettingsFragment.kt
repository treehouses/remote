package io.treehouses.remote.Fragments

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import io.treehouses.remote.R
import io.treehouses.remote.callback.HomeInteractListener
import io.treehouses.remote.utils.KeyUtils
import io.treehouses.remote.utils.SaveUtils

class SettingsFragment : PreferenceFragmentCompat(), Preference.OnPreferenceClickListener {
    private var preferenceChangeListener: OnSharedPreferenceChangeListener? = null
    private lateinit var listener : HomeInteractListener
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        setPreferencesFromResource(R.xml.app_preferences, rootKey)
        val clearCommandsList = findPreference<Preference>("clear_commands")
        val resetCommandsList = findPreference<Preference>("reset_commands")
        val clearNetworkProfiles = findPreference<Preference>("network_profiles")
        val reactivateTutorials = findPreference<Preference>("reactivate_tutorials")
        val clearSSHHosts = findPreference<Preference>("ssh_hosts")
        val clearSSHKeys = findPreference<Preference>("ssh_keys")
        val showBluetoothFile = findPreference<Preference>("bluetooth_file")

        setClickListener(clearCommandsList)
        setClickListener(resetCommandsList)
        setClickListener(clearNetworkProfiles)
        setClickListener(reactivateTutorials)
        setClickListener(clearSSHHosts)
        setClickListener(clearSSHKeys)
        setClickListener(showBluetoothFile)

        preferenceChangeListener = OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == "dark_mode") {
                darkMode(sharedPreferences.getString(key, "").toString())
            }
        }
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

    private fun darkMode(key: String) {
        when (key) {
            "ON" ->  AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "OFF" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "Follow System" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    override fun onPreferenceClick(preference: Preference): Boolean {
        when (preference.key) {
            "clear_commands" -> clearCommands()
            "reset_commands" -> resetCommands()
            "network_profiles" -> networkProfiles()
            "reactivate_tutorials" -> reactivateTutorialsPrompt()
            "ssh_hosts" -> clearSSHHosts()
            "ssh_keys" -> clearSSHKeys()
            "bluetooth_file" -> openBluetoothFile()
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

    private fun clearSSHHosts() {
        createAlertDialog("Clear All SSH Hosts", "Would you like to delete all SSH Hosts? ", "Clear", CLEAR_SSH_HOSTS)
    }

    private fun clearSSHKeys() = createAlertDialog("Clear All SSH Keys", "Would you like to delete all SSH Keys?", "Clear", CLEAR_SSH_KEYS)

    private fun openBluetoothFile() {
        listener.openCallFragment(ShowBluetoothFile())
    }
    private fun createAlertDialog(title: String, message: String, positive: String, ID: Int) {
        val dialog = AlertDialog.Builder(ContextThemeWrapper(context, R.style.CustomAlertDialogStyle))
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positive) { _: DialogInterface?, _: Int -> onClickDialog(ID) }
                .setNegativeButton("Cancel") { _: DialogInterface?, _: Int -> }
                .create()
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun clearNetworkProfiles() {
        clear("profiles", "Network Profiles have been reset")
    }

    private fun clear(subject: String, message: String) {
        when (subject) {
            "profiles" -> {
                SaveUtils.clearProfiles(requireContext())
            }
            "commandsList" -> {
                SaveUtils.clearCommandsList(requireContext())
            }
        }
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    private fun onClickDialog(id: Int) {
        when (id) {
            CLEAR_COMMANDS_ID -> {
                clear("commandsList", "Commands List has been Cleared")
            }
            RESET_COMMANDS_ID -> {
                SaveUtils.clearCommandsList(requireContext())
                SaveUtils.initCommandsList(requireContext())
                Toast.makeText(context, "Commands has been reset to default", Toast.LENGTH_LONG).show()
            }
            NETWORK_PROFILES_ID -> clearNetworkProfiles()
            REACTIVATE_TUTORIALS -> reactivateTutorials()
            CLEAR_SSH_HOSTS -> SaveUtils.deleteAllHosts(requireContext())
            CLEAR_SSH_KEYS -> KeyUtils.deleteAllKeys(requireContext())
        }
    }

    private fun reactivateTutorialsPrompt() {
        createAlertDialog("Reactivate Tutorials", "Would you like to reactivate all the tutorials in the application? ", "Reactivate", REACTIVATE_TUTORIALS)
    }

    private fun reactivateTutorials() {
        for(screen in SaveUtils.Screens.values()) SaveUtils.setFragmentFirstTime(requireContext(), screen, true)
        Toast.makeText(context, "Tutorials reactivated", Toast.LENGTH_LONG).show()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = if (context is HomeInteractListener) context
                    else throw Exception("Context does not implement HomeInteractListener")
    }

    companion object {
        private const val CLEAR_COMMANDS_ID = 1
        private const val RESET_COMMANDS_ID = 2
        private const val NETWORK_PROFILES_ID = 3
        private const val REACTIVATE_TUTORIALS = 4
        private const val CLEAR_SSH_HOSTS = 5
        private const val CLEAR_SSH_KEYS = 6
    }
}