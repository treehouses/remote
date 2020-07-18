package io.treehouses.remote.utils

import android.content.Context
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import io.treehouses.remote.SSH.beans.PubKeyBean

object KeyUtils {
    const val ALL_KEY_NAMES = "keys_names_for_ssh"
    fun getAllKeyNames(context: Context) : MutableList<String> {
        return SaveUtils.getStringList(context, ALL_KEY_NAMES)
    }

    private fun saveKeyPair(context: Context, key: PubKeyBean) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context).edit()
        prefs.putString(key.nickname, Gson().toJson(key))
    }

}