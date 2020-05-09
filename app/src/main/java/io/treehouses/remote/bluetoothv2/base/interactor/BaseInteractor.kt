package io.treehouses.remote.bluetoothv2.base.interactor

import android.bluetooth.le.BluetoothLeScanner
import io.treehouses.remote.bluetoothv2.services.BluetoothConnectionService

open class BaseInteractor() : MVPInteractor {

    protected lateinit var bluetoothConnectionService: BluetoothConnectionService

    constructor(bluetoothConnectionService: BluetoothConnectionService) : this() {
        this.bluetoothConnectionService = bluetoothConnectionService
    }

    override fun isBluetoothConnected() = this.bluetoothConnectionService.isConnected

    override fun disconnect() = this.bluetoothConnectionService.triggerDisconnect()

}