package io.treehouses.remote.bluetoothv2.ui.home.interactor

import com.polidea.rxandroidble2.scan.ScanResult
import io.treehouses.remote.bluetoothv2.base.interactor.BaseInteractor
import io.treehouses.remote.bluetoothv2.services.BluetoothConnectionService
import io.treehouses.remote.bluetoothv2.services.BluetoothHelper
import javax.inject.Inject

class HomeInteractor @Inject internal constructor(bluetoothHelper: BluetoothHelper) : BaseInteractor(bluetoothHelper = bluetoothHelper), HomeMVPInterator {

    override fun scanDevices() = bluetoothHelper.scanDevices()

    override fun connectToDevice(mac: String) = bluetoothHelper.connectDevice()

    override fun disconnectDevice() = bluetoothHelper.disconnectDevice()

}