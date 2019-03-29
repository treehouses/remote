package io.treehouses.remote;

import android.app.Application;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.treehouses.remote.Fragments.TerminalFragment;

public class MainApplication extends Application {

    private static ArrayList terminalList;
    private static boolean onResume;

    @Override
    public void onCreate() {
        super.onCreate();
        terminalList = new ArrayList();
        Log.e("tag", "MainApplication onResume called");
        onResume = false;
    }

    public static ArrayList getTerminalList() {
        return terminalList;
    }

    public static boolean getOnResume() {
        return onResume;
    }

    public static void setOnResume() {
        if (onResume) {
            onResume = false;
        } else {
            onResume = true;
        }
    }
}
