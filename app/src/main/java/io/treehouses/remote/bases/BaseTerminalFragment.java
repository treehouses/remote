package io.treehouses.remote.bases;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.preference.PreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import io.treehouses.remote.Constants;
import io.treehouses.remote.Network.BluetoothChatService;
import io.treehouses.remote.R;
import io.treehouses.remote.pojo.CommandsList;
import io.treehouses.remote.utils.Utils;

public class BaseTerminalFragment extends BaseFragment{
    private final String[] array2 = {"treehouses", "docker"};
    private Set<String> inSecondLevel, inThirdLevel;
    private ArrayAdapter<String> arrayAdapter1, arrayAdapter2, arrayAdapter3;

    public String handlerCaseWrite(String TAG, ArrayAdapter<String> mConversationArrayAdapter, Message msg) {

        byte[] writeBuf = (byte[]) msg.obj;
        // construct a string from the buffer
        String writeMessage = new String(writeBuf);
        if (!writeMessage.contains("google.com") && !writeMessage.contains("remote")) {
            Log.d(TAG, "writeMessage = " + writeMessage);
            mConversationArrayAdapter.add("\nCommand:  " + writeMessage);
        }
        return writeMessage;
    }

    public void handlerCaseName(Message msg, Activity activity ) {
        // save the connected device's name
        String mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
        if (null != activity) {
            Toast.makeText(activity, "Connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
        }
    }

    public void handlerCaseToast(Message msg) {
        if (null != getActivity()) {
            Toast.makeText(getActivity(), msg.getData().getString(Constants.TOAST), Toast.LENGTH_SHORT).show();
        }
    }

    public View getViews(View view, Boolean isRead) {
        TextView consoleView = view.findViewById(R.id.listItem);
        if (isRead) {
            consoleView.setTextColor(Color.BLUE);
        } else {
            consoleView.setTextColor(Color.RED);
        }
        return view;
    }

    private void bgResource(Button pingStatusButton, int color) {
        pingStatusButton.setBackgroundResource((R.drawable.circle));
        GradientDrawable bgShape = (GradientDrawable) pingStatusButton.getBackground();
        bgShape.setColor(color);
    }

    private void offline(TextView mPingStatus, Button pingStatusButton) {
        mPingStatus.setText(R.string.bStatusOffline);
        bgResource(pingStatusButton, Color.RED);
    }

    protected void idle(TextView mPingStatus, Button pingStatusButton) {
        mPingStatus.setText(R.string.bStatusIdle);
        bgResource(pingStatusButton, Color.YELLOW);
    }

    private void connect(TextView mPingStatus, Button pingStatusButton) {
        mPingStatus.setText(R.string.bStatusConnected);
        bgResource(pingStatusButton, Color.GREEN);
    }



    protected void copyToList(final ListView mConversationView, final Context context) {
        mConversationView.setOnItemClickListener((parent, view, position, id) -> {
            String clickedData = (String) mConversationView.getItemAtPosition(position);
            Utils.copyToClipboard(context, clickedData);
        });
    }

    protected void checkStatus(BluetoothChatService mChatService, TextView mPingStatus, Button pingStatusButton) {
        if (mChatService.getState() == Constants.STATE_CONNECTED) {
            connect(mPingStatus, pingStatusButton);
        } else if (mChatService.getState() == Constants.STATE_NONE) {
            offline(mPingStatus, pingStatusButton);
        } else {
            idle(mPingStatus, pingStatusButton);
        }
    }
    private boolean filterMessage(String readMessage) {
        boolean a = !readMessage.contains("1 packets") && !readMessage.contains("64 bytes") && !readMessage.contains("google.com") && !readMessage.contains("rtt") && !readMessage.trim().isEmpty();
        boolean b = !readMessage.startsWith("treehouses ") && !readMessage.contains("treehouses remote commands");
        return a && b;
    }

    protected void filterMessages(String readMessage, ArrayAdapter mConversationArrayAdapter, ArrayList list) {
        //make it so text doesn't show on chat (need a better way to check multiple strings since mConversationArrayAdapter only takes messages line by line)
        if (filterMessage(readMessage)) {
            list.add(readMessage);
            mConversationArrayAdapter.notifyDataSetChanged();
        }
    }

    private void isPingSuccesfull(String readMessage, TextView mPingStatus, Button pingStatusButton) {
        readMessage = readMessage.trim();

        //check if ping was successful
        if (readMessage.contains("1 packets")) {
            connect(mPingStatus, pingStatusButton);
        }
        if (readMessage.contains("Unreachable") || readMessage.contains("failure")) {
            offline(mPingStatus, pingStatusButton);
        }
    }

    protected void handlerCaseRead(String readMessage, TextView mPingStatus, Button pingStatusButton) {

        Log.d("TAG", "readMessage = " + readMessage);

        //TODO: if message is json -> callback from RPi
        if (!isJson(readMessage)) {
            isPingSuccesfull(readMessage, mPingStatus, pingStatusButton);
        }
    }

    private boolean isJson(String readMessage) {
        try {
            new JSONObject(readMessage);
        } catch (JSONException ex) {
            return false;
        }
        return true;
    }
    private int countSpaces(String s) {
        int count = 0;
        for (int i = 0; i < s.length(); i++) { if (s.charAt(i) == ' ') count++; }
        return count;
    }

    protected void setUpAutoComplete(AutoCompleteTextView autoComplete) {
        inSecondLevel = new HashSet<>();
        inThirdLevel = new HashSet<>();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(Objects.requireNonNull(getContext()));
        arrayAdapter1 = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, array2);
        arrayAdapter2 = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
        arrayAdapter3 = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
        if (preferences.getBoolean("autocomplete", true)) {
            autoComplete.setThreshold(0);
            autoComplete.setAdapter(arrayAdapter1);
            addTextChangeListener(autoComplete);
            addSpaces(autoComplete);
        }
    }

