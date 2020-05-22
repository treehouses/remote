package io.treehouses.remote.bluetoothv2.services

import android.content.Context
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.RxBleConnection
import com.polidea.rxandroidble2.RxBleDevice
import com.polidea.rxandroidble2.scan.ScanFilter
import com.polidea.rxandroidble2.scan.ScanResult
import com.polidea.rxandroidble2.scan.ScanSettings
import io.reactivex.Observable
import javax.inject.Inject

class BluetoothService @Inject constructor(context: Context) : BluetoothHelper {
    private val rxBleClient = RxBleClient.create(context)

    override fun scanDevices(): Observable<ScanResult> {
        val scanSettings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .build()
        val scanFilter = ScanFilter.Builder()
                .build()
        return rxBleClient.scanBleDevices(scanSettings, scanFilter)

    }

    override fun observeStateChange(bleDevice: RxBleDevice): Observable<RxBleConnection.RxBleConnectionState> {
        return bleDevice.observeConnectionStateChanges()
    }


    override fun connectDevice(bleDevice: RxBleDevice): Observable<RxBleConnection> {
        return bleDevice.establishConnection(true)
    }

//    override fun disconnectDevice(): Observable<String> {
//        return Observable.ambArray();
//    }
//
//    override fun getConnectionState(): Observable<String> {
//        return Observable.ambArray();
//    }

    override fun readMessage(): Observable<String> {
        return Observable.ambArray();
    }

    override fun writeMesage(): Observable<String> {
        return Observable.ambArray();
    }
}