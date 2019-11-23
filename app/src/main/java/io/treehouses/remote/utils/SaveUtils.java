package io.treehouses.remote.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.treehouses.remote.pojo.CommandListItem;

public class SaveUtils {
    private static final String DELIMITER = "#/@/#";

    public static final String COMMANDS_TITLES_KEY = "commands_titles";
    public static final String COMMANDS_VALUES_KEY = "commands_values";

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
            return null;
        }
        else {
            strArr = str.split(DELIMITER);
        }
        return new ArrayList<>(Arrays.asList(strArr));
    }

    public static void addToArrayList(Context context, String arrayName, String toAdd) {
        ArrayList<String> arrayList = getStringArray(context, arrayName);
        if (arrayList == null) arrayList = new ArrayList<>();
        arrayList.add(toAdd);
        saveStringArray(context, arrayList, arrayName);
    }

    public static Boolean removeFromArrayList(Context context, String arrayName, String toRemove) {
        ArrayList<String> arrayList = getStringArray(context, arrayName);
        if (arrayList != null) {
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
        if (getStringArray(context, COMMANDS_TITLES_KEY) == null || getStringArray(context, COMMANDS_VALUES_KEY) == null) {
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

        if (titles == null || values == null || titles.size() != values.size()) {
            Log.e("COMMANDLIST", "ERROR SIZE: COMMANDS and VALUES");
            return new ArrayList<CommandListItem>();
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
}
