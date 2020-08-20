package io.treehouses.remote.bases

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.View
import androidx.core.content.ContextCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import io.treehouses.remote.InitialActivity
import io.treehouses.remote.R
import io.treehouses.remote.callback.BackPressReceiver

abstract class BasePreferenceFragment: PreferenceFragmentCompat(), BackPressReceiver, Preference.OnPreferenceClickListener {
    protected var preferenceChangeListener: SharedPreferences.OnSharedPreferenceChangeListener? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.windowBackground))
        setDivider(null)
        (requireActivity() as InitialActivity).changeAppBar()
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    override fun onBackPressed() {
        parentFragmentManager.popBackStack()
        (requireActivity() as InitialActivity).resetMenuIcon()
    }

    protected fun createAlertDialog(title: String, message: String, positive: String, ID: Int) {
        val dialog = AlertDialog.Builder(ContextThemeWrapper(context, R.style.CustomAlertDialogStyle))
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positive) { _: DialogInterface?, _: Int -> onClickDialog(ID) }
                .setNegativeButton("Cancel") { _: DialogInterface?, _: Int -> }
                .create()
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    protected abstract fun onClickDialog(id: Int)
}