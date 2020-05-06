package io.treehouses.remote;


import androidx.multidex.MultiDexApplication;

import com.parse.Parse;
import com.polidea.rxandroidble2.LogConstants;
import com.polidea.rxandroidble2.LogOptions;
import com.polidea.rxandroidble2.RxBleClient;

import java.util.ArrayList;

import io.treehouses.remote.utils.SaveUtils;

public class MainApplication extends MultiDexApplication {

    private static ArrayList terminalList, tunnelList, commandList;
    public static boolean showLogDialog = true;
    public static boolean ratingDialog = true;
    public static RxBleClient rxBleClient;
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

        rxBleClient = RxBleClient.create(this);
        RxBleClient.updateLogOptions(new LogOptions.Builder()
                .setLogLevel(LogConstants.INFO)
                .setMacAddressLogSetting(LogConstants.MAC_ADDRESS_FULL)
                .setUuidsLogSetting(LogConstants.UUIDS_FULL)
                .setShouldLogAttributeValues(true)
                .build());
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
