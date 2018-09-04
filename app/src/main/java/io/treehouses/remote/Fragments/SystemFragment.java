package io.treehouses.remote.Fragments;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import io.treehouses.remote.MiscOld.Constants;
import io.treehouses.remote.R;

public class SystemFragment extends Fragment{

    View view;

    public SystemFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_system_fragment, container, false);

        ArrayList<String> list = new ArrayList<String>();
        list.add("Reboot");
        list.add("Expand File System");
        list.add("Rename Hostname");
        list.add("RPI Password Settings");
        list.add("Container");
        list.add("Upgrade CLI");

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
            case 0:
                showEthernetDialog();
                break;
            case 1:
                showWifiDialog();
                break;
            case 2:
                showHotspotDialog();
                break;
            case 3:
                showBridgeDialog();
                break;
            case 4:
                showResetFragment();
                break;
            default:
                Log.e("Default Network Switch", "Nothing...");
        }
    }
    public void showBridgeDialog(){
        DialogFragment dialogFrag = BridgeDialogFragment.newInstance(123);
        dialogFrag.setTargetFragment(this, Constants.REQUEST_DIALOG_FRAGMENT_HOTSPOT);
        dialogFrag.show(getFragmentManager().beginTransaction(),"bridgeDialog");
    }
    public void showEthernetDialog(){
        DialogFragment dialogFrag = EthernetDialogFragment.newInstance(123);
        dialogFrag.setTargetFragment(this, Constants.REQUEST_DIALOG_FRAGMENT_HOTSPOT);
        dialogFrag.show(getFragmentManager().beginTransaction(),"ethernetDialog");
    }
    public void showHotspotDialog(){
        DialogFragment dialogFrag = HotspotDialogFragment.newInstance(123);
        dialogFrag.setTargetFragment(this, Constants.REQUEST_DIALOG_FRAGMENT_HOTSPOT);
        dialogFrag.show(getFragmentManager().beginTransaction(),"hotspotDialog");
    }
    public void showWifiDialog() {
        DialogFragment dialogFrag = WifiDialogFragment.newInstance(123);
        dialogFrag.setTargetFragment(this, Constants.REQUEST_DIALOG_FRAGMENT);
        dialogFrag.show(getFragmentManager().beginTransaction(), "wifiDialog");
    }
    public void showResetFragment(){
        DialogFragment dialogFrag = ResetFragment.newInstance(123);
        dialogFrag.setTargetFragment(this, Constants.REQUEST_DIALOG_FRAGMENT);
        dialogFrag.show(getFragmentManager().beginTransaction(), "resetDialog");
    }
}
