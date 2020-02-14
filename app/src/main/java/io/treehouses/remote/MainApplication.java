package io.treehouses.remote;

import android.app.Application;

import com.parse.Parse;

import java.util.ArrayList;

import io.treehouses.remote.utils.SaveUtils;

public class MainApplication extends Application {

    private static ArrayList terminalList, tunnelList, commandList;
    public static boolean showLogDialog = true;
    @Override
    public void onCreate() {
        super.onCreate();
        terminalList = new ArrayList();
        tunnelList = new ArrayList();
        commandList = new ArrayList();
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId(Constants.PARSE_APPLICATION_ID)
                .clientKey(null)
                .server(Constants.PARSE_URL)
                .build()
        );
        SaveUtils.initCommandsList(getApplicationContext());
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
