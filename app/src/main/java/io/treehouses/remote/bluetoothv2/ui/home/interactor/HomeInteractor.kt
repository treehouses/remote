package io.treehouses.remote.bluetoothv2.ui.home.interactor

import bleshadow.javax.inject.Inject
import io.treehouses.remote.bluetoothv2.base.interactor.BaseInteractor
import io.treehouses.remote.bluetoothv2.services.BluetoothConnectionService

class HomeInteractor @Inject internal constructor(bluetoothConnectionService: BluetoothConnectionService) : BaseInteractor( bluetoothConnectionService = bluetoothConnectionService), HomeMVPInterator {

    override fun scanDevices() {}
    override fun connectToDevice(mac: String) {
    }
    override fun disconnectDevice() {}
}