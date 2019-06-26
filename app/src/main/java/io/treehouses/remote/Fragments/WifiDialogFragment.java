package io.treehouses.remote.Fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ThemedSpinnerAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.treehouses.remote.R;
import io.treehouses.remote.WifiAdapter.WiFiManager;

public class WifiDialogFragment extends DialogFragment {

    private AlertDialog mDialog;
    private ArrayAdapter<String> arrayAdapter;
    private WifiManager wifiManager;
    private ArrayList<String> wifiList = new ArrayList<>();
    private Context context;
    private Boolean wait = false;

    public static androidx.fragment.app.DialogFragment newInstance() {
        WifiDialogFragment wifiDialogFragment = new WifiDialogFragment();
//        Bundle bundle = new Bundle();
//        bundle.putStringArrayList("wifiList", wifiList);
//        wifiDialogFragment.setArguments(bundle);
        return wifiDialogFragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        context = getContext();
        Thread thread = setupWifi();

        thread.join();

        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View mView = inflater.inflate(R.layout.activity_rpi_dialog_fragment, null);
        ListView listView = mView.findViewById(R.id.listView);
        mDialog = RPIDialogFragment.getInstance().getAlertDialog(mView, getContext(), true);
        mDialog.setTitle(R.string.select_device);

        Log.e("TAG", "SSID = " +  wifiList.toString());

        arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, wifiList);

        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mDialog.dismiss();
            }
        });

        return mDialog;
    }

    private Thread setupWifi() {
        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        Thread thread;

        thread = new Thread(() -> {
            BroadcastReceiver wifiScanReceiver = wifiBroadcastReceiver();

            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
            context.registerReceiver(wifiScanReceiver, intentFilter);

            boolean success = wifiManager.startScan();
            if (!success) {
                scanFailure();
            }
        });
        thread.start();
        return thread;
    }

    private BroadcastReceiver wifiBroadcastReceiver() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
                boolean success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);
                if (success) {
                    scanSuccess();
                    wait = false;

                } else {
                    scanFailure();
                }
            }
        };
    }

    private void scanSuccess() {
        List<ScanResult> results = wifiManager.getScanResults();
        Log.e("TAG", "Scan Success - scan results: " + results);

        // converts Object list to array
        Object[] object = results.toArray();
        String temp = Arrays.toString(object);
        String[] resultArray = temp.split(",");

        // extracts SSID from wifi data
        for (String s : resultArray) {
            if (s.contains("SSID") && !s.contains("BSSID")) {

                wifiList.add(s.substring(6));

                Log.e("TAG", "SSID = " + s.substring(6));
            }
        }
    }

    private void scanFailure() {
        // handle failure: new scan did NOT succeed
        // consider using old scan results
        List<ScanResult> results = wifiManager.getScanResults();
        Log.e("TAG", "Scan Failed - scan results: " + results);
    }
}
