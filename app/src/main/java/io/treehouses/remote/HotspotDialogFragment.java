package io.treehouses.remote;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

/**
 * Created by Terrence on 3/12/2018.
 */

public class HotspotDialogFragment extends WifiDialogFragment {

    private static final String TAG = "HotspotDialogFragment";


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
        View mView = inflater.inflate(R.layout.dialog_design,null);
        initLayoutView(mView);

        final AlertDialog mDialog = new AlertDialog.Builder(getActivity())
                .setView(mView)
                .setTitle(R.string.dialog_message_hotspot)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(R.string.start_configuration,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
//                                getActivity().getIntent().putExtra("isValidInput", mSSIDEditText.getText().toString().length() > 0? Boolean.TRUE: Boolean.FALSE);
                                if(isValidInput){
                                    String SSID = mSSIDEditText.getText().toString();
                                    String PWD = mPWDEditText.getText().toString();

                                    Intent intent = new Intent();
                                    intent.putExtra("SSID", SSID);
                                    intent.putExtra("PWD", PWD);
                                    getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
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

        //initially disable button click
        mDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                mDialog.getButton(DialogInterface.BUTTON_POSITIVE).setClickable(false);
                mDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                mSSIDEditText.setError(getString(R.string.error_ssid_empty));
            }
        });
        setTextChangeListener(mDialog);

        return mDialog;
    }
}


