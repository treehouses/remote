package io.treehouses.remote.bluetoothv2.services

import com.polidea.rxandroidble2.scan.ScanResult
import io.reactivex.Observable

interface BluetoothHelper {
    fun scanDevices():  Observable<ScanResult>
    fun connectDevice(): Observable<String>
    fun disconnectDevice() :  Observable<String>
    fun getConnectionState(): Observable<String>
    fun readMessage(): Observable<String>
    fun writeMesage(): Observable<String>
}