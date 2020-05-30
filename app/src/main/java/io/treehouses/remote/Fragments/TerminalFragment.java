package io.treehouses.remote.Fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.treehouses.remote.Constants;
import io.treehouses.remote.Fragments.DialogFragments.AddCommandDialogFragment;
import io.treehouses.remote.Fragments.DialogFragments.ChPasswordDialogFragment;
import io.treehouses.remote.Fragments.DialogFragments.HelpDialog;
import io.treehouses.remote.MainApplication;
import io.treehouses.remote.Network.BluetoothChatService;
import io.treehouses.remote.R;
import io.treehouses.remote.adapter.CommandListAdapter;
import io.treehouses.remote.bases.BaseTerminalFragment;
import io.treehouses.remote.databinding.ActivityTerminalFragmentBinding;
import io.treehouses.remote.pojo.CommandListItem;
import io.treehouses.remote.pojo.CommandsList;
import io.treehouses.remote.utils.SaveUtils;

public class TerminalFragment extends BaseTerminalFragment {

    private static final String TAG = "BluetoothChatFragment";
    private static final String TITLE_EXPANDABLE = "Commands";
    private static TerminalFragment instance = null;
    private ExpandableListAdapter expandableListAdapter;
    private ArrayList<String> list;
    private CommandsList commands;
    private int i;
    private String last;
    private List<String> expandableListTitle;
    private HashMap<String, List<CommandListItem>> expandableListDetail;

    /**
     * Array adapter for the conversation thread
     */
    private ArrayAdapter<String> mConversationArrayAdapter;

    /**
     * Member object for the chat services
     */
    private BluetoothChatService mChatService = null;

    private static boolean isRead = false;

    private String jsonString = "";

    private ActivityTerminalFragmentBinding bind;

