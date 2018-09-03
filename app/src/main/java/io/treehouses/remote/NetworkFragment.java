package io.treehouses.remote;

import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import io.treehouses.remote.MiscOld.Constants;
import io.treehouses.remote.Network.HotspotDialogFragment;
import io.treehouses.remote.Network.WifiDialogFragment;

public class NetworkFragment extends Fragment {

    public NetworkFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_network_fragment, container, false);
        ArrayList<String> list = new ArrayList<String>();
        list.add("Ethernet");
        list.add("Wi-Fi");
        list.add("Hotspot");
        list.add("Bridge");
        list.add("Reset");

        ListView listView = (ListView) view.findViewById(R.id.listView);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, list);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                getListFragment(position);
            }
        });

        return view;
    }

    public void getListFragment(int position){
        switch (position){
            case 1:
                showWifiDialog();
                break;
            case 2:
                showHotspotDialog();
                break;
            default:
                Log.e("Default Network Switch", "Nothing...");
        }
    }
    public void showHotspotDialog(){
        // Create an instance of the dialog fragment and show it
        DialogFragment dialogFrag = HotspotDialogFragment.newInstance(123);
        dialogFrag.setTargetFragment(this, Constants.REQUEST_DIALOG_FRAGMENT_HOTSPOT);
        dialogFrag.show(getFragmentManager().beginTransaction(),"hotspotDialog");
    }
    public void showWifiDialog() {
        // Create an instance of the dialog fragment and show it
        DialogFragment dialogFrag = WifiDialogFragment.newInstance(123);
        dialogFrag.setTargetFragment(this, Constants.REQUEST_DIALOG_FRAGMENT);
        dialogFrag.show(getFragmentManager().beginTransaction(), "wifiDialog");
    }

//    private Fragment getCurrentFragment(){
//        FragmentManager fragmentManager = getSupportFragmentManager();
//        String fragmentTag = fragmentManager.getBackStackEntryAt(fragmentManager.getBackStackEntryCount() - 1).getName();
//        Fragment currentFragment = fragmentManager.findFragmentByTag(fragmentTag);
//        return currentFragment;
//    }
//
//    @Override
//    public void onBackPressed() {
//        Fragment fragmentBeforeBackPress = getCurrentFragment();
//        // Perform the usual back action
//        super.onBackPressed();
//        Fragment fragmentAfterBackPress = getCurrentFragment();
//    }
}
