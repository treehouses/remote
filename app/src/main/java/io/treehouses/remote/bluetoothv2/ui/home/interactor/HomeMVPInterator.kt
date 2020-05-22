package io.treehouses.remote.bluetoothv2.ui.home.interactor

import com.polidea.rxandroidble2.RxBleConnection
import com.polidea.rxandroidble2.RxBleDevice
import com.polidea.rxandroidble2.scan.ScanResult
import io.reactivex.Observable
import io.treehouses.remote.bluetoothv2.base.interactor.MVPInteractor

interface HomeMVPInterator : MVPInteractor {

     fun scanDevices(): Observable<ScanResult>
     fun connectToDevice(rxBleDevice: RxBleDevice): Observable<RxBleConnection>
//     fun disconnectDevice() :  Observable<String>
}