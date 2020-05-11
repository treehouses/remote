package io.treehouses.remote.bluetoothv2.services

import com.polidea.rxandroidble2.RxBleDevice
import com.polidea.rxandroidble2.scan.ScanFilter
import com.polidea.rxandroidble2.scan.ScanResult
import com.polidea.rxandroidble2.scan.ScanSettings
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.treehouses.remote.MainApplication
import io.treehouses.remote.bluetoothv2.util.isConnected
import javax.inject.Inject

class BluetoothConnectionService @Inject constructor() {
    public interface ScanBluetoothCallback {
        fun onDeviceFound(device: ScanResult)
        fun onScanFailure(message: String)
        fun requestLocationPermission()
    }

    public interface ConnectionCallback {
        fun onConnected()
        fun onDisconnected()
        fun onConnectionFailed(string: String?)
    }

    private val rxBleClient = MainApplication.rxBleClient

    private var scanDisposable: Disposable? = null
    private val isScanning: Boolean
        get() = scanDisposable != null

    private var connectionDisposable: Disposable? = null

    private var stateDisposable: Disposable? = null

    private  var bleDevice: RxBleDevice? = null

    public val isConnected : Boolean
            get() = bleDevice != null

    public fun scanDevices(callback: ScanBluetoothCallback) {
        if (isScanning) {
            scanDisposable?.dispose()
        } else {
            if (rxBleClient.isScanRuntimePermissionGranted) {
                return scanBleDevices()
                        .observeOn(AndroidSchedulers.mainThread())
                        .doFinally { scanDisposable = null }
                        .subscribe({ callback.onDeviceFound(it) }, { callback.onScanFailure("Unable to search devices") })
                        .let { scanDisposable = it }
            } else {
                callback.requestLocationPermission()
            }
        }
    }

    public fun onPause() {
        if (isScanning) scanDisposable?.dispose()
    }

    private fun scanBleDevices(): Observable<ScanResult> {
        val scanSettings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .build()

        val scanFilter = ScanFilter.Builder()
                .build()
        return rxBleClient.scanBleDevices(scanSettings, scanFilter)
    }

    private fun observeConnectionChange(device: RxBleDevice) {
        device.observeConnectionStateChanges()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { it.toString() }
                .let { stateDisposable = it }
    }

    public fun triggerDisconnect() = connectionDisposable?.dispose()

    public fun connectDisconnected(device: RxBleDevice, connectionCallback: ConnectionCallback) {
        if (device.isConnected) {
            triggerDisconnect()
            connectionCallback.onDisconnected()
        } else {
            bleDevice!!.establishConnection(true)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doFinally { connectionDisposable = null }
                    .subscribe({ connectionCallback.onConnected() }, { connectionCallback.onConnectionFailed(it.message) })
                    .let { connectionDisposable = it }
        }
    }
}