package io.treehouses.remote.utils

import android.content.Context
import androidx.preference.PreferenceManager
import com.google.gson.*
import com.trilead.ssh2.KnownHosts
import io.treehouses.remote.ssh.PubKeyUtils
import io.treehouses.remote.ssh.beans.KnownHostBean
import io.treehouses.remote.ssh.beans.PubKeyBean
import java.lang.reflect.Type
import java.security.KeyPairGenerator
import java.security.PublicKey
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
            e.printStackTrace()
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
                e.printStackTrace()
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
                    val hostKey = String.format(Locale.US, "%s:%s", host.hostName, host.port.toString())
                    knownHosts.addHostkey(arrayOf(hostKey), host.algorithm, host.pubKey)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return knownHosts
    }

    fun removeKnownHost(context: Context, hostNamePort: String) {
        SaveUtils.removeFromArrayList(context, ALL_KNOWN_HOSTS, hostNamePort)
        PreferenceManager.getDefaultSharedPreferences(context).edit().remove(hostNamePort).apply()
    }

    fun getOpenSSH(pubkey: PubKeyBean) : String {
        val decodedPublic = PubKeyUtils.decodeKey(pubkey.publicKey!!, pubkey.type, "public")
        return PubKeyUtils.convertToOpenSSHFormat(decodedPublic as PublicKey, pubkey.nickname)
    }

    fun createSmartConnectKey(context: Context): PubKeyBean {
        val key = generateSmartConnectKey("SmartConnectKey", "RSA", "", 2048)
        saveKey(context, key)
        return key
    }

    private fun generateSmartConnectKey(name: String, algorithm: String, password: String, bitSize: Int): PubKeyBean {
        val keyPair = KeyPairGenerator.getInstance(algorithm).apply {
            initialize(bitSize)
        }.generateKeyPair()
        val encPrivate = PubKeyUtils.getEncodedPrivate(keyPair.private, password)
        val pubEncoded = keyPair.public.encoded
        return PubKeyBean(name, algorithm, encPrivate, pubEncoded)
    }
}