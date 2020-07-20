package io.treehouses.remote.utils

import android.content.Context
import android.util.Log
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.trilead.ssh2.KnownHosts
import io.treehouses.remote.SSH.beans.KnownHostBean
import io.treehouses.remote.SSH.beans.PubKeyBean
import java.util.*

object KeyUtils {
    const val ALL_KEY_NAMES = "keys_names_for_ssh"
    const val ALL_KNOWN_HOSTS = "known_hosts_header"
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
        PreferenceManager.getDefaultSharedPreferences(context).edit().remove(keyName).apply()
        SaveUtils.removeFromArrayList(context, ALL_KEY_NAMES, keyName)
    }

    fun saveKnownHost(context: Context, hostNamePort: String, algorithm: String, key: ByteArray) {
        Log.e("SAVING KNOWN HOST", "$hostNamePort with key $key")
        val hostName  = hostNamePort.split(":")[0]
        val port = Integer.parseInt(hostNamePort.split(":")[1])

        val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
        editor.putString(hostNamePort, Gson().toJson(KnownHostBean(hostName, port, algorithm, key)))
        editor.apply()
        SaveUtils.addToArrayList(context, ALL_KNOWN_HOSTS, hostNamePort)
    }

    fun getKnownHost(context: Context, hostNamePort: String) : KnownHostBean? {
        val a = PreferenceManager.getDefaultSharedPreferences(context).getString(hostNamePort, "")
        a?.let {
            return try {
                Gson().fromJson(a, KnownHostBean::class.java)
            } catch (e: Exception) {
                Log.e("COULD NOT PARSE", "GETTING KNOWN HOST: $a", e)
                null
            }
        }
        return null
    }

    fun getAllKnownHosts(context: Context) : KnownHosts {
        val knownHosts = KnownHosts()
        SaveUtils.getStringList(context, ALL_KNOWN_HOSTS).forEach {
            val host = getKnownHost(context, it)
            try {
                if (host != null) {
                    Log.e("ALL HOSTS: ", "${host.hostName} with ${host.algorithm} and key ${host.pubKey}")
                    knownHosts.addHostkey(arrayOf(
                            String.format(Locale.US, "%s:%s", host.hostName, host.port.toString())
                    ), host.algorithm, host.pubKey)
                    Log.e("Added Host key", String.format(Locale.US, "%s%s", host.hostName, host.port.toString()))
                }
            } catch (e: Exception) {
                Log.e("Getting Known Hosts", "Problem when adding host to known hosts", e)
            }
        }
        return knownHosts
    }

    fun removeKnownHost(context: Context, hostNamePort: String, algorithm: String, key: ByteArray) {
        SaveUtils.removeFromArrayList(context, ALL_KNOWN_HOSTS, hostNamePort)
        PreferenceManager.getDefaultSharedPreferences(context).edit().remove(hostNamePort).apply()
    }

}