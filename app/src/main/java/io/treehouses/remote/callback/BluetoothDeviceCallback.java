package io.treehouses.remote.callback;

import android.bluetooth.BluetoothDevice;

public interface BluetoothDeviceCallback {
    void onDeviceFound(BluetoothDevice bluetoothDevice);
}
