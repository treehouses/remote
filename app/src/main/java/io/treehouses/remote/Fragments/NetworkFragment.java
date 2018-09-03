package io.treehouses.remote.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.os.Bundle;
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

public class NetworkFragment extends Fragment {

    View view;

    public NetworkFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_network_fragment, container, false);
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
//                final AlertDialog mDialog = getAlertDialog(view);
//                mDialog.show();
                showResetFragment();
                break;
            default:
                Log.e("Default Network Switch", "Nothing...");
        }
    }
    public void showBridgeDialog(){
        // Create an instance of the dialog fragment and show it
        DialogFragment dialogFrag = BridgeDialogFragment.newInstance(123);
        dialogFrag.setTargetFragment(this, Constants.REQUEST_DIALOG_FRAGMENT_HOTSPOT);
        dialogFrag.show(getFragmentManager().beginTransaction(),"bridgeDialog");
    }
    public void showEthernetDialog(){
        // Create an instance of the dialog fragment and show it
        DialogFragment dialogFrag = EthernetDialogFragment.newInstance(123);
        dialogFrag.setTargetFragment(this, Constants.REQUEST_DIALOG_FRAGMENT_HOTSPOT);
        dialogFrag.show(getFragmentManager().beginTransaction(),"ethernetDialog");
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
    public void showResetFragment(){
        DialogFragment dialogFrag = ResetFragment.newInstance(123);
        dialogFrag.setTargetFragment(this, Constants.REQUEST_DIALOG_FRAGMENT);
        dialogFrag.show(getFragmentManager().beginTransaction(), "resetDialog");
    }
    protected AlertDialog getAlertDialog(View mView) {
        return new AlertDialog.Builder(getActivity())
                .setView(mView)
                .setTitle(R.string.dialog_message)
                .setIcon(R.drawable.dialog_icon)
                .setPositiveButton(R.string.start_configuration,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                //                                getActivity().getIntent().putExtra("isValidInput", mSSIDEditText.getText().toString().length() > 0? Boolean.TRUE: Boolean.FALSE);
//                                String ip = IpAddressEditText.getText().toString();
//                                String mask = MaskEditText.getText().toString();
//                                String gateway = GateWayEditText.getText().toString();
//                                String dns = DNSEditText.getText().toString();

                                Intent intent = new Intent();
//                                intent.putExtra("HSSID", SSID);
//                                intent.putExtra("HPWD", PWD);
                                getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
                            }
                        }
                )
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_CANCELED, getActivity().getIntent());
                    }
                })
                .create();
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
