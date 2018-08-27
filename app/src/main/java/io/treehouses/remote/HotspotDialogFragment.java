package io.treehouses.remote;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import java.lang.reflect.Method;


/**
 * Created by Terrence on 3/12/2018.
 */

public class HotspotDialogFragment extends DialogFragment {

    private static final String TAG = "HotspotDialogFragment";

    protected EditText hotspotSSIDEditText;
    protected EditText hotspotPWDEditText;
    protected EditText confirmPWDEditText;
    TextBoxValidation textBoxValidation = new TextBoxValidation();


    public static HotspotDialogFragment newInstance(int num) {
        HotspotDialogFragment hDialogFragment = new HotspotDialogFragment();
//        Bundle bundle = new Bundle();
//        bundle.putInt("num", num);
//        dialogFragment.setArguments(bundle);

        return hDialogFragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.d(TAG,"In onCreateDialog()");

        // Build the dialog and set up the button click handlers
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View mView = inflater.inflate(R.layout.hotspot_dialog,null);
        initLayoutView(mView);

        final AlertDialog mDialog = getAlertDialog(mView);
        mDialog.setTitle(R.string.dialog_message_hotspot);

        //initially disable button click
        textBoxValidation.getListener(mDialog);
        setTextChangeListener(mDialog);

        return mDialog;
    }

    protected AlertDialog getAlertDialog(View mView) {
        return new AlertDialog.Builder(getActivity())
                .setView(mView)
                .setTitle(R.string.dialog_message)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(R.string.start_configuration,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                //                                getActivity().getIntent().putExtra("isValidInput", mSSIDEditText.getText().toString().length() > 0? Boolean.TRUE: Boolean.FALSE);
                                String SSID = hotspotSSIDEditText.getText().toString();
                                String PWD = hotspotPWDEditText.getText().toString();

                                WifiManager wifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                                if(wifiManager.isWifiEnabled())
                                {
                                    wifiManager.setWifiEnabled(false);
                                }

                                WifiConfiguration netConfig = new WifiConfiguration();

                                netConfig.SSID = SSID;
                                netConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                                netConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                                netConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                                netConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                                netConfig.preSharedKey = PWD;

                                try{
                                    Method setWifiApMethod = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
                                    boolean apstatus=(Boolean) setWifiApMethod.invoke(wifiManager, netConfig,true);

                                    Method isWifiApEnabledmethod = wifiManager.getClass().getMethod("isWifiApEnabled");
                                    while(!(Boolean)isWifiApEnabledmethod.invoke(wifiManager)){};
                                    Method getWifiApStateMethod = wifiManager.getClass().getMethod("getWifiApState");
                                    int apstate=(Integer)getWifiApStateMethod.invoke(wifiManager);
                                    Method getWifiApConfigurationMethod = wifiManager.getClass().getMethod("getWifiApConfiguration");
                                    netConfig=(WifiConfiguration)getWifiApConfigurationMethod.invoke(wifiManager);
                                    Log.e("CLIENT", "\nSSID:"+netConfig.SSID+"\nPassword:"+netConfig.preSharedKey+"\n");

                                    Intent intent = new Intent();
                                    intent.putExtra("HSSID", SSID);
                                    intent.putExtra("HPWD", PWD);
                                    getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);

                                } catch (Exception e) {
                                    Log.e(this.getClass().toString(), "", e);
                                }
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

    public void setTextChangeListener(final AlertDialog mDialog) {
        textBoxValidation.mDialog = mDialog;
        textBoxValidation.textWatcher = hotspotSSIDEditText;
        textBoxValidation.SSID = hotspotSSIDEditText;
        textBoxValidation.PWD = hotspotPWDEditText;
        textBoxValidation.hotspotTextboxValidation(confirmPWDEditText, getContext());

        textBoxValidation.mDialog = mDialog;
        textBoxValidation.textWatcher = hotspotPWDEditText;
        textBoxValidation.SSID = hotspotSSIDEditText;
        textBoxValidation.PWD = hotspotPWDEditText;
        textBoxValidation.hotspotTextboxValidation(confirmPWDEditText, getContext());

        textBoxValidation.mDialog = mDialog;
        textBoxValidation.textWatcher = confirmPWDEditText;
        textBoxValidation.SSID = hotspotSSIDEditText;
        textBoxValidation.PWD = hotspotPWDEditText;
        textBoxValidation.hotspotTextboxValidation(confirmPWDEditText, getContext());
    }

    protected void initLayoutView(View mView) {
        hotspotSSIDEditText = (EditText)mView.findViewById(R.id.hotspotSSID);
        hotspotPWDEditText = (EditText)mView.findViewById(R.id.hotspotPassword);
        confirmPWDEditText = (EditText)mView.findViewById(R.id.confirmPassword);
    }

}


