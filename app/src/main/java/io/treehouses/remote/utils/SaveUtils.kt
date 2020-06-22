package io.treehouses.remote.utils

import android.content.Context
import androidx.preference.PreferenceManager
import android.util.Log
import com.google.gson.Gson
import io.treehouses.remote.Fragments.HomeFragment
import io.treehouses.remote.pojo.CommandListItem
import io.treehouses.remote.pojo.NetworkProfile
import java.util.*

object SaveUtils {
    private const val DELIMITER = "#/@/#"
    private const val COMMANDS_TITLES_KEY = "commands_titles"
    private const val COMMANDS_VALUES_KEY = "commands_values"
    private const val NETWORK_PROFILES_KEY = "network_profile_keys"

    enum class Screens {
        HOME, NETWORK, SYSTEM, TERMINAL, SERVICES_OVERVIEW, SERVICES_DETAILS, TUNNEL, STATUS
    }

    private fun saveStringArray(context: Context, array: ArrayList<String>, arrayName: String) {
        var strArr = StringBuilder()
        for (i in array.indices) {
            strArr.append(array[i]).append(DELIMITER)
        }
        if (strArr.length != 0) {
            strArr = StringBuilder(strArr.substring(0, strArr.length - DELIMITER.length))
        }
        val e = PreferenceManager.getDefaultSharedPreferences(context).edit()
        e.putString(arrayName, strArr.toString())
        e.apply()
    }

    private fun getStringArray(context: Context, arrayName: String): ArrayList<String> {
        val strArr: Array<String>
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val str = prefs.getString(arrayName, null)
        strArr = if (str == null || str == "") {
            return ArrayList()
        } else {
            str.split(DELIMITER.toRegex()).toTypedArray()
        }
        return ArrayList(Arrays.asList(*strArr))
    }

    private fun addToArrayList(context: Context, arrayName: String, toAdd: String) {
        val arrayList = getStringArray(context, arrayName)
        arrayList.add(toAdd)
        saveStringArray(context, arrayList, arrayName)
    }

    private fun removeFromArrayList(context: Context, arrayName: String, toRemove: String) {
        val arrayList = getStringArray(context, arrayName)
        if (!arrayList.isEmpty()) {
            arrayList.remove(toRemove)
            saveStringArray(context, arrayList, arrayName)
        }
    }

    private fun clearArrayList(context: Context, arrayName: String) {
        saveStringArray(context, ArrayList(), arrayName)
    }

    //TERMINAL COMMAND LIST UTILS
    @JvmStatic
    fun initCommandsList(context: Context) {
        if (getStringArray(context, COMMANDS_TITLES_KEY).isEmpty() || getStringArray(context, COMMANDS_VALUES_KEY).isEmpty()) {
            val titles = arrayOf("CHANGE PASSWORD", "HELP", "DOCKER PS", "DETECT RPI", "EXPAND FS",
                    "VNC ON", "VNC OFF", "VNC STATUS", "TOR", "NETWORK MODE INFO", "CLEAR")
            val commands = arrayOf("ACTION", "treehouses help", "docker ps", "treehouses detectrpi", "treehouses expandfs",
                    "treehouses vnc on", "treehouses vnc off", "treehouses vnc", "treehouses tor", "treehouses networkmode info", "ACTION")
            saveStringArray(context, ArrayList(Arrays.asList(*titles)), COMMANDS_TITLES_KEY)
            saveStringArray(context, ArrayList(Arrays.asList(*commands)), COMMANDS_VALUES_KEY)
        }
    }

    fun getCommandsList(context: Context): List<CommandListItem> {
        val titles = getStringArray(context, COMMANDS_TITLES_KEY)
        val values = getStringArray(context, COMMANDS_VALUES_KEY)
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

    fun getProfiles(context: Context): HashMap<String, List<NetworkProfile>> {
        val gson = Gson()
        val json_profiles = getStringArray(context, NETWORK_PROFILES_KEY)
        val wifi = ArrayList<NetworkProfile>()
        val hotspot = ArrayList<NetworkProfile>()
        val bridge = ArrayList<NetworkProfile>()
        for (i in json_profiles.indices) {
            val profile = gson.fromJson(json_profiles[i], NetworkProfile::class.java)
            if (profile.isWifi) {
                wifi.add(profile)
            } else if (profile.isHotspot) {
                hotspot.add(profile)
            } else if (profile.isBridge) {
                bridge.add(profile)
            } else {
                Log.e("SAVE UTILS", "Not a supported type")
            }
        }
        val profiles = HashMap<String, List<NetworkProfile>>()
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
}