    public TerminalFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        bind = ActivityTerminalFragmentBinding.inflate(inflater, container, false);
        mChatService = listener.getChatService();
        mChatService.updateHandler(mHandler);
        jsonSent = true;
        listener.sendMessage("treehouses remote commands json\n");
        instance = this;
        expandableListDetail = new HashMap<>();
        expandableListDetail.put(TITLE_EXPANDABLE, SaveUtils.getCommandsList(getContext()));
        setHasOptionsMenu(true);
        setupList();
        return bind.getRoot();
    }



    public void setupList() {
        expandableListTitle = new ArrayList<>(expandableListDetail.keySet());
        expandableListAdapter = new CommandListAdapter(getContext(), expandableListTitle, expandableListDetail);
        bind.terminalList.setAdapter(expandableListAdapter);
        bind.terminalList.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {
            if (childPosition < expandableListDetail.get("Commands").size()) {
                String title = expandableListDetail.get(expandableListTitle.get(groupPosition)).get(childPosition).getTitle();
                if (title.equalsIgnoreCase("CLEAR")) {
                    MainApplication.getTerminalList().clear();
                    getmConversationArrayAdapter().notifyDataSetChanged();
                } else if (title.equalsIgnoreCase("CHANGE PASSWORD")) {
                    showDialog(ChPasswordDialogFragment.newInstance(), Constants.REQUEST_DIALOG_FRAGMENT_CHPASS, "ChangePassDialog");
                } else {
                    listener.sendMessage(expandableListDetail.get(expandableListTitle.get(groupPosition)).get(childPosition).getCommand());
                }
            } else {
                showDialog(AddCommandDialogFragment.newInstance(), Constants.REQUEST_DIALOG_FRAGMENT_ADD_COMMAND, "AddCommandDialog");
            }
            return false;
        });
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        setUpAutoComplete(bind.editTextOut);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null && mChatService.getState() == Constants.STATE_NONE) {
            mChatService.start();
            idle(bind.pingStatus, bind.PING);
        }
    }

    @Override
    public void onResume() {
        checkStatus(mChatService, bind.pingStatus, bind.PING);
        onLoad(mHandler);
        super.onResume();
        setupChat();
    }

    public static TerminalFragment getInstance() { return instance; }

    private ArrayAdapter<String> getmConversationArrayAdapter() { return mConversationArrayAdapter; }

    /**
     * Set up the UI and background operations for chat.
     */
    public void setupChat() {
        copyToList(bind.in, getContext());

        mConversationArrayAdapter = new ArrayAdapter<String>(requireActivity(), R.layout.message, MainApplication.getTerminalList()) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                return getViews(super.getView(position, convertView, parent), isRead);
            }
        };
        bind.in.setAdapter(mConversationArrayAdapter);

        btnSendClickListener();

        // Initialize the BluetoothChatService to perform bluetooth connections
        if (mChatService.getState() == Constants.STATE_NONE) mChatService = new BluetoothChatService(mHandler, getActivity().getApplicationContext());
    }

    private void btnSendClickListener() {
        // Initialize the send button with a listener that for click events
        bind.buttonSend.setOnClickListener(v -> {
            // Send a message using content of the edit text widget
            View view = getView();
            if (null != view) {
                listener.sendMessage(bind.editTextOut.getText().toString());
                bind.editTextOut.setText("");
            }
        });
        bind.btnPrevious.setOnClickListener(v -> {
            try {
                last = list.get(--i);
                bind.editTextOut.setText(last);
                bind.editTextOut.setSelection(bind.editTextOut.length());
            } catch (Exception e) { e.printStackTrace(); } });

        bind.infoButton.setOnClickListener(v -> {
            listener.sendMessage(getString(R.string.TREEHOUSES_HELP_JSON));
            jsonSent = true;
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constants.REQUEST_ENABLE_BT:
                onResultCaseEnable(resultCode);
                break;
            case Constants.REQUEST_DIALOG_FRAGMENT_CHPASS:
                onResultCaseDialogChpass(resultCode, data);
                break;
            case Constants.REQUEST_DIALOG_FRAGMENT_ADD_COMMAND:
                if (resultCode == Activity.RESULT_OK) {
                    expandableListDetail.clear();
                    expandableListDetail.put(TITLE_EXPANDABLE, SaveUtils.getCommandsList(getContext()));
                    expandableListTitle = new ArrayList<>(expandableListDetail.keySet());
                    expandableListAdapter = new CommandListAdapter(getContext(), expandableListTitle, expandableListDetail);
                    bind.terminalList.setAdapter(expandableListAdapter);
                    bind.terminalList.expandGroup(0,true);
                }
                break;
        }
    }

    protected void onResultCaseEnable(int resultCode) {
        // When the request to enable Bluetooth returns
        if (resultCode == Activity.RESULT_OK) {
            // Bluetooth is now enabled, so set up a chat session
            setupChat();
        } else {
            // User did not enable Bluetooth or an error occurred
            Toast.makeText(getActivity(), R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }
    }


    private void showDialog(androidx.fragment.app.DialogFragment dialogFrag, int requestCode, String tag) {
        // Create an instance of the dialog fragment and show it
        dialogFrag.setTargetFragment(this, requestCode);
        dialogFrag.show(getFragmentManager().beginTransaction(), tag);
    }

    private void addToCommandList(String writeMessage) {
        MainApplication.getCommandList().add(writeMessage);
        list = MainApplication.getCommandList();
        i = list.size();
    }
    private void onResultCaseDialogChpass(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            //get password change request
            String chPWD = data.getStringExtra("password") == null ? "" : data.getStringExtra("password");
            //store password and command
            //send password to command line interface
            listener.sendMessage("treehouses password " + chPWD);
        }
    }

    private void buildJSON() {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            commands = new Gson().fromJson(jsonObject.toString(), CommandsList.class);
            if (commands != null) updateArrayAdapters(commands);
        } catch (JSONException e) { e.printStackTrace(); }
    }

    private void handleJson(String readMessage) {
        if (jsonReceiving) {
            jsonString += readMessage;
            if (jsonString.trim().endsWith("]}")) {
                buildJSON();
                jsonReceiving = false;
                jsonSent = false;
            }
            else if (jsonString.trim().endsWith("\" }")) {
                Log.e("SHOWING", "HELP with "+ jsonString);
                showHelpDialog();
                jsonReceiving = false;
                jsonSent = false;
            }
        } else if (readMessage.startsWith("{")) {
            jsonReceiving = true;
            jsonString = readMessage.trim();
        }
    }
    private void showHelpDialog() {
        Bundle b = new Bundle();
        b.putString("jsonString", jsonString);
        DialogFragment dialogFrag = new HelpDialog();
        dialogFrag.setTargetFragment(this, Constants.REQUEST_DIALOG_FRAGMENT);
        dialogFrag.setArguments(b);
        dialogFrag.show(this.requireActivity().getSupportFragmentManager().beginTransaction(), "helpDialog");
    }

    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    if (msg.arg1 == Constants.STATE_LISTEN || msg.arg1 == Constants.STATE_NONE) { idle(bind.pingStatus, bind.PING); }
                    break;
                case Constants.MESSAGE_WRITE:
                    isRead = false;
                    addToCommandList(handlerCaseWrite(TAG, mConversationArrayAdapter, msg));
                    break;
                case Constants.MESSAGE_READ:
                    String readMessage = (String) msg.obj;
                    isRead = true;
                    if (readMessage.contains("unknown")) jsonSent = false;
                    if (jsonSent) handleJson(readMessage);
                    else { handlerCaseRead(readMessage, bind.pingStatus, bind.PING);
                        filterMessages(readMessage, mConversationArrayAdapter, MainApplication.getTerminalList()); }
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    handlerCaseName(msg, getActivity());
                    break;
                case Constants.MESSAGE_TOAST:
                    handlerCaseToast(msg);
                    break;
            }
        }
    };
}
