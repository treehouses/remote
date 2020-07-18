package io.treehouses.remote.utils

import android.content.Context
import android.util.Log
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import io.treehouses.remote.Fragments.HomeFragment
import io.treehouses.remote.SSH.beans.HostBean
import io.treehouses.remote.pojo.CommandListItem
import io.treehouses.remote.pojo.NetworkProfile
import java.security.KeyPair
import java.util.*

object SaveUtils {
    private const val DELIMITER = "#/@/#"
    private const val COMMANDS_TITLES_KEY = "commands_titles"
    private const val COMMANDS_VALUES_KEY = "commands_values"
    private const val NETWORK_PROFILES_KEY = "network_profile_keys"
    private const val SSH_HOSTS = "ssh_hosts_values"

    enum class Screens {
        FIRST_TIME, HOME, NETWORK, SYSTEM, TERMINAL, SERVICES_OVERVIEW, SERVICES_DETAILS, TUNNEL, STATUS
    }

    fun saveStringList(context: Context, list: MutableList<String>, arrayName: String) {
        var strArr = StringBuilder()
        for (value in list) {
            strArr.append(value).append(DELIMITER)
        }
        if (strArr.isNotEmpty()) {
            strArr = StringBuilder(strArr.substring(0, strArr.length - DELIMITER.length))
        }
        val e = PreferenceManager.getDefaultSharedPreferences(context).edit()
        e.putString(arrayName, strArr.toString())
        e.apply()
    }

    fun getStringList(context: Context, arrayName: String): MutableList<String> {
        val strList: List<String>
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val str = prefs.getString(arrayName, null)
        strList = if (str == null || str == "") {
            return ArrayList()
        } else {
            str.split(DELIMITER)
        }
        return strList.toMutableList()
    }

    private fun addToArrayList(context: Context, arrayName: String, toAdd: String) {
        val arrayList = getStringList(context, arrayName)
        arrayList.add(toAdd)
        saveStringList(context, arrayList, arrayName)
    }

    private fun removeFromArrayList(context: Context, arrayName: String, toRemove: String) {
        val arrayList = getStringList(context, arrayName)
        if (arrayList.isNotEmpty()) {
            arrayList.remove(toRemove)
            saveStringList(context, arrayList, arrayName)
        }
    }

    private fun clearArrayList(context: Context, arrayName: String) {
        saveStringList(context, ArrayList(), arrayName)
    }

    //TERMINAL COMMAND LIST UTILS
    @JvmStatic
    fun initCommandsList(context: Context) {
        if (getStringList(context, COMMANDS_TITLES_KEY).isEmpty() || getStringList(context, COMMANDS_VALUES_KEY).isEmpty()) {
            val titles = arrayOf("CHANGE PASSWORD", "HELP", "DOCKER PS", "DETECT RPI", "EXPAND FS",
                    "VNC ON", "VNC OFF", "VNC STATUS", "TOR", "NETWORK MODE INFO", "CLEAR")
            val commands = arrayOf("ACTION", "treehouses help", "docker ps", "treehouses detectrpi", "treehouses expandfs",
                    "treehouses vnc on", "treehouses vnc off", "treehouses vnc", "treehouses tor", "treehouses networkmode info", "ACTION")
            saveStringList(context, ArrayList(Arrays.asList(*titles)), COMMANDS_TITLES_KEY)
            saveStringList(context, ArrayList(Arrays.asList(*commands)), COMMANDS_VALUES_KEY)
        }
    }

    fun getCommandsList(context: Context): List<CommandListItem> {
        val titles = getStringList(context, COMMANDS_TITLES_KEY)
        val values = getStringList(context, COMMANDS_VALUES_KEY)
        val finalArray: MutableList<CommandListItem> = ArrayList()
        if (titles.isEmpty() || values.isEmpty()) {
            return ArrayList()
        }
        if (titles.size != values.size) {
            Log.e("COMMANDLIST", "ERROR SIZE: COMMANDS and VALUES")
            return ArrayList()
        }
        for (i in titles.indices) {
            finalArray.add(CommandListItem(titles[i], values[i]))
        }
        return finalArray
    }


