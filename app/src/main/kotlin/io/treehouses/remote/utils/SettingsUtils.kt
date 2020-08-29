package io.treehouses.remote.utils

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.preference.Preference
import io.treehouses.remote.R

object SettingsUtils {
    fun openFragment(isParent: Boolean, fragmentTransaction: FragmentTransaction, f: Fragment) {
        fragmentTransaction.replace(R.id.fragment_container, f)
        if (isParent) fragmentTransaction.addToBackStack("")
        else fragmentTransaction.addToBackStack(null)
        try {
            fragmentTransaction.commit()
        } catch (exception:IllegalStateException ){
            LogUtils.log("Error $exception")
        }
    }

    fun setClickListener(listener: Preference.OnPreferenceClickListener, preference: Preference?) {
        if (preference != null) {
            preference.onPreferenceClickListener = listener
        } else {
            LogUtils.log("SETTINGS Unknown key")
        }
    }
}