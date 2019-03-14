package io.treehouses.remote.Fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;
import io.treehouses.remote.InitialActivity;
import io.treehouses.remote.MiscOld.Constants;
import io.treehouses.remote.Network.BluetoothChatService;
import io.treehouses.remote.R;

import static android.content.Context.CLIPBOARD_SERVICE;

public class TerminalFragment extends androidx.fragment.app.Fragment {

    View view;
    ListView listView;
    InitialActivity initialActivity;
    Context context;
    public TerminalFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_terminal_fragment, container, false);

        initialActivity = new InitialActivity();
//        RPIDialogFragment initialActivity = new RPIDialogFragment();
//        BluetoothDevice device = initialActivity.getMainDevice();
        mChatService = initialActivity.getChatService();

//        if(mChatService == null){
//            showRPIDialog();
//        }else{
        mChatService.updateHandler(mHandler);
        Log.e("TERMINAL mChatService", ""+mChatService.getState());
//        }

        listView = view.findViewById(R.id.listView);
        listView.setDivider(null);
        listView.setDividerHeight(0);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.terminal_options_list, R.id.terminalTexxtview, Constants.list);
        listView.setAdapter(adapter);

        setHasOptionsMenu(true);
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();



        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(getActivity(), "Bluetooth is not available", Toast.LENGTH_LONG).show();
            getActivity().finish();
        }

        return view;
    }

    private static final String TAG = "BluetoothChatFragment";

//    //current connection status
//    static String currentStatus = "not connected";

    // Layout Views
    private ListView mConversationView;
    private EditText mOutEditText;
    private Button mSendButton;
//    private Button treehousesButton;
//    private Button dockerButton;
//    private Button RPI_HW_Button;
    private Button pingStatusButton;
//    private Button hostnameButton;
//    private Button changePasswordButton;
//    private Button expandButton;
    private Button mCheckButton;
    private TextView mPingStatus;

    /**
     * Name of the connected device
     */
    private String mConnectedDeviceName = null;

    /**
     * Array adapter for the conversation thread
     */
    private ArrayAdapter<String> mConversationArrayAdapter;

    /**
     * String buffer for outgoing messages
     */
    private StringBuffer mOutStringBuffer;

    /**
     * Local Bluetooth adapter
     */
    private BluetoothAdapter mBluetoothAdapter = null;

    /**
     * Member object for the chat services
     */
    private BluetoothChatService mChatService = null;
    private static boolean isRead = false;
    private static boolean isCountdown = false;

    @Override
    public void onStart() {
        super.onStart();

//        if((new RPIDialogFragment()).equals(null)){
//            Log.e("TERMINAL", "NULL");
//        }
        initialActivity = new InitialActivity();
//        RPIDialogFragment initialActivity = new RPIDialogFragment();
//        BluetoothDevice device = initialActivity.getMainDevice();
        mChatService = initialActivity.getChatService();

//        if(mChatService == null){
//            showRPIDialog();
//        }else{
            mChatService.updateHandler(mHandler);
            Log.e("TERMINAL mChatService", ""+mChatService.getState());
            checkStatusNow();
//        }
//        Log.e("DEVICE ", ""+device.getName());
//         If BT is not on, request that it be enabled.
//         setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, Constants.REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        }
        else {
            setupChat();
//            mChatService.connect(device,true);
        }
    }

//    @Override
//    public void onResume(){
//        super.onResume();
//        if(getActivity().getActionBar().isShowing())
//            getActivity().getActionBar().hide();
//    }

//    @Override
//    public void onPause(){
//        super.onPause();
//        // Show Actionbar when go back to Dashboard
//        getActivity().getActionBar().show();
//    }
//    public void showRPIDialog(){
//        DialogFragment dialogFrag = RPIDialogFragment.newInstance(123);
//        dialogFrag.setTargetFragment(this, Constants.REQUEST_DIALOG_FRAGMENT_HOTSPOT);
//        dialogFrag.show(getFragmentManager().beginTransaction(),"rpiDialog");
//    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        /*
        if(isCountdown){
            mHandler.removeCallbacks(watchDogTimeOut);
        }
        */
        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == Constants.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
                mIdle();
            }
        }
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mConversationView = view.findViewById(R.id.in);
        mOutEditText = view.findViewById(R.id.edit_text_out);
        mCheckButton = view.findViewById(R.id.check);
        mSendButton = view.findViewById(R.id.button_send);
        mPingStatus = view.findViewById(R.id.pingStatus);
