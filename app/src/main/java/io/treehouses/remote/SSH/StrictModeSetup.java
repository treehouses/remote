package io.treehouses.remote.SSH;

import android.annotation.TargetApi;
import android.os.StrictMode;

@TargetApi(9)
public class StrictModeSetup {
    public static void run() {
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.LAX);
    }
}