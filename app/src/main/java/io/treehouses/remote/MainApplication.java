package io.treehouses.remote;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.treehouses.remote.Fragments.TerminalFragment;

public class MainApplication extends Application {

    private static ArrayList terminalList, tunnelList, commandList;

    @Override
    public void onCreate() {
        super.onCreate();

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {

            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

               // new activity created; force its orientation to portrait
               activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }

            @Override
            public void onActivityStarted(Activity activity) { }
            @Override
            public void onActivityResumed(Activity activity) { }
            @Override
            public void onActivityPaused(Activity activity) { }
            @Override
            public void onActivityStopped(Activity activity) { }
            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) { }
            @Override
            public void onActivityDestroyed(Activity activity) { }
        });

        terminalList = new ArrayList();
        tunnelList = new ArrayList();
        commandList = new ArrayList();
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
