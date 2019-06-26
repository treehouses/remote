package io.treehouses.remote.WifiAdapter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import io.treehouses.remote.Constants;
import io.treehouses.remote.Fragments.WifiDialogFragment;
import io.treehouses.remote.bases.BaseFragment;

public class WiFiManager extends BaseFragment {

    private WifiManager wifiManager;
    private List<String> strings;
    private static WiFiManager instance = null;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

//        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//
//        BroadcastReceiver wifiScanReceiver = wifiBroadcastReceiver();
//
//        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
//        context.registerReceiver(wifiScanReceiver, intentFilter);
//
//        boolean success = wifiManager.startScan();
//        if (!success) {
//            scanFailure();
//        }
    }

//    @Override
//    public void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
////        instance = this;
//
//
//    }

    public static WiFiManager getInstance() {
        return instance;
    }



//    private void showWifiDialog(ArrayList networkList){
//        androidx.fragment.app.DialogFragment dialogFrag =  WifiDialogFragment.newInstance(networkList);
//        dialogFrag.setTargetFragment(this, Constants.REQUEST_DIALOG_FRAGMENT_HOTSPOT);
//        dialogFrag.show(getFragmentManager().beginTransaction(),"wifiDialog");
//    }
}
