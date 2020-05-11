package io.treehouses.remote.bluetoothv2.ui.home.interactor

import com.polidea.rxandroidble2.scan.ScanResult
import io.treehouses.remote.bluetoothv2.base.interactor.BaseInteractor
import io.treehouses.remote.bluetoothv2.services.BluetoothConnectionService
import javax.inject.Inject

class HomeInteractor @Inject internal constructor(bluetoothConnectionService: BluetoothConnectionService) : BaseInteractor(bluetoothConnectionService = bluetoothConnectionService), HomeMVPInterator, BluetoothConnectionService.ScanBluetoothCallback {

    override fun scanDevices() {
        //   bluetoothConnectionService.scanDevices()
        //todo : return observable from this method
    }

    override fun connectToDevice(mac: String) {
    }

    override fun disconnectDevice() {}
    override fun onDeviceFound(device: ScanResult) {

    }

    override fun onScanFailure(message: String) {
    }

    override fun requestLocationPermission() {
    }
}