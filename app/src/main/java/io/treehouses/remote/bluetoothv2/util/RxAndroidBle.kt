package io.treehouses.remote.bluetoothv2.util

import android.bluetooth.BluetoothGattCharacteristic
import com.polidea.rxandroidble2.RxBleConnection
import com.polidea.rxandroidble2.RxBleDevice

/**
 * Returns `true` if connection state is [CONNECTED][RxBleConnection.RxBleConnectionState.CONNECTED].
 */
internal val RxBleDevice.isConnected: Boolean
    get() = connectionState == RxBleConnection.RxBleConnectionState.CONNECTED

fun ByteArray.toHex() = joinToString("") { String.format("%02X", (it.toInt() and 0xff)) }
fun BluetoothGattCharacteristic.hasProperty(property: Int): Boolean = (properties and property) > 0
