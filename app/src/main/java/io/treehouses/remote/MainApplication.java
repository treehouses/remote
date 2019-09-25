package io.treehouses.remote;

import android.app.Application;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;

public class MainApplication extends Application {

    private static ArrayList terminalList, tunnelList, commandList;
    public static FirebaseAnalytics mFirebaseAnalytics;
    @Override
    public void onCreate() {
        super.onCreate();
        terminalList = new ArrayList();
        tunnelList = new ArrayList();
        commandList = new ArrayList();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }

    public static ArrayList getTerminalList() {
        return terminalList;
    }

    public static ArrayList getTunnelList() {
        return tunnelList;
    }

    public static ArrayList getCommandList() {
        return commandList;
    }
}