//        treehousesButton = (Button) view.findViewById(R.id.TB);
//        dockerButton = (Button)view.findViewById(R.id.DB);
//        RPI_HW_Button = (Button)view.findViewById(R.id.VB);
        pingStatusButton = view.findViewById(R.id.PING);
//        hostnameButton = (Button)view.findViewById(R.id.HN);
//        changePasswordButton = (Button) view.findViewById(R.id.CP);
//        expandButton = (Button)view.findViewById(R.id.EF);
    }

    /**
     * Set up the UI and background operations for chat.
     */
    private void setupChat() {
        Log.d(TAG, "setupChat()");

        // Initialize the array adapter for the conversation thread
        mConversationArrayAdapter = new ArrayAdapter<String>(getActivity(),R.layout.message){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView consoleView = view.findViewById(R.id.listItem);

                if(isRead){
                    consoleView.setTextColor(Color.BLUE);
                }else{
                    consoleView.setTextColor(Color.RED);
                }
                return view;
            }
        };



        mConversationView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String clickedData=(String) mConversationView.getItemAtPosition(position);

                context = getContext();
                Utils.copyToClipboard(context , clickedData);
            }
        });



        mConversationView.setAdapter(mConversationArrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position == 1){
                    initialActivity.sendMessage("treehouses");
                }else if(position == 3){
                    initialActivity.sendMessage("docker ps");
                }else if(position == 2){
                    initialActivity.sendMessage("treehouses detectrpi");
                }else if(position == 4){
//                    initialActivity.showDialog(view);
                }else if(position == 0){
                    showChPasswordDialog();
                }else if(position == 5){
                    initialActivity.sendMessage("treehouses expandfs");
                }
            }
        });

        // Initialize the compose field with a listener for the return key
        mOutEditText.setOnEditorActionListener(mWriteListener);

        // Initialize the send button with a listener that for click events
        mSendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                View view = getView();
                if (null != view) {
                    TextView consoleInput = view.findViewById(R.id.edit_text_out);
                    String message = consoleInput.getText().toString();
                    initialActivity.sendMessage(message);
                    consoleInput.setText("");
                }
            }
        });

        mCheckButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("CHECK STATUS",""+mChatService.getState());
                checkStatusNow();
            }
        });

//        treehousesButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                String t = "treehouses";
//                sendMessage(t);
//            }
//        });
//
//        dockerButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                String d = "docker ps";
//                sendMessage(d);
//            }
//        });
//
//        RPI_HW_Button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                String v = "treehouses detectrpi";
//                sendMessage(v);
//            }
//        });
//
//        hostnameButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                showDialog(view);
//            }
//        });
//
//        changePasswordButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                showChPasswordDialog();
//            }
//        });
//        expandButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                String e = "treehouses expandfs";
//                sendMessage(e);
//            }
//        });

        // Initialize the BluetoothChatService to perform bluetooth connections
        if(mChatService.getState() == Constants.STATE_NONE){
            mChatService = new BluetoothChatService( mHandler);
        }
        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer();
    }

    private void checkStatusNow(){
        if(mChatService.getState() == Constants.STATE_CONNECTED){
            mConnect();

        }else if(mChatService.getState() == Constants.STATE_NONE){
            mOffline();
        }else{
            mIdle();
        }
    }

    /**
     * The action listener for the EditText widget, to listen for the return key
     */
    private TextView.OnEditorActionListener mWriteListener
            = new TextView.OnEditorActionListener() {
        public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
            // If the action is a key-up event on the return key, send the message
            if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                String message = view.getText().toString();
                initialActivity.sendMessage(message);
                mOutEditText.setText("");
            }
            return true;
        }
    };


