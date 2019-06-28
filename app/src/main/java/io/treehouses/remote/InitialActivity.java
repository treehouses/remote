package io.treehouses.remote;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.android.material.navigation.NavigationView;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;
import io.treehouses.remote.Fragments.AboutFragment;
import io.treehouses.remote.Fragments.HomeFragment;
import io.treehouses.remote.Fragments.NetworkFragment;
import io.treehouses.remote.Fragments.ServicesFragment;
import io.treehouses.remote.Fragments.StatusFragment;
import io.treehouses.remote.Fragments.SystemFragment;
import io.treehouses.remote.Fragments.TerminalFragment;
import io.treehouses.remote.Fragments.TunnelFragment;
import io.treehouses.remote.Network.BluetoothChatService;
import io.treehouses.remote.bases.PermissionActivity;
import io.treehouses.remote.callback.HomeInteractListener;
import io.treehouses.remote.utils.LogUtils;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class InitialActivity extends PermissionActivity
        implements NavigationView.OnNavigationItemSelectedListener, HomeInteractListener {

    private static InitialActivity instance = null;
    private Boolean validBluetoothConnection = false;
    int REQUEST_COARSE_LOCATION = 99;
    private static BluetoothChatService mChatService = null;
    private String mConnectedDeviceName = null;
    DrawerLayout drawer;
    private String TAG = "InitialActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        setContentView(R.layout.activity_initial2);
        requestPermission();
        Log.e(TAG, "onCreate(Bundle) called");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer_layout);

        checkLocationPermission();

        if (mChatService == (null)) {
            Log.e(TAG, "mChatService Status: NULL");
            mChatService = new BluetoothChatService(mHandler);
        } else {
            Log.e(TAG, "mChatService Status: " + mChatService.getState());
            mChatService.updateHandler(mHandler);
        }

        checkStatusNow();

//        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
//        getSupportActionBar().setTitle(R.string.app_project_name);
        openCallFragment(new HomeFragment());
//
//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
////                        .setAction("Action", null).show();
//            }
//        });


        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
//        navigationView.addHeaderView(getResources().getLayout(R.layout.navigation_view_header));
    }

    public static InitialActivity getInstance() {
        return instance;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.initial, menu);
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        checkStatusNow();
        if (validBluetoothConnection) {
            onNavigationItemClicked(id);
        } else {
            if (id == R.id.menu_about) {
                openCallFragment((new AboutFragment()));
            } else if (id == R.id.menu_home) {
                openCallFragment(new HomeFragment());
            } else {
                showAlertDialog();
            }
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void onNavigationItemClicked(int id) {
        if (id == R.id.menu_home) {
            openCallFragment(new HomeFragment());
        } else if (id == R.id.menu_network) {
            openCallFragment((new NetworkFragment()));
        } else if (id == R.id.menu_system) {
            openCallFragment(new SystemFragment());
        } else if (id == R.id.menu_terminal) {
            openCallFragment(new TerminalFragment());
        } else {
            checkMore(id);
        }
    }

    private void checkMore(int id) {
        if (id == R.id.menu_services) {
            openCallFragment(new ServicesFragment());
        } else if (id == R.id.menu_tunnel) {
            openCallFragment(new TunnelFragment());
        } else if (id == R.id.menu_about) {
            openCallFragment((new AboutFragment()));
        } else if (id == R.id.menu_status) {
            openCallFragment(new StatusFragment());
        } else {
            openCallFragment(new HomeFragment());
        }
    }

    @Override
    public void openCallFragment(androidx.fragment.app.Fragment newfragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, newfragment);
        fragmentTransaction.addToBackStack("");
        fragmentTransaction.commit();
//        menuItem.setChecked(true);
        setTitle("Treehouses Remote");
//        drawer.closeDrawers();

    }
//
//    @Override
//    public void updateHandler(Handler handler) {
//        if (mChatService != null)
//            mChatService.updateHandler(mHandler);
//    }

    protected void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_COARSE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 99: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    //TODO re-request
                }
                break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mChatService.updateHandler(mHandler);
    }


    @Override
    public void setChatService(BluetoothChatService chatService) {
        mChatService = chatService;
        mChatService.updateHandler(mHandler);
        checkStatusNow();
    }

    @Override
    public BluetoothChatService getChatService() {
        return mChatService;
    }

    private void checkStatusNow() {
        if (mChatService.getState() == Constants.STATE_CONNECTED) {
            LogUtils.mConnect();
            validBluetoothConnection = true;
        } else if (mChatService.getState() == Constants.STATE_NONE) {
            LogUtils.mOffline();
            validBluetoothConnection = false;
        } else {
            LogUtils.mIdle();
            validBluetoothConnection = false;
        }
        Log.e("BOOLEAN", "" + validBluetoothConnection);

        //start pinging for wifi check
//        final Handler h = new Handler();
//        final int delay = 20000;
//        h.postDelayed(new Runnable(){
//            public void run(){
//                String ping = "ping -c 1 google.com";
//                sendPing(ping);
//                //remove the space at the very end of the readMessage -> eliminate space between items
//                String readMessage = new String(mOutStringBuffer);
//                readMessage = readMessage.substring(0,readMessage.length()-1);
//                //check if ping was successful
//                if(readMessage.contains("1 packets")){validWifiConnection = true;}
//                if(readMessage.contains("Unreachable") || readMessage.contains("failure")){validWifiConnection = false;}
//                h.postDelayed(this, delay);
//            }
//        }, delay);

    }
