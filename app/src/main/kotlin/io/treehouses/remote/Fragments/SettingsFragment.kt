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
import androidx.fragment.app.Fragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import io.treehouses.remote.Fragments.PreferenceFragments.AboutPreference
import io.treehouses.remote.Fragments.PreferenceFragments.AdvancedPreference
import io.treehouses.remote.Fragments.PreferenceFragments.GeneralPreference
import io.treehouses.remote.Fragments.PreferenceFragments.PrivacyPreference
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
        val clearSSHHosts = findPreference<Preference>("ssh_hosts")
        val clearSSHKeys = findPreference<Preference>("ssh_keys")
        val general = findPreference<Preference>("general")
        val privacy = findPreference<Preference>("privacy")
        val about = findPreference<Preference>("about")
        val advanced = findPreference<Preference>("advanced")
        setClickListener(clearCommandsList)
        setClickListener(resetCommandsList)
        setClickListener(clearNetworkProfiles)
        setClickListener(clearSSHHosts)
        setClickListener(clearSSHKeys)
        setClickListener(general)
        setClickListener(privacy)
        setClickListener(about)
        setClickListener(advanced)
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
            "clear_commands" -> clearCommands()
            "reset_commands" -> resetCommands()
            "network_profiles" -> networkProfiles()
            "ssh_hosts" -> clearSSHHosts()
            "ssh_keys" -> clearSSHKeys()
            "general" -> openFragment(GeneralPreference())
            "privacy" -> openFragment(PrivacyPreference())
            "about" -> openFragment(AboutPreference())
            "advanced" -> openFragment(AdvancedPreference())
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
            CLEAR_SSH_HOSTS -> SaveUtils.deleteAllHosts(requireContext())
            CLEAR_SSH_KEYS -> KeyUtils.deleteAllKeys(requireContext())
        }
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
        private const val CLEAR_SSH_HOSTS = 4
        private const val CLEAR_SSH_KEYS = 5
    }
}