//    /**
//     * This block is to create a dialog box for creating a new name or changing the password for the PI device
//     * Sets the dialog button to be disabled if no text is in the EditText
//     */
//    private void showDialog(View view) {
//        final EditText input = new EditText(getActivity());
//        final AlertDialog alertDialog = showAlertDialog(
//                "Rename Hostname",
//                "Please enter new hostname",
//                "treehouses rename ", input);
//
//        alertDialog.getButton(alertDialog.BUTTON_POSITIVE).setClickable(false);
//        alertDialog.getButton(alertDialog.BUTTON_POSITIVE).setEnabled(false);
//
//        input.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//            }
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                if(s.length() > 0) {
//                    alertDialog.getButton(alertDialog.BUTTON_POSITIVE).setClickable(true);
//                    alertDialog.getButton(alertDialog.BUTTON_POSITIVE).setEnabled(true);
//                }else{
//                    alertDialog.getButton(alertDialog.BUTTON_POSITIVE).setClickable(false);
//                    alertDialog.getButton(alertDialog.BUTTON_POSITIVE).setEnabled(false);
//                }
//            }
//            @Override
//            public void afterTextChanged(Editable s) {
//            }
//        });
//    }
//
//    private AlertDialog showAlertDialog(String title, String message, final String command, final EditText input){
//        return new AlertDialog.Builder(getActivity())
//                .setTitle(title)
//                .setMessage(message)
//                .setIcon(android.R.drawable.ic_dialog_info)
//                .setView(input)
//                .setPositiveButton("Change", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                        String hostnameInput = input.getText().toString();
//                        String h = command + hostnameInput.toString();
//                        sendMessage(h);
//                    }
//                })
//                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.cancel();
//                    }
//                })
//                .show();
//    }
//




//    /**
//     * Makes this device discoverable for 300 seconds (5 minutes).
//     */
//    private void ensureDiscoverable() {
//        if (mBluetoothAdapter.getScanMode() !=
//                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
//            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
//            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
//            startActivity(discoverableIntent);
//        }
//    }
//
//
//
//    /**
//     * The action listener for the EditText widget, to listen for the return key
//     */
//    private TextView.OnEditorActionListener mWriteListener
//            = new TextView.OnEditorActionListener() {
//        public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
//            // If the action is a key-up event on the return key, send the message
//            if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
//                String message = view.getText().toString();
//                sendMessage(message);
//            }
//            return true;
//        }
//    };

    /**
     * Updates the status on the action bar.
     *
     * @param
     * arg: can be either a string resource ID or subTitle status
     */

//    private void setStatus(Object arg) {
//        FragmentActivity activity = getActivity();
//        if (null == activity) {
//            return;
//        }
//        final ActionBar actionBar = activity.getActionBar();
//        if (null == actionBar) {
//            return;
//        }
//        if(arg instanceof Integer){
//            Log.d(TAG, "actionBar.setSubtitle(resId) = " + arg);
//            currentStatus = getString((Integer) arg);
//            actionBar.setSubtitle((Integer) arg);
//        } else if(arg instanceof CharSequence){
//            Log.d(TAG, "actionBar.setSubtitle(subTitle) = " + arg);
//            currentStatus = arg.toString();
//            actionBar.setSubtitle((CharSequence) arg);
//        }
//    }

    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    public final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case Constants.STATE_LISTEN:
                        case Constants.STATE_NONE:
//                            setStatus(R.string.title_not_connected);
                            mIdle();
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    isRead = false;
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    if(!writeMessage.contains("google.com")) {
                        Log.d(TAG, "writeMessage = " + writeMessage);
                        mConversationArrayAdapter.add("Command:  " + writeMessage);
                    }
                    break;
                case Constants.MESSAGE_READ:
                    isRead = true;
//                    byte[] readBuf = (byte[]) msg.obj;
//                     construct a string from the valid bytes in the buffer
//                    String readMessage = new String(readBuf, 0, msg.arg1);
//                    String readMessage = new String(readBuf);
                    String readMessage = (String)msg.obj;
                    Log.d(TAG, "readMessage = " + readMessage);
                    //TODO: if message is json -> callback from RPi
                    if(isJson(readMessage)){
                        //handleCallback(readMessage);
                    }else{
                        if(isCountdown){
                            //mHandler.removeCallbacks(watchDogTimeOut);
                            isCountdown = false;
                        }
                        //remove the space at the very end of the readMessage -> eliminate space between items
                        readMessage = readMessage.substring(0,readMessage.length()-1);
                        //mConversationArrayAdapter.add(mConnectedDeviceName + ":  " + readMessage);

                        //check if ping was successful
                        if(readMessage.contains("1 packets")){
                            mConnect();
                        }
                        if(readMessage.contains("Unreachable") || readMessage.contains("failure")){
                            mOffline();
                        }
                        //make it so text doesn't show on chat (need a better way to check multiple strings since mConversationArrayAdapter only takes messages line by line)
                        if (!readMessage.contains("1 packets") && !readMessage.contains("64 bytes") && !readMessage.contains("google.com") &&
                                !readMessage.contains("rtt") && !readMessage.trim().isEmpty()){
                            mConversationArrayAdapter.add(readMessage);
                        }
                    }
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != getActivity()) {
                        Toast.makeText(getActivity(), "Connected to "
                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != getActivity()) {
                        Toast.makeText(getActivity(), msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constants.REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupChat();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(getActivity(), R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }
                break;
            case Constants.REQUEST_DIALOG_FRAGMENT_CHPASS:
                if(resultCode == Activity.RESULT_OK){

                    //get password change request
                    String chPWD = data.getStringExtra("password") == null? "":data.getStringExtra("password");

                    //store password and command
                    String password = "treehouses password " + chPWD;

                    Log.d(TAG, "back from change password");

                    //send password to command line interface
                    initialActivity.sendMessage(password);

                }else{
                    Log.d(TAG, "back from change password, fail");
                }
                break;
        }
    }

//    /**
//     * Establish connection with other device
//     *
//     * @param data   An {@link Intent} with {@link DeviceListActivity#EXTRA_DEVICE_ADDRESS} extra.
//     * @param secure Socket Security type - Secure (true) , Insecure (false)
//     */
//    private void connectDevice(Intent data, boolean secure) {
//        // Get the device MAC address
//        String address = data.getExtras()
//                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
//        // Get the BluetoothDevice object
//        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
//        // Attempt to connect to the device
//        mChatService.connect(device, secure);
//    }

    // Is this unused? Can we remove this????
//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        //inflater.inflate(R.menu.bluetooth_chat, menu);
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.insecure_connect_scan: {
//                // Launch the DeviceListActivity to see devices and do scan
//                Intent serverIntent = new Intent(getActivity(), DeviceListActivity.class);
//                startActivityForResult(serverIntent, Constants.REQUEST_CONNECT_DEVICE_INSECURE);
//                //return true;
//                break;
//            }
//            case R.id.discoverable: {
//                // Ensure this device is discoverable by others
//                ensureDiscoverable();
//                //return true;
//                break;
//            }
//            default:
//                return false;
//        }
//        return true;
//    }

    public void showChPasswordDialog() {
        // Create an instance of the dialog fragment and show it
        androidx.fragment.app.DialogFragment dialogFrag = ChPasswordDialogFragment.newInstance(123);
        dialogFrag.setTargetFragment(this, Constants.REQUEST_DIALOG_FRAGMENT_CHPASS);
        dialogFrag.show(getFragmentManager().beginTransaction(),"ChangePassDialog");
    }

    public boolean isJson(String str) {
        try {
            new JSONObject(str);
        } catch (JSONException ex) {
            return false;
        }
        return true;
    }

    /*
    public void handleCallback(String str){
        String result;
        String ip;
        if(isCountdown){
            mHandler.removeCallbacks(watchDogTimeOut);
            isCountdown = false;
        }

        //enable user interaction
        wifiPD.dismiss();
        try{
            JSONObject mJSON = new JSONObject(str);
            result = mJSON.getString("result") == null? "" : mJSON.getString("result");
            ip = mJSON.getString("IP") == null? "" : mJSON.getString("IP");
            //Toast.makeText(getActivity(), "result: "+result+", IP: "+ip, Toast.LENGTH_LONG).show();

            if(!result.equals("SUCCESS")){
                Toast.makeText(getActivity(), R.string.config_fail,
                        Toast.LENGTH_LONG).show();
            }else{
//                Toast.makeText(getActivity(), R.string.config_success,
//                            Toast.LENGTH_SHORT).show();
                Toast.makeText(getActivity(),getString(R.string.config_success) + ip,Toast.LENGTH_LONG).show();
            }

        }catch (JSONException e){
            // error handling
            Toast.makeText(getActivity(), "SOMETHING WENT WRONG", Toast.LENGTH_LONG).show();
        }
    }
    */

    public void mOffline(){
        mPingStatus.setText(R.string.bStatusOffline);
        pingStatusButton.setBackgroundResource((R.drawable.circle));
        GradientDrawable bgShape = (GradientDrawable)pingStatusButton.getBackground();
        bgShape.setColor(Color.RED);
    }

    public void mIdle(){
        mPingStatus.setText(R.string.bStatusIdle);
        pingStatusButton.setBackgroundResource((R.drawable.circle));
        GradientDrawable bgShape = (GradientDrawable)pingStatusButton.getBackground();
        bgShape.setColor(Color.YELLOW);
    }

    public void mConnect(){
        mPingStatus.setText(R.string.bStatusConnected);
        pingStatusButton.setBackgroundResource((R.drawable.circle));
        GradientDrawable bgShape = (GradientDrawable)pingStatusButton.getBackground();
        bgShape.setColor(Color.GREEN);
    }
}
