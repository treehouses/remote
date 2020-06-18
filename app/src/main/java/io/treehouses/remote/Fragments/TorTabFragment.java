package io.treehouses.remote.Fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

import io.treehouses.remote.Constants;
import io.treehouses.remote.Network.BluetoothChatService;
import io.treehouses.remote.R;
import io.treehouses.remote.bases.BaseFragment;


public class TorTabFragment extends BaseFragment {
    private BluetoothChatService mChatService;
    private Button startButton, moreButton, addPortButton;
    private TextView textStatus;
    private ArrayList<String> portsName;
    private ArrayAdapter<String> adapter;
    View view;
    private ImageView background, logo, internetstatus;
    private ClipboardManager myClipboard;
    private ClipData myClip;
    private ProgressDialog nDialog;
    private ListView portList;
    private Switch notification;
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        mChatService = listener.getChatService();
        mChatService.updateHandler(mHandler);

        listener.sendMessage("treehouses tor ports");

        portsName = new ArrayList<String>();

        adapter = new ArrayAdapter<String>
                (requireContext(), android.R.layout.select_dialog_item,portsName);


        view = inflater.inflate(R.layout.activity_tor_fragment, container, false);
        notification = (Switch) view.findViewById(R.id.switchNotification);
        notification.setEnabled(false);
        notification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    notification.setEnabled(false);
                    listener.sendMessage("treehouses tor notice on");
                }
                else{
                    notification.setEnabled(false);
                    listener.sendMessage("treehouses tor notice off");
                }
            }
        });
        portList = view.findViewById(R.id.countries);
        portList.setAdapter(adapter);
        portList.setOnItemClickListener((parent, view, position, id) -> {

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Delete Port " + portsName.get(position) + " ?");

//            builder.setMessage("Would you like to delete?");

            // add the buttons

            builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    listener.sendMessage("treehouses tor delete " +portsName.get(position).split(":", 2)[0]);
                    addPortButton.setText("deleting port .....");
                    portList.setEnabled(false);
                    addPortButton.setEnabled(false);
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton("Cancel", null);

            // create and show the alert dialog
            AlertDialog dialog = builder.create();
            dialog.show();
        });
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
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        Window window = dialog.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        TextInputEditText inputExternal = dialog.findViewById(R.id.ExternalTextInput);
        TextInputEditText inputInternal = dialog.findViewById(R.id.InternalTextInput);
        Button addingPortButton = dialog.findViewById(R.id.btn_adding_port);

            addPortButton.setOnClickListener(v -> {
                dialog.show();


            });
        addingPortButton.setOnClickListener(v ->{
            if(inputExternal.getText().toString() != "" && inputInternal.getText().toString() != ""){
                String s1 = inputInternal.getText().toString();
                String s2 = inputExternal.getText().toString();
                listener.sendMessage("treehouses tor add " + s2 + " " + s1);
                addPortButton.setText("Adding port, please wait for a while ............");
                portList.setEnabled(false);
                addPortButton.setEnabled(false);
                dialog.dismiss();

                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
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
                    listener.sendMessage("treehouses tor notice");
                }
                else if(readMessage.contains("the tor service has been stopped") || readMessage.contains("the tor service has been started")){

                    listener.sendMessage("treehouses tor status");
                }
                else if(readMessage.contains(".onion")){
                    textStatus.setText(readMessage);
                    listener.sendMessage("treehouses tor notice");

                }
                else if(readMessage.contains("Error")){
                    Toast.makeText(requireContext(), "Error",Toast.LENGTH_SHORT).show();
                    addPortButton.setText("add ports");
                    addPortButton.setEnabled(true);
                    portList.setEnabled(true);


                }
                else if(readMessage.contains("active")){
                    ColorMatrix matrix = new ColorMatrix();
                    ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
                    logo.setColorFilter(filter);
                    startButton.setText("Stop Tor");
                    listener.sendMessage("treehouses tor");
                    startButton.setEnabled(true);

                }
                else if (readMessage.contains("OK.")){
                    listener.sendMessage("treehouses tor notice");
                }
                else if (readMessage.contains("Status: on")){
                    notification.setChecked(true);
                    notification.setEnabled(true);
                }
                else if (readMessage.contains("Status: off")){
                    notification.setChecked(false);
                    notification.setEnabled(true);
                } //regex to match ports text
                else if(readMessage.matches("(([0-9]+:[0-9]+)\\s?)+")){
                    addPortButton.setText("Add Port");
                    portList.setEnabled(true);
                    addPortButton.setEnabled(true);
                    String[] ports = readMessage.split(" ");
                    for (int i = 0; i < ports.length; i++) {
                        portsName.add(ports[i]);
                    }
                    adapter = new ArrayAdapter<String>
                            (requireContext(), android.R.layout.select_dialog_item,portsName);
                    ListView portList = view.findViewById(R.id.countries);
                    portList.setAdapter(adapter);
                    listener.sendMessage("treehouses tor status");

                }
                else if (readMessage.contains("the port has been added")){
                    listener.sendMessage("treehouses tor ports");
                    portsName = new ArrayList<String>();
                    addPortButton.setText("Retrieving port.... Please wait");
                    Toast.makeText(requireContext(), "Port added. Retrieving ports list again",Toast.LENGTH_SHORT).show();
                }
                else if (readMessage.contains("has been deleted")){
                    listener.sendMessage("treehouses tor ports");
                    portsName = new ArrayList<String>();
                    addPortButton.setText("Retrieving port..... Please wait");
                    Toast.makeText(requireContext(), "Port deleted. Retrieving ports list again",Toast.LENGTH_SHORT).show();
                }


                }



        }
    };
}


