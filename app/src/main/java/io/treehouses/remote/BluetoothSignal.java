package io.treehouses.remote;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;


public class BluetoothSignal extends BroadcastReceiver {

    private BluetoothAdapter mBluetoothAdapter = null;

    private static final String TAG = "BTSignal";


        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String mIntentAction = intent.getAction();
            final Handler handler = new Handler();
            final int delay = 3000;
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
             handler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    if(BluetoothDevice.ACTION_ACL_CONNECTED.equals(mIntentAction)) {
                        Log.i(TAG, "starting discovery");
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        Object RSSI = intent.getParcelableExtra(BluetoothDevice.EXTRA_RSSI);//intent.getByteExtra(BluetoothDevice.EXTRA_RSSI, Byte.MIN_VALUE);
                        Log.d(TAG, "Attempting to retreive RSSI...");
                        Log.i(TAG, "Attempting to retreive RSSI..." + RSSI);
                        Log.i(TAG, context.getApplicationContext().getClass().getName());
                        Toast.makeText(context.getApplicationContext(),"  RSSI: " + RSSI + "dBm", Toast.LENGTH_SHORT).show();
                        Log.i(TAG, "Attempting to retreive Device name..." + device.getName());
                        handler.postDelayed(this, delay);
                    }
                }
            }, delay);
        }
    }