//    /**
//     * This block is to create a dialog box for creating a new name or changing the password for the PI device
//     * Sets the dialog button to be disabled if no text is in the EditText
//     */
//    private void showDialog(View view) {
//        final EditText input = new EditText(InitialActivity.this);
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

    private AlertDialog showAlertDialog() {
        return new AlertDialog.Builder(InitialActivity.this)
                .setTitle("ALERT:")
                .setMessage("Connect to raspberry pi via bluetooth in the HOME PAGE first before accessing this feature")
                .setIcon(R.drawable.bluetooth)
                .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .show();
    }

    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */
    @Override
    public void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        LogUtils.log(message);
        if (mChatService.getState() != Constants.STATE_CONNECTED) {
            Toast.makeText(InitialActivity.this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            LogUtils.mIdle();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);

            // Reset out string buffer to zero and clear the edit text field
//            mOutStringBuffer.setLength(0);
        }
    }

    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    public final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
//            FragmentActivity activity = getActivity();
            //InitialActivity activity = InitialActivity.this;
            switch (msg.what) {
//                case Constants.MESSAGE_STATE_CHANGE:
//                    switch (msg.arg1) {
//                        case Constants.STATE_LISTEN:
//                        case Constants.STATE_NONE:
////                            setStatus(R.string.title_not_connected);
//                            mIdle();
//                            break;
//                    }
//                    break;
//                case Constants.MESSAGE_WRITE:
//                    isRead = false;
//                    byte[] writeBuf = (byte[]) msg.obj;
//                    // construct a string from the buffer
//                    String writeMessage = new String(writeBuf);
//                    if(!writeMessage.contains("google.com")) {
//                        Log.d(TAG, "writeMessage = " + writeMessage);
//                        mConversationArrayAdapter.add("Command:  " + writeMessage);
//                    }
//                    break;
//                case Constants.MESSAGE_READ:
//                    isRead = true;
////                    byte[] readBuf = (byte[]) msg.obj;
////                     construct a string from the valid bytes in the buffer
////                    String readMessage = new String(readBuf, 0, msg.arg1);
////                    String readMessage = new String(readBuf);
//                    String readMessage = (String)msg.obj;
//                    Log.d(TAG, "readMessage = " + readMessage);
//                    //TODO: if message is json -> callback from RPi
//                    if(isJson(readMessage)){
//                        //handleCallback(readMessage);
//                    }else{
//                        if(isCountdown){
//                            //mHandler.removeCallbacks(watchDogTimeOut);
//                            isCountdown = false;
//                        }
//                        //remove the space at the very end of the readMessage -> eliminate space between items
//                        readMessage = readMessage.substring(0,readMessage.length()-1);
//                        //mConversationArrayAdapter.add(mConnectedDeviceName + ":  " + readMessage);
//
//                        //check if ping was successful
//                        if(readMessage.contains("1 packets")){
//                            mConnect();
//                        }
//                        if(readMessage.contains("Unreachable") || readMessage.contains("failure")){
//                            mOffline();
//                        }
//                        //make it so text doesn't show on chat (need a better way to check multiple strings since mConversationArrayAdapter only takes messages line by line)
//                        if (!readMessage.contains("1 packets") && !readMessage.contains("64 bytes") && !readMessage.contains("google.com") &&
//                                !readMessage.contains("rtt") && !readMessage.trim().isEmpty()){
//                            mConversationArrayAdapter.add(readMessage);
//                        }
//                    }
//                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (mConnectedDeviceName != "" || mConnectedDeviceName != null) {
                        Log.e("DEVICE", "" + mConnectedDeviceName);
                        checkStatusNow();
//                        Toast.makeText(InitialActivity.this, "Connected to "+mConnectedDeviceName, Toast.LENGTH_LONG).show();
                    }
//                    if (null != activity) {
//                        Toast.makeText(activity, "Connected to "
//                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
//                    }
                    break;
//                case Constants.MESSAGE_TOAST:
//                    if (null != activity) {
//                        Toast.makeText(activity, msg.getData().getString(Constants.TOAST),
//                                Toast.LENGTH_SHORT).show();
//                    }
//                    break;
            }
        }
    };


//    public boolean isJson(String str) {
//        try {
//            new JSONObject(str);
//        } catch (JSONException ex) {
//            return false;
//        }
//        return true;
//    }


}
