package io.treehouses.remote.Fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

import io.treehouses.remote.Constants;
import io.treehouses.remote.Fragments.DialogFragments.BottomSheetDialogs.EthernetBottomSheet;
import io.treehouses.remote.Fragments.DialogFragments.BottomSheetDialogs.TorBottomSheet;
import io.treehouses.remote.Network.BluetoothChatService;
import io.treehouses.remote.R;
import io.treehouses.remote.bases.BaseFragment;


public class TorTabFragment extends BaseFragment {
    private BluetoothChatService mChatService;
    private Button startButton, moreButton, addPortButton;
    private TextView textStatus;
    private ArrayList<String> countriesName;
    private ArrayAdapter<String> adapter;
    View view;
    private ImageView background, logo, internetstatus;
    private ClipboardManager myClipboard;
    private ClipData myClip;
    private ProgressDialog nDialog;
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        mChatService = listener.getChatService();
        mChatService.updateHandler(mHandler);

        listener.sendMessage("treehouses tor list");

        countriesName = new ArrayList<String>();

        adapter = new ArrayAdapter<String>
                (requireContext(), android.R.layout.select_dialog_item,countriesName);


        view = inflater.inflate(R.layout.activity_tor_fragment, container, false);
        ListView countryList = view.findViewById(R.id.countries);
        countryList.setAdapter(adapter);
        logo = view.findViewById(R.id.treehouse_logo);
        startButton = view.findViewById(R.id.btn_tor_start);
        addPortButton = view.findViewById(R.id.btn_add_port);
        startButton.setEnabled(false);
        startButton.setText("Getting Tor Status from raspberry pi");
        textStatus = view.findViewById(R.id.tor_status_text);
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);
        textStatus.setText("-");
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
            logo.setColorFilter(filter);
             /* start/stop tor button click */
            startButton.setOnClickListener(v -> {
                if(startButton.getText().toString() == "Stop Tor"){
                   startButton.setText("Stopping Tor");
                    startButton.setEnabled(false);
                    listener.sendMessage("treehouses tor stop");
                }

                else{

                    listener.sendMessage("treehouses tor start");
                    startButton.setEnabled(false);
                    startButton.setText("Starting tor......");

                }


            });
        final Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_tor_ports);
        TextInputEditText inputExternal = dialog.findViewById(R.id.ExternalTextInput);
        TextInputEditText inputInternal = dialog.findViewById(R.id.InternalTextInput);
        Button addingPortButton = dialog.findViewById(R.id.btn_adding_port);

            addPortButton.setOnClickListener(v -> {
                dialog.show();
                Log.d("dasd", inputInternal.getText().toString());

            });
        addingPortButton.setOnClickListener(v ->{
            if(inputExternal.getText().toString() != "" && inputInternal.getText().toString() != ""){
                String s1 = inputInternal.getText().toString();
                String s2 = inputExternal.getText().toString();
                listener.sendMessage("treehouses tor add " + s2 + " " + s1);
                dialog.dismiss();

                InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        });


        textStatus.setOnClickListener(v -> {


                myClipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                String text;
                text = textStatus.getText().toString();

                myClip = ClipData.newPlainText("text", textStatus.getText());
                myClipboard.setPrimaryClip(myClip);

                Toast.makeText(requireContext(), textStatus.getText() + " copied!",Toast.LENGTH_SHORT).show();

        });
            /* more button click */
//            moreButton.setOnClickListener(v ->{
//                showBottomSheet(new TorBottomSheet(), "ethernet");
//            });


        return view;
    }



    private void showBottomSheet(BottomSheetDialogFragment fragment, String tag) {
        fragment.setTargetFragment(TorTabFragment.this, Constants.NETWORK_BOTTOM_SHEET);
        fragment.show(getFragmentManager(), tag);
    }
    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            if (msg.what == Constants.MESSAGE_READ) {
                String readMessage = (String) msg.obj;
                Log.d("Tor reply", "" + readMessage);
                if(readMessage.contains("inactive")){


                    ColorMatrix matrix = new ColorMatrix();
                    matrix.setSaturation(0);
                    textStatus.setText("-");
                    ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
                    logo.setColorFilter(filter);
                    startButton.setText("Start Tor");
                    textStatus.setText("-");
                    startButton.setEnabled(true);
                }
                else if(readMessage.contains("the tor service has been stopped") || readMessage.contains("the tor service has been started")){

                    listener.sendMessage("treehouses tor status");
                }
                else if(readMessage.contains(".onion")){
                    textStatus.setText(readMessage);

                }
                else if(readMessage.contains("Error")){
                    textStatus.setText("Error");
                }
                else if(readMessage.contains("active")){
                    ColorMatrix matrix = new ColorMatrix();
                    ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
                    logo.setColorFilter(filter);
                    startButton.setText("Stop Tor");
                    listener.sendMessage("treehouses tor");
                    startButton.setEnabled(true);
                }
                else if(readMessage.contains("<=>")){
                    countriesName.add(readMessage);
                    adapter = new ArrayAdapter<String>
                            (requireContext(), android.R.layout.select_dialog_item,countriesName);
                    ListView countryList = view.findViewById(R.id.countries);
                    countryList.setAdapter(adapter);
                    listener.sendMessage("treehouses tor status");

                }
                else if (readMessage.contains("the port has been added")){
                    listener.sendMessage("treehouses tor list");
                    countriesName = new ArrayList<String>();
                    addPortButton.setText("Add Port");
                    Toast.makeText(requireContext(), "Port added. Retrieving ports list again",Toast.LENGTH_SHORT).show();
                }

                }



        }
    };
}


