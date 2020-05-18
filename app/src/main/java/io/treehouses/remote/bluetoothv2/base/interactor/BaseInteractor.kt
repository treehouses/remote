package io.treehouses.remote.bluetoothv2.base.interactor

import android.bluetooth.le.BluetoothLeScanner
import io.treehouses.remote.bluetoothv2.services.BluetoothConnectionService
import io.treehouses.remote.bluetoothv2.services.BluetoothHelper

open class BaseInteractor() : MVPInteractor {

    protected lateinit var bluetoothHelper: BluetoothHelper

    constructor(bluetoothHelper: BluetoothHelper) : this() {
        this.bluetoothHelper = bluetoothHelper
    }



}