    private void addTextChangeListener( AutoCompleteTextView autoComplete) {
        autoComplete.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (countSpaces(s.toString()) == 0) {
                    autoComplete.setAdapter(arrayAdapter1);
                }
                else if (countSpaces(s.toString()) == 1) {
                    autoComplete.setAdapter(arrayAdapter2);
                }
                else if (countSpaces(s.toString()) == 2){
                    autoComplete.setAdapter(arrayAdapter3);
                }

            }
            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().endsWith("\n")) {
                    listener.sendMessage(autoComplete.getText().toString().substring(0,autoComplete.getText().toString().length()-1));
                    autoComplete.setText("");
                }
            }});
    }

    private String getRootCommand (String s) {
        StringBuilder stringBuilder = new StringBuilder();
        int count = 0;
        for (int i = 0 ; i < s.length(); i++) {
            if (s.charAt(i) == ' ') count++;
            if (count >= 2) break;
            stringBuilder.append(s.charAt(i));
        }
        return stringBuilder.toString();
    }
    protected void updateArrayAdapters(CommandsList data) {
        if (data.commands == null) {
            Toast.makeText(requireContext(), "Error has occurred. Please Refresh", Toast.LENGTH_SHORT).show();
            return;
        }
        for (int i = 0;i < data.commands.size();i++) {
            String s = getRootCommand(data.commands.get(i)).trim();
            Log.d("TAG", "updateArrayAdapters: "+s);
            if (!inSecondLevel.contains(s)) {
                arrayAdapter2.add(s);
                inSecondLevel.add(s);
            }
            if (!inThirdLevel.contains(data.commands.get(i))) {
                arrayAdapter3.add(data.commands.get(i));
                inThirdLevel.add(data.commands.get(i));
            }
        }
    }

    private void addSpaces(AutoCompleteTextView autoComplete) {
        autoComplete.setOnItemClickListener((parent, view, position, id) -> {
            autoComplete.postDelayed(autoComplete::showDropDown,100);
            autoComplete.append(" ");
        });
    }
}
