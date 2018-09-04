package io.treehouses.remote.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import io.treehouses.remote.R;

public class EthernetDialogFragment extends DialogFragment{

    private static final String TAG = "HotspotDialogFragment";

    protected EditText IpAddressEditText;
    protected EditText MaskEditText;
    protected EditText GateWayEditText;
    protected EditText DNSEditText;

    //    protected EditText confirmPWDEditText;
    TextBoxValidation textboxValidation = new TextBoxValidation();


    public static EthernetDialogFragment newInstance(int num) {
        EthernetDialogFragment ethernetDialogFragment = new EthernetDialogFragment();
//        Bundle bundle = new Bundle();
//        bundle.putInt("num", num);
//        dialogFragment.setArguments(bundle);

        return ethernetDialogFragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.d(TAG,"In onCreateDialog()");

        // Build the dialog and set up the button click handlers
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View mView = inflater.inflate(R.layout.dialog_ethernet,null);
        initLayoutView(mView);

        final AlertDialog mDialog = getAlertDialog(mView);

        //initially disable button click
        textboxValidation.getListener(mDialog);
        setTextChangeListener(mDialog);

        return mDialog;
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
                                String ip = IpAddressEditText.getText().toString();
                                String mask = MaskEditText.getText().toString();
                                String gateway = GateWayEditText.getText().toString();
                                String dns = DNSEditText.getText().toString();

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

    public void setTextChangeListener(final AlertDialog mDialog) {
        addingTextChange(mDialog, IpAddressEditText);
        addingTextChange(mDialog, MaskEditText);
        addingTextChange(mDialog, GateWayEditText);
        addingTextChange(mDialog, DNSEditText);
    }

    public void addingTextChange(final AlertDialog mDialog, EditText editText){
        textboxValidation.mDialog = mDialog;
        textboxValidation.textWatcher = editText;
        textboxValidation.IpAddressEditText = IpAddressEditText;
        textboxValidation.MaskEditText = MaskEditText;
        textboxValidation.GateWayEditText = GateWayEditText;
        textboxValidation.DNSEditText = DNSEditText;
        textboxValidation.textboxValidation(getContext(), "ethernet");
    }

    protected void initLayoutView(View mView) {
        IpAddressEditText = (EditText)mView.findViewById(R.id.ip);
        MaskEditText = (EditText)mView.findViewById(R.id.mask);
        GateWayEditText = (EditText)mView.findViewById(R.id.gateway);
        DNSEditText = (EditText)mView.findViewById(R.id.dns);
    }

}
