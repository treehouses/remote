package io.treehouses.remote;

import android.app.Application;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.treehouses.remote.Fragments.TerminalFragment;

public class MainApplication extends Application {

    private static ArrayList terminalList;
    private static ArrayList tunnelList;

    @Override
    public void onCreate() {
        super.onCreate();
        terminalList = new ArrayList();
        tunnelList = new ArrayList();
    }

    public static ArrayList getTerminalList() {
        return terminalList;
    }

    public static ArrayList getTunnelList() {
        return tunnelList;
    }
}
