package io.treehouses.remote.fragments.preferencefragments

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.WindowManager
import android.widget.Toast
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import io.treehouses.remote.MainApplication
import io.treehouses.remote.R
import io.treehouses.remote.bases.BasePreferenceFragment
import io.treehouses.remote.utils.KeyUtils
import io.treehouses.remote.utils.SaveUtils
import io.treehouses.remote.utils.SettingsUtils

class UserCustomizationPreferenceFragment: BasePreferenceFragment() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.user_customization_preferences, rootKey)
        val clearCommandsList = findPreference<Preference>("clear_commands")
        val resetCommandsList = findPreference<Preference>("reset_commands")
        val clearNetworkProfiles = findPreference<Preference>("network_profiles")
        val clearSSHHosts = findPreference<Preference>("ssh_hosts")
        val clearSSHKeys = findPreference<Preference>("ssh_keys")
        val fontSize = findPreference<Preference>("font_size")
        fontSize?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, newValue ->
                PreferenceManager.getDefaultSharedPreferences(requireContext()).edit().putInt("font_size", newValue.toString().toInt()).commit()
                adjustFontScale(resources.configuration, newValue.toString().toInt())
                activity?.recreate()
                false
            }
        SettingsUtils.setClickListener(this, clearCommandsList)
        SettingsUtils.setClickListener(this, resetCommandsList)
        SettingsUtils.setClickListener(this, clearNetworkProfiles)
        SettingsUtils.setClickListener(this, clearSSHHosts)
        SettingsUtils.setClickListener(this, clearSSHKeys)
    }

    fun adjustFontScale(configuration: Configuration?, fontSize: Int) {
        configuration?.let {
            it.fontScale = 0.05F*fontSize.toFloat()
            val metrics: DisplayMetrics = resources.displayMetrics
            val wm: WindowManager = getActivity()?.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            wm.defaultDisplay.getMetrics(metrics)
            metrics.scaledDensity = configuration.fontScale * metrics.density

            MainApplication.context.createConfigurationContext(it)
            MainApplication.context.resources.displayMetrics.setTo(metrics)
        }
    }

    override fun onPreferenceClick(preference: Preference): Boolean {
        when (preference.key) {
            "clear_commands" -> clearCommands()
            "reset_commands" -> resetCommands()
            "network_profiles" -> networkProfiles()
            "ssh_hosts" -> clearSSHHosts()
            "ssh_keys" -> clearSSHKeys()
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

    private fun clear(subject: String, message: String) {
        when (subject) {
            "profiles" -> SaveUtils.clearProfiles(requireContext())
            "commandsList" -> SaveUtils.clearCommandsList(requireContext())
            "SSH_hosts" -> SaveUtils.deleteAllHosts(requireContext())
            "SSH_keys" -> KeyUtils.deleteAllKeys(requireContext())
        }
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    override fun onClickDialog(id: Int) {
        when (id) {
            CLEAR_COMMANDS_ID -> clear("commandsList", "Commands List has been Cleared")
            RESET_COMMANDS_ID -> {
                SaveUtils.clearCommandsList(requireContext())
                SaveUtils.initCommandsList(requireContext())
                Toast.makeText(context, "Commands has been reset to default", Toast.LENGTH_LONG).show()
            }
            NETWORK_PROFILES_ID -> clear("profiles", "Network Profiles have been reset")
            CLEAR_SSH_HOSTS -> clear("SSH_hosts", "SSH Hosts have been cleared")
            CLEAR_SSH_KEYS -> clear("SSH_keys", "SSH Keys have been cleared")
        }
    }

    companion object {
        private const val CLEAR_COMMANDS_ID = 1
        private const val RESET_COMMANDS_ID = 2
        private const val NETWORK_PROFILES_ID = 3
        private const val CLEAR_SSH_HOSTS = 4
        private const val CLEAR_SSH_KEYS = 5
    }
}