    @JvmStatic
    fun addToCommandsList(context: Context, commandListItem: CommandListItem) {
        addToArrayList(context, COMMANDS_TITLES_KEY, commandListItem.getTitle())
        addToArrayList(context, COMMANDS_VALUES_KEY, commandListItem.getCommand())
    }

    fun clearCommandsList(context: Context) {
        clearArrayList(context, COMMANDS_TITLES_KEY)
        clearArrayList(context, COMMANDS_VALUES_KEY)
    }

    fun addProfile(context: Context, profile: NetworkProfile?) {
        val gson = Gson()
        addToArrayList(context, NETWORK_PROFILES_KEY, gson.toJson(profile))
    }

    fun getProfiles(context: Context): HashMap<String, MutableList<NetworkProfile>> {
        val gson = Gson()
        val json_profiles = getStringList(context, NETWORK_PROFILES_KEY)
        val wifi = ArrayList<NetworkProfile>()
        val hotspot = ArrayList<NetworkProfile>()
        val bridge = ArrayList<NetworkProfile>()
        for (i in json_profiles.indices) {
            val profile = gson.fromJson(json_profiles[i], NetworkProfile::class.java)
            when {
                profile.isWifi -> wifi.add(profile)
                profile.isHotspot -> hotspot.add(profile)
                profile.isBridge -> bridge.add(profile)
                else -> Log.e("SAVE UTILS", "Not a supported type")
            }
        }
        val profiles = HashMap<String, MutableList<NetworkProfile>>()
        profiles[HomeFragment.group_labels[0]] = wifi
        profiles[HomeFragment.group_labels[1]] = hotspot
        profiles[HomeFragment.group_labels[2]] = bridge
        return profiles
    }

    @JvmStatic
    fun deleteProfile(context: Context, groupPosition: Int, childPosition: Int) {
        val profiles = getProfiles(context)
        val networkProfile = profiles[HomeFragment.group_labels[groupPosition]]!![childPosition]
        val gson = Gson()
        removeFromArrayList(context, NETWORK_PROFILES_KEY, gson.toJson(networkProfile))
    }

    fun clearProfiles(context: Context) {
        clearArrayList(context, NETWORK_PROFILES_KEY)
    }

    fun getFragmentFirstTime(context: Context, which: Screens) : Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean(which.name, true)
    }

    fun setFragmentFirstTime(context: Context, which: Screens, firstTime: Boolean) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context).edit()
        prefs.putBoolean(which.name, firstTime)
        prefs.apply()
    }

    fun addHost(context: Context, hostBean: HostBean) {
        val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
        editor.putString(hostBean.uri.toString(), Gson().toJson(hostBean))
        editor.apply()
        addToArrayList(context, SSH_HOSTS, hostBean.uri.toString())
    }

    fun getHost(context: Context, hostUri: String) : HostBean? {
        val hostString = PreferenceManager.getDefaultSharedPreferences(context).getString(hostUri, "")
        return try {
            Gson().fromJson(hostString, HostBean::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun updateHostList (context: Context, hostBean: HostBean) {
        val allHosts = getAllHosts(context).toMutableList()
        if (allHosts.contains(hostBean)) {
            allHosts.remove(hostBean)
            allHosts.add(hostBean)
            saveStringList(context, allHosts.map { it.uri.toString() }.toMutableList(), SSH_HOSTS)
        } else {
            addHost(context, hostBean)
        }
    }

    fun getAllHosts(context: Context) : List<HostBean> {
        return getStringList(context, SSH_HOSTS).mapNotNull { getHost(context, it) }
    }
    
    fun deleteAllHosts(context: Context) {
        val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
        getAllHosts(context).forEach { editor.remove(it.uri.toString()) }
        editor.apply()
        clearArrayList(context, SSH_HOSTS)
    }
}