package io.treehouses.remote.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.renderscript.ScriptGroup;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ToggleButton;

import java.util.ArrayList;

import androidx.core.content.ContextCompat;
import io.treehouses.remote.R;


/**
 * Created by Terrence on 3/12/2018.
 */

public class HotspotDialogFragment extends androidx.fragment.app.DialogFragment {

    private static final String TAG = "HotspotDialogFragment";

    protected EditText hotspotSSIDEditText;
    protected EditText hotspotPWDEditText;
//    protected EditText confirmPWDEditText;
    TextBoxValidation textboxValidation = new TextBoxValidation();

    ToggleButton show;

    Spinner spinner;
    public static HotspotDialogFragment newInstance(int num) {
        HotspotDialogFragment hDialogFragment = new HotspotDialogFragment();
//        Bundle bundle = new Bundle();
//        bundle.putInt("num", num);
//        dialogFragment.setArguments(bundle);

        return hDialogFragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.d(TAG,"In onCreateDialog()");

        // Build the dialog and set up the button click handlers
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View mView = inflater.inflate(R.layout.dialog_hotspot,null);
        initLayoutView(mView);

        ArrayList<String> list = new ArrayList<String>();
        list.add("internet");
        list.add("local");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        final AlertDialog mDialog = getAlertDialog(mView);
        mDialog.setTitle(R.string.dialog_message_hotspot);

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
                                String SSID = hotspotSSIDEditText.getText().toString();
                                String hotspotType = spinner.getSelectedItem().toString();
                                if(hotspotPWDEditText.getText().toString() == null){
                                    Intent intent = new Intent();
                                    intent.putExtra("HSSID", SSID);
                                    intent.putExtra("HPWD", "");
                                    intent.putExtra("hotspotType",hotspotType);
                                    intent.putExtra("type", "hotspot");
                                    getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
                                }
                                String PWD = hotspotPWDEditText.getText().toString();
                                Intent intent = new Intent();
                                intent.putExtra("HSSID", SSID);
                                intent.putExtra("HPWD", PWD);
                                intent.putExtra("hotspotType",hotspotType);
                                intent.putExtra("type", "hotspot");
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

        textboxValidation.mDialog = mDialog;
        textboxValidation.textWatcher = hotspotSSIDEditText;
        textboxValidation.SSID = hotspotSSIDEditText;
        textboxValidation.PWD = hotspotPWDEditText;
        textboxValidation.textboxValidation(getActivity(), "wifi");

        textboxValidation.mDialog = mDialog;
        textboxValidation.textWatcher = hotspotPWDEditText;
        textboxValidation.SSID = hotspotSSIDEditText;
        textboxValidation.PWD = hotspotPWDEditText;
        textboxValidation.textboxValidation(getActivity(), "wifi");

    }

    protected void initLayoutView(View mView) {
        spinner = mView.findViewById(R.id.hostspotType);
        hotspotSSIDEditText = mView.findViewById(R.id.hotspotSSID);
        hotspotPWDEditText = mView.findViewById(R.id.hotspotPassword);
    }
}


