package io.treehouses.remote.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ToggleButton;

import androidx.core.content.ContextCompat;
import io.treehouses.remote.R;

public class BridgeDialogFragment extends androidx.fragment.app.DialogFragment {

    private static final String TAG = "HotspotDialogFragment";

    protected EditText ESSIDEditText;
    protected EditText HotspotESSIDEditText;
    protected EditText PasswordEditText;
    protected EditText HotspotPasswordEditText;

    //    protected EditText confirmPWDEditText;
    TextBoxValidation textboxValidation = new TextBoxValidation();

    ToggleButton hotspot_password,essid_password;


    public static BridgeDialogFragment newInstance(int num) {
        BridgeDialogFragment bridgeDialogFragment = new BridgeDialogFragment();
//        Bundle bundle = new Bundle();
//        bundle.putInt("num", num);
//        dialogFragment.setArguments(bundle);

        return bridgeDialogFragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.d(TAG,"In onCreateDialog()");

        // Build the dialog and set up the button click handlers
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View mView = inflater.inflate(R.layout.dialog_bridge,null);
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
                                String essid = ESSIDEditText.getText().toString();
                                String hotspotessid = HotspotESSIDEditText.getText().toString();
                                String password = PasswordEditText.getText().toString();
                                String hotspotpassword = HotspotPasswordEditText.getText().toString();

                                Intent intent = new Intent();
                                intent.putExtra("essid", essid);
                                intent.putExtra("hssid", hotspotessid);
                                intent.putExtra("type","bridge");
                                intent.putExtra("password",password);
                                intent.putExtra("hpassword",hotspotpassword);
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
        addingTextChange(mDialog, ESSIDEditText);
        addingTextChange(mDialog, HotspotESSIDEditText);
        addingTextChange(mDialog, PasswordEditText);
        addingTextChange(mDialog, HotspotPasswordEditText);
    }

    public void addingTextChange(final AlertDialog mDialog, EditText editText){
        textboxValidation.mDialog = mDialog;
        textboxValidation.textWatcher = editText;
        textboxValidation.ESSIDEditText = ESSIDEditText;
        textboxValidation.HotspotESSIDEditText = HotspotESSIDEditText;
        textboxValidation.PasswordEditText = PasswordEditText;
        textboxValidation.HotspotPasswordEditText = HotspotPasswordEditText;
        textboxValidation.textboxValidation(getContext(), "bridge");
    }

    protected void initLayoutView(View mView) {
        ESSIDEditText = mView.findViewById(R.id.ESSID);
        HotspotESSIDEditText = mView.findViewById(R.id.hotspotESSID);
        PasswordEditText = mView.findViewById(R.id.password);
        HotspotPasswordEditText = mView.findViewById(R.id.hotspotPassword);

        hotspot_password = mView.findViewById(R.id.show_hotspot);
        essid_password = mView.findViewById(R.id.show_essid_password);

        hotspot_password.setText(null);
        hotspot_password.setTextOn(null);
        hotspot_password.setTextOff(null);

        essid_password.setText(null);
        essid_password.setTextOn(null);
        essid_password.setTextOff(null);

        hotspot_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(hotspot_password.isChecked()){
                    HotspotPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
                    HotspotPasswordEditText.setSelection(HotspotPasswordEditText.getText().length());
                    hotspot_password.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.showing));
                }
                else{
                    HotspotPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    HotspotPasswordEditText.setSelection(HotspotPasswordEditText.getText().length());
                    hotspot_password.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.show_password));
                }
            }
        });

        essid_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(essid_password.isChecked()){
                    PasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
                    PasswordEditText.setSelection(PasswordEditText.getText().length());
                    essid_password.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.showing));
                }
                else{
                    PasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    PasswordEditText.setSelection(PasswordEditText.getText().length());
                    essid_password.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.show_password));
                }
            }
        });
    }

}
