package io.treehouses.remote.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import io.treehouses.remote.R;

public class BridgeDialogFragment extends androidx.fragment.app.DialogFragment {

    private static final String TAG = "HotspotDialogFragment";

    protected EditText ESSIDEditText;
    protected EditText HotspotESSIDEditText;
    protected EditText PasswordEditText;
    protected EditText HotspotPasswordEditText;

    //    protected EditText confirmPWDEditText;
    TextBoxValidation textboxValidation = new TextBoxValidation();


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
                                //                                getActivity().getIntent().putExtra("isValidInput", mSSIDEditText.getText().toString().length() > 0? Boolean.TRUE: Boolean.FALSE);
                                String essid = ESSIDEditText.getText().toString();
                                String hotspotessid = HotspotESSIDEditText.getText().toString();
                                String password = PasswordEditText.getText().toString();
                                String hotspotpassword = HotspotPasswordEditText.getText().toString();

                                Intent intent = new Intent();
                                intent.putExtra("essid", essid);
                                intent.putExtra("hssid", hotspotessid);
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
    }

}
