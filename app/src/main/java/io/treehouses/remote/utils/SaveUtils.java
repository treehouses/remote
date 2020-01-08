package io.treehouses.remote.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import io.treehouses.remote.pojo.CommandListItem;
import io.treehouses.remote.pojo.NetworkProfile;

public class SaveUtils {
    private static final String DELIMITER = "#/@/#";

    public static final String COMMANDS_TITLES_KEY = "commands_titles";
    public static final String COMMANDS_VALUES_KEY = "commands_values";

    public static final String SSIDS_KEY = "essids_names";
    public static final String PASSWORDS_KEY = "passwords_keys";
    public static final String OPTIONS_KEY = "options_keys";

    public static final String ACTION_KEYWORD = "ACTION";

    public static final String NONE = "/@/";

    public static void saveStringArray(Context context, ArrayList<String> array, String arrayName) {
        String strArr = "";
        for (int i=0; i<array.size(); i++) {
            strArr += array.get(i) + DELIMITER;
        }
        if (strArr.length() != 0) {
            strArr = strArr.substring(0, strArr.length() - DELIMITER.length());
        }

        SharedPreferences.Editor e = PreferenceManager.getDefaultSharedPreferences(context).edit();
        e.putString(arrayName, strArr);
        e.commit();
    }

    public static ArrayList<String> getStringArray(Context context, String arrayName) {
        String[] strArr;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String str = prefs.getString(arrayName, null);
        if (str == null || str.equals("")) {
            return new ArrayList<String>();
        }
        else {
            strArr = str.split(DELIMITER);
        }
        return new ArrayList<>(Arrays.asList(strArr));
    }

    public static void addToArrayList(Context context, String arrayName, String toAdd) {
        ArrayList<String> arrayList = getStringArray(context, arrayName);
        arrayList.add(toAdd);
        saveStringArray(context, arrayList, arrayName);
    }

    public static Boolean removeFromArrayList(Context context, String arrayName, String toRemove) {
        ArrayList<String> arrayList = getStringArray(context, arrayName);
        if (!arrayList.isEmpty()) {
            arrayList.remove(toRemove);
            return true;
        }
        return false;
    }

    public static void clearArrayList(Context context, String arrayName) {
        saveStringArray(context, new ArrayList<>(), arrayName);
    }

    //TERMINAL COMMAND LIST UTILS

    public static void initCommandsList(Context context) {
        if (getStringArray(context, COMMANDS_TITLES_KEY).isEmpty() || getStringArray(context, COMMANDS_VALUES_KEY).isEmpty()) {
            String[] titles = {"CHANGE PASSWORD", "HELP", "DOCKER PS", "DETECT RPI", "EXPAND FS",
                    "VNC ON", "VNC OFF", "VNC STATUS", "TOR", "NETWORK MODE INFO", "CLEAR"};
            String[] commands = {"ACTION", "treehouses help", "docker ps", "treehouses detectrpi", "treehouses expandfs",
                    "treehouses vnc on", "treehouses vnc off", "treehouses vnc", "treehouses tor", "treehouses networkmode info", "ACTION"};

            saveStringArray(context, new ArrayList<>(Arrays.asList(titles)), COMMANDS_TITLES_KEY);
            saveStringArray(context, new ArrayList<>(Arrays.asList(commands)), COMMANDS_VALUES_KEY);
        }
    }

    public static List<CommandListItem> getCommandsList(Context context) {
        ArrayList<String> titles = getStringArray(context, COMMANDS_TITLES_KEY);
        ArrayList<String> values = getStringArray(context, COMMANDS_VALUES_KEY);

        List<CommandListItem> finalArray = new ArrayList<>();

        if (titles.isEmpty() || values.isEmpty()) {
            return new ArrayList<CommandListItem>();
        }
        if (titles.size() != values.size()) {
            Log.e("COMMANDLIST", "ERROR SIZE: COMMANDS and VALUES");
            return new ArrayList<>();
        }

        for (int i = 0; i < titles.size(); i++) {
            finalArray.add(new CommandListItem(titles.get(i), values.get(i)));
        }
        return finalArray;
    }

    public static void saveCommandsList(Context context, List<CommandListItem> newCommandList) {
        ArrayList<String> titles = new ArrayList<>();
        ArrayList<String> commands = new ArrayList<>();

        for (CommandListItem commandListItem : newCommandList) {
            titles.add(commandListItem.getTitle());
            commands.add(commandListItem.getCommand());
        }

        saveStringArray(context, titles, COMMANDS_TITLES_KEY);
        saveStringArray(context, commands, COMMANDS_VALUES_KEY);

    }

    public static void addToCommandsList(Context context, CommandListItem commandListItem) {
        addToArrayList(context, COMMANDS_TITLES_KEY, commandListItem.getTitle());
        addToArrayList(context, COMMANDS_VALUES_KEY, commandListItem.getCommand());
    }

    public static void removeFromCommandsList(Context context, CommandListItem commandListItem) {
        removeFromArrayList(context, COMMANDS_TITLES_KEY, commandListItem.getTitle());
        removeFromArrayList(context, COMMANDS_VALUES_KEY, commandListItem.getCommand());
    }

    public static void clearCommandsList (Context context) {
        clearArrayList(context, COMMANDS_TITLES_KEY);
        clearArrayList(context, COMMANDS_VALUES_KEY);
    }

    // Network Profiles
    private static String nonEmpty (String s) {
        if( s.equals("") || s.equals(" ")) {
            return NONE;
        }
        return s;
    }

    public static void addProfile(Context context, NetworkProfile profile) {
        addToArrayList(context, SSIDS_KEY, profile.ssid);
        addToArrayList(context, PASSWORDS_KEY, nonEmpty(profile.password));
        addToArrayList(context, OPTIONS_KEY, nonEmpty(profile.option));
    }

    public static HashMap<String, List<NetworkProfile>> getProfiles(Context context) {
        ArrayList<String> essids = getStringArray(context, SSIDS_KEY);
        ArrayList<String> passwords = getStringArray(context, PASSWORDS_KEY);
        ArrayList<String> options = getStringArray(context, OPTIONS_KEY);

        ArrayList<NetworkProfile> wifi = new ArrayList<>();
        ArrayList<NetworkProfile> hotspot = new ArrayList<>();
        for (int i = 0; i < essids.size(); i++) {
            if (options.get(i).equals(NONE)) {
                wifi.add(new NetworkProfile(essids.get(i), passwords.get(i)));
            }
            else {
                hotspot.add(new NetworkProfile(essids.get(i), passwords.get(i),options.get(i)));
            }
        }
        HashMap<String, List<NetworkProfile>> profiles = new HashMap<>();
        profiles.put("WIFI", wifi);
        profiles.put("Hotspot", hotspot);
        return profiles;
    }

    public static void clearProfiles(Context context) {
        clearArrayList(context, SSIDS_KEY);
        clearArrayList(context, PASSWORDS_KEY);
        clearArrayList(context, OPTIONS_KEY);
    }
}
