package io.treehouses.remote.Fragments;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

import io.treehouses.remote.MiscOld.Constants;
import io.treehouses.remote.R;
import me.aflak.bluetooth_library.*;
import me.aflak.bluetooth.*;

public class HomeFragment extends Fragment {

    View view;

    public HomeFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_home_fragment, container, false);

        Button connectRpi = view.findViewById(R.id.button);
        connectRpi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRPIDialog();
            }
        });

        return view;
    }

    public void showRPIDialog(){
        DialogFragment dialogFrag = RPIDialogFragment.newInstance(123);
        dialogFrag.setTargetFragment(this, Constants.REQUEST_DIALOG_FRAGMENT_HOTSPOT);
        dialogFrag.show(getFragmentManager().beginTransaction(),"rpiDialog");
    }
}
