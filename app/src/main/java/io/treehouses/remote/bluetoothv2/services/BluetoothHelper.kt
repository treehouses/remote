package io.treehouses.remote.bluetoothv2.services

import com.polidea.rxandroidble2.RxBleConnection
import com.polidea.rxandroidble2.RxBleDevice
import com.polidea.rxandroidble2.scan.ScanResult
import io.reactivex.Observable

interface BluetoothHelper {
    fun scanDevices():  Observable<ScanResult>
    fun observeStateChange(bleDevice: RxBleDevice): Observable<RxBleConnection.RxBleConnectionState>
    fun connectDevice(bleDevice: RxBleDevice):Observable<RxBleConnection>
//    fun disconnectDevice() :  Observable<String>
    fun readMessage(): Observable<String>
    fun writeMesage(): Observable<String>
}