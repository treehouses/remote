package io.treehouses.remote.fragments

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreferenceCompat
import io.treehouses.remote.MainApplication
import io.treehouses.remote.R
import io.treehouses.remote.callback.HomeInteractListener
import io.treehouses.remote.fragments.preferencefragments.GeneralPreferenceFragment
import io.treehouses.remote.fragments.preferencefragments.UserCustomizationPreferenceFragment
import io.treehouses.remote.utils.DialogUtils
import io.treehouses.remote.utils.SettingsUtils

class SettingsFragment : PreferenceFragmentCompat(), Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {
    private var preferenceChangeListener: OnSharedPreferenceChangeListener? = null
    private lateinit var listener : HomeInteractListener
    private lateinit var preferences: SharedPreferences

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.app_preferences, rootKey)
        val general = findPreference<Preference>("general")
        val usercustomization = findPreference<Preference>("user_customization")
        val showBluetoothFile = findPreference<Preference>("bluetooth_file")
        SettingsUtils.setClickListener(this, showBluetoothFile)
        SettingsUtils.setClickListener(this, general)
        SettingsUtils.setClickListener(this, usercustomization)

        val sendLogPreference = findPreference<SwitchPreferenceCompat>("send_log")
        sendLogPreference?.onPreferenceChangeListener = this
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.windowBackground))
        setDivider(null)
        preferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences?.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
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
        listener.openCallFragment(ShowBluetoothFileFragment())
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = if (context is HomeInteractListener) context
                    else throw Exception("Context does not implement HomeInteractListener")
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean {
        if (preference.key == "send_log") {
            val value = newValue as? Boolean ?: false
            if (value) {
                dataSharing()
            } else {
                preferences.edit()?.putBoolean("send_log", false)?.apply()
            }
            (preference as? SwitchPreferenceCompat)?.isChecked = preferences.getBoolean("send_log", false)
        }
        return true
    }


    fun dataSharing() {
        val v = layoutInflater.inflate(R.layout.alert_log, null)
        val emoji = String(Character.toChars(0x1F60A))
        val builder = DialogUtils.createAlertDialog(activity,
            "Sharing is Caring  $emoji",
            "Treehouses wants to collect your activities. Do you like to share it? It will help us to improve.", v)
            .setCancelable(false)
        DialogUtils.createAdvancedDialog(builder, Pair("Continue", "Cancel"), {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                    REQUEST_LOCATION_PERMISSION_FOR_ACTIVITY_COLLECTION
                )
            } else {
                preferences.edit()?.putBoolean("send_log", true)?.apply()
            }
        }, {
            val sendLogPreference = findPreference<SwitchPreferenceCompat>("send_log")
            sendLogPreference?.isChecked = false
            preferences.edit()?.putBoolean("send_log", false)?.apply()
            MainApplication.showLogDialog = false
        })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_LOCATION_PERMISSION_FOR_ACTIVITY_COLLECTION -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    preferences.edit()?.putBoolean("send_log", true)?.apply()
                } else {
                    Toast.makeText(context, "Permission denied. We won't collect your activities", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        private const val REQUEST_LOCATION_PERMISSION_FOR_ACTIVITY_COLLECTION = 2
    }
}