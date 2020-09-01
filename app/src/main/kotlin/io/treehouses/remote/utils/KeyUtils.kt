package io.treehouses.remote.utils

import android.content.Context
import android.util.Log
import androidx.preference.PreferenceManager
import com.google.gson.*
import com.trilead.ssh2.KnownHosts
import io.treehouses.remote.SSH.beans.KnownHostBean
import io.treehouses.remote.SSH.beans.PubKeyBean
import java.lang.reflect.Type
import java.util.*


object KeyUtils {
    const val ALL_KEY_NAMES = "keys_names_for_ssh"
    const val ALL_KNOWN_HOSTS = "known_hosts_header"
    fun getAllKeyNames(context: Context) : MutableList<String> {
        return SaveUtils.getStringList(context, ALL_KEY_NAMES)
    }
    val customGson = GsonBuilder().registerTypeHierarchyAdapter(ByteArray::class.java, ByteArrayToBase64TypeAdapter()).create()

    // Using Android's base64 libraries. This can be replaced with any base64 library.
    private class ByteArrayToBase64TypeAdapter : JsonSerializer<ByteArray?>, JsonDeserializer<ByteArray?> {
        @Throws(JsonParseException::class)
        override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext?): ByteArray {
            return android.util.Base64.decode(json.asString, android.util.Base64.DEFAULT)
        }

        override fun serialize(src: ByteArray?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
            return JsonPrimitive(android.util.Base64.encodeToString(src, android.util.Base64.DEFAULT))
        }
    }

    fun saveKey(context: Context, key: PubKeyBean) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context).edit()

        prefs.putString(key.nickname, customGson.toJson(key))
        prefs.apply()

        SaveUtils.addToArrayList(context, ALL_KEY_NAMES, key.nickname)
    }

    fun getKey(context: Context, keyName:String) : PubKeyBean? {
        val string = PreferenceManager.getDefaultSharedPreferences(context).getString(keyName, "")
        return try {
            customGson.fromJson(string, PubKeyBean::class.java)
        } catch (e: Exception) {
            logD("Get Key $e")
            null
        }
    }

    fun getAllKeys(context: Context) : List<PubKeyBean> {
        val allKeys = mutableListOf<PubKeyBean>()
        getAllKeyNames(context).forEach {
            val key = getKey(context, it)
            if (key != null) allKeys.add(key)
        }
        return allKeys
    }

    fun deleteKey(context: Context, keyName: String) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().remove(keyName).apply()
        SaveUtils.removeFromArrayList(context, ALL_KEY_NAMES, keyName)
    }

    fun deleteAllKeys(context: Context) {
        val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
        getAllKeyNames(context).forEach { editor.remove(it) }
        editor.apply()
        SaveUtils.clearArrayList(context, ALL_KEY_NAMES)
    }

    fun saveKnownHost(context: Context, hostNamePort: String, algorithm: String, key: ByteArray) {
        logD("SAVING KNOWN HOST $hostNamePort with key $key")
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
                logE("COULD NOT PARSE GETTING KNOWN HOST: $a")
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
                    logD("ALL HOSTS: ${host.hostName} with ${host.algorithm} and key ${host.pubKey}")
                    val hostKey = String.format(Locale.US, "%s:%s", host.hostName, host.port.toString())
                    knownHosts.addHostkey(arrayOf(hostKey), host.algorithm, host.pubKey)
                    logD("Added Host key $hostKey")
                }
            } catch (e: Exception) {
                logD("Getting Known Hosts Problem when adding host to known hosts $e")
            }
        }
        return knownHosts
    }

    fun removeKnownHost(context: Context, hostNamePort: String, algorithm: String, key: ByteArray) {
        SaveUtils.removeFromArrayList(context, ALL_KNOWN_HOSTS, hostNamePort)
        PreferenceManager.getDefaultSharedPreferences(context).edit().remove(hostNamePort).apply()
    }

}