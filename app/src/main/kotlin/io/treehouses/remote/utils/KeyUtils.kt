package io.treehouses.remote.utils

import android.content.Context
import android.util.Log
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import io.treehouses.remote.SSH.beans.PubKeyBean

object KeyUtils {
    const val ALL_KEY_NAMES = "keys_names_for_ssh"
    fun getAllKeyNames(context: Context) : MutableList<String> {
        return SaveUtils.getStringList(context, ALL_KEY_NAMES)
    }

    fun saveKey(context: Context, key: PubKeyBean) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context).edit()
        prefs.putString(key.nickname, Gson().toJson(key))
        prefs.apply()

        SaveUtils.addToArrayList(context, ALL_KEY_NAMES, key.nickname)
    }

    fun getKey(context: Context, keyName:String) : PubKeyBean? {
        val string = PreferenceManager.getDefaultSharedPreferences(context).getString(keyName, "")
        return try {
            Gson().fromJson(string, PubKeyBean::class.java)
        } catch (e: Exception) {
            Log.e("Get Key", e.message, e)
            null
        }
    }

    fun deleteKey(context: Context, keyName: String) {
        //TODO("Delete key")
    }

}