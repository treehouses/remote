package io.treehouses.remote.Fragments.DialogFragments;

import android.app.AlertDialog;
import android.app.Dialog;
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
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.fragment.app.DialogFragment;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import io.treehouses.remote.ButtonConfiguration;
import io.treehouses.remote.R;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class WifiDialogFragment extends DialogFragment {

    private AlertDialog mDialog;
    private WifiManager wifiManager;
    private ArrayList<String> wifiList = new ArrayList<>();
    private Context context;
    private String SSID;
    private View mView;
    private Boolean firstScan = true;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        mView = inflater.inflate(R.layout.activity_rpi_dialog_fragment, null);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        context = getContext();

        setupWifi();

        mDialog = RPIDialogFragment.getInstance().getAlertDialog(mView, getContext(), true);
        mDialog.setTitle("Choose a network");

        Log.e("TAG", "SSID = " +  wifiList.toString());

        return mDialog;
    }

    public static androidx.fragment.app.DialogFragment newInstance() {
        return new WifiDialogFragment();
    }

    private void setAdapter() {
        ListView listView = mView.findViewById(R.id.listView);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, wifiList);

        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            SSID = wifiList.get(position);
            ButtonConfiguration.getSSID().setText(SSID.trim());
            wifiList.clear();
            mDialog.dismiss();
        });
    }

    private void setupWifi() {
        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager == null)
            return;
        wifiManager.setWifiEnabled(true);
        BroadcastReceiver wifiScanReceiver = wifiBroadcastReceiver();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        context.registerReceiver(wifiScanReceiver, intentFilter);

        boolean success = wifiManager.startScan();
        if (!success) {
            scanFailure();
        }
    }

    private BroadcastReceiver wifiBroadcastReceiver() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
                boolean success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);
                if (success) {
                    scanSuccess();
                } else if (firstScan) {
                    scanFailure();
                }
            }
        };
    }

    private void getSSIDs(List<ScanResult> results) {
        wifiList.clear();
        // converts Object list to array
        Object[] object = results.toArray();
        String temp = Arrays.toString(object);
        String[] resultArray = temp.split(",");

        // extracts SSID from wifi data
        for (String s : resultArray) {
            if (s.contains("SSID") && !s.contains("BSSID")) {

                String ssid = s.substring(6);

                // add to list if SSID is not hidden
                addToList(ssid);

                Log.e("TAG", "SSID = " + ssid);
            }
        }
    }

    private void addToList(String ssid) {
        if (!ssid.equals("")) {
            wifiList.add(ssid);
        }
    }

    private void scanSuccess() {
        List<ScanResult> results = wifiManager.getScanResults();
        Log.e("TAG", "Scan Success - scan results: " + results);

        getSSIDs(results);
        setAdapter();
    }

    private void scanFailure() {
        // handle failure: new scan did not succeed
        List<ScanResult> results = wifiManager.getScanResults();
        Log.e("TAG", "Scan Failed - scan results: " + results);

        getSSIDs(results);

        if (results.size() >= 1 && firstScan) {
            Toast.makeText(context, "Scan unsuccessful. These are old results", Toast.LENGTH_LONG).show();
            setAdapter();
        } else if (results.size() < 1 && firstScan) {
            ifResultListEmpty();
        }
        firstScan = false;

    }

    private void ifResultListEmpty() {
        Toast.makeText(context, "Scan unsuccessful, please try again.", Toast.LENGTH_LONG).show();
        if (mDialog != null) {
            mDialog.dismiss();
        }
    }
}
