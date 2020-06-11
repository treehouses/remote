package io.treehouses.remote.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import io.treehouses.remote.Fragments.HomeFragment;
import io.treehouses.remote.pojo.CommandListItem;
import io.treehouses.remote.pojo.NetworkProfile;

public class SaveUtils {
    private static final String DELIMITER = "#/@/#";

    private static final String COMMANDS_TITLES_KEY = "commands_titles";
    private static final String COMMANDS_VALUES_KEY = "commands_values";

    private static final String NETWORK_PROFILES_KEY = "network_profile_keys";

    public static final String ACTION_KEYWORD = "ACTION";

    private static void saveStringArray(Context context, ArrayList<String> array, String arrayName) {
        StringBuilder strArr = new StringBuilder();
        for (int i=0; i<array.size(); i++) {
            strArr.append(array.get(i)).append(DELIMITER);
        }
        if (strArr.length() != 0) {
            strArr = new StringBuilder(strArr.substring(0, strArr.length() - DELIMITER.length()));
        }

        SharedPreferences.Editor e = PreferenceManager.getDefaultSharedPreferences(context).edit();
        e.putString(arrayName, strArr.toString());
        e.apply();
    }

    private static ArrayList<String> getStringArray(Context context, String arrayName) {
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

    private static void addToArrayList(Context context, String arrayName, String toAdd) {
        ArrayList<String> arrayList = getStringArray(context, arrayName);
        arrayList.add(toAdd);
        saveStringArray(context, arrayList, arrayName);
    }

    private static void removeFromArrayList(Context context, String arrayName, String toRemove) {
        ArrayList<String> arrayList = getStringArray(context, arrayName);
        if (!arrayList.isEmpty()) {
            arrayList.remove(toRemove);
            saveStringArray(context, arrayList, arrayName);
        }
    }

    private static void clearArrayList(Context context, String arrayName) {
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
            return new ArrayList<>();
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

    public static void addProfile(Context context, NetworkProfile profile) {
        Gson gson = new Gson();
        addToArrayList(context, NETWORK_PROFILES_KEY, gson.toJson(profile));
    }

    public static HashMap<String, List<NetworkProfile>> getProfiles(Context context) {
        Gson gson = new Gson();
        ArrayList<String> json_profiles = getStringArray(context, NETWORK_PROFILES_KEY);
        ArrayList<NetworkProfile> wifi = new ArrayList<>();
        ArrayList<NetworkProfile> hotspot = new ArrayList<>();
        ArrayList<NetworkProfile> bridge = new ArrayList<>();
        for (int i = 0; i < json_profiles.size(); i++) {
            NetworkProfile profile = gson.fromJson(json_profiles.get(i), NetworkProfile.class);
            if (profile.isWifi()) {
                wifi.add(profile);
            }
            else if (profile.isHotspot()) {
                hotspot.add(profile);
            }
            else if (profile.isBridge()) {
                bridge.add(profile);
            }
            else {
                Log.e("SAVE UTILS", "Not a supported type");
            }
        }
        HashMap<String, List<NetworkProfile>> profiles = new HashMap<>();
        profiles.put(HomeFragment.group_labels[0], wifi);
        profiles.put(HomeFragment.group_labels[1], hotspot);
        profiles.put(HomeFragment.group_labels[2], bridge);
        return profiles;
    }

    public static void deleteProfile(Context context, int groupPosition, int childPosition) {
        HashMap<String, List<NetworkProfile>> profiles = getProfiles(context);
        NetworkProfile networkProfile = profiles.get(HomeFragment.group_labels[groupPosition]).get(childPosition);
        Gson gson = new Gson();
        removeFromArrayList(context, NETWORK_PROFILES_KEY, gson.toJson(networkProfile));
    }

    public static void clearProfiles(Context context) {
        clearArrayList(context, NETWORK_PROFILES_KEY);
    }